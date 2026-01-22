package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.kafka.service.KafkaMessageService;
import com.post_hub.iam_service.mapper.UserMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.entities.Role;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.InvalidPasswordException;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.model.request.user.LoginRequest;
import com.post_hub.iam_service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_service.model.entities.RefreshToken;
import com.post_hub.iam_service.model.entities.User;
import com.post_hub.iam_service.model.exception.InvalidDataException;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.repository.RoleRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.security.JwtTokenProvider;
import com.post_hub.iam_service.security.validation.AccessValidator;
import com.post_hub.iam_service.service.AuthService;
import com.post_hub.iam_service.service.RefreshTokenService;
import com.post_hub.iam_service.service.model.IamServiceUserRole;
import com.post_hub.iam_service.utils.PasswordUtils;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final KafkaMessageService kafkaMessageService;

    @Override
    public IamResponse<UserProfileDTO> login(@NotNull LoginRequest loginRequest) {
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

        return IamResponse.createSuccessfulWithNewToken(userProfileDTO);

    }

    @Override
    public IamResponse<UserProfileDTO> refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateAndRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        String token = jwtTokenProvider.generateToken(user);

        return IamResponse.createSuccessfulWithNewToken(userMapper.toUserProfileDto(user, token, refreshToken.getToken()));


    }

    @Override
    public IamResponse<UserProfileDTO> register(@NotNull RegistrationUserRequest registrationUserRequest) {

        accessValidator.validateNewUser(
                registrationUserRequest.getUsername(),
                registrationUserRequest.getEmail(),
                registrationUserRequest.getPassword(),
                registrationUserRequest.getConfirmPassword()
        );

        Role userRole = roleRepository.findByName(IamServiceUserRole.USER.getRole())
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

        return IamResponse.createSuccessfulWithNewToken(userProfileDTO);

    }
}
