package site.delivra.application.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.mapper.UserMapper;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.user.UserProfileDTO;
import site.delivra.application.model.entities.RefreshToken;
import site.delivra.application.model.entities.Role;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.request.user.LoginRequest;
import site.delivra.application.model.request.user.RegistrationUserRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.repository.RoleRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.security.JwtTokenProvider;
import site.delivra.application.security.validation.AccessValidator;
import site.delivra.application.service.AuthService;
import site.delivra.application.service.RefreshTokenService;
import site.delivra.application.service.model.DelivraServiceUserRole;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessValidator accessValidator;

    @Override
    public DelivraResponse<UserProfileDTO> login(@NotNull LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage());
        }

        User user = userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage()));


        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(user);
        String token = jwtTokenProvider.generateToken(user);
        UserProfileDTO userProfileDTO = userMapper.toUserProfileDto(user, token, refreshToken.getToken());
        userProfileDTO.setToken(token);

        return DelivraResponse.createSuccessfulWithNewToken(userProfileDTO);

    }

    @Override
    public DelivraResponse<UserProfileDTO> refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateAndRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        String token = jwtTokenProvider.generateToken(user);

        return DelivraResponse.createSuccessfulWithNewToken(userMapper.toUserProfileDto(user, token, refreshToken.getToken()));


    }

    @Override
    public DelivraResponse<UserProfileDTO> register(@NotNull RegistrationUserRequest registrationUserRequest) {

        accessValidator.validateNewUser(
                registrationUserRequest.getUsername(),
                registrationUserRequest.getEmail(),
                registrationUserRequest.getPassword(),
                registrationUserRequest.getConfirmPassword()
        );

        Role userRole = roleRepository.findByName(DelivraServiceUserRole.USER.getRole())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()));

        User newUser = userMapper.fromDto(registrationUserRequest);
        newUser.setPassword(passwordEncoder.encode(registrationUserRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        userRepository.save(newUser);

        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(newUser);
        String token = jwtTokenProvider.generateToken(newUser);
        UserProfileDTO userProfileDTO = userMapper.toUserProfileDto(newUser, token, refreshToken.getToken());
        userProfileDTO.setToken(token);

        return DelivraResponse.createSuccessfulWithNewToken(userProfileDTO);

    }
}
