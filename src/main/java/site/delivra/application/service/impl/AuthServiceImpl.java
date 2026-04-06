package site.delivra.application.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import site.delivra.application.model.entities.Company;
import site.delivra.application.model.enums.CompanyStatus;
import site.delivra.application.repository.CompanyRepository;
import site.delivra.application.model.entities.UserToken;
import site.delivra.application.model.enums.TokenType;
import site.delivra.application.model.request.user.ForgotPasswordRequest;
import site.delivra.application.model.request.user.ResetPasswordRequest;
import site.delivra.application.repository.UserTokenRepository;
import site.delivra.application.service.AuthService;
import site.delivra.application.service.EmailService;
import site.delivra.application.service.RefreshTokenService;
import site.delivra.application.service.model.DelivraServiceUserRole;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private final CompanyRepository companyRepository;
    private final UserTokenRepository userTokenRepository;
    private final EmailService emailService;

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

        if (user.getCompany() != null) {
            Company company = user.getCompany();
            if (company.getStatus() == CompanyStatus.TRIAL
                    && company.getTrialEndsAt() != null
                    && company.getTrialEndsAt().isBefore(java.time.LocalDateTime.now())) {
                company.setStatus(CompanyStatus.SUSPENDED);
                companyRepository.save(company);
            }
            if (company.getStatus() == CompanyStatus.SUSPENDED) {
                throw new InvalidDataException("Company account is suspended. Please contact support.");
            }
        }

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

        Role userRole = roleRepository.findByName(DelivraServiceUserRole.DRIVER.getRole())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()));

        User newUser = userMapper.fromDto(registrationUserRequest);
        newUser.setPassword(passwordEncoder.encode(registrationUserRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        userRepository.save(newUser);

        sendVerificationEmail(newUser);

        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(newUser);
        String token = jwtTokenProvider.generateToken(newUser);
        UserProfileDTO userProfileDTO = userMapper.toUserProfileDto(newUser, token, refreshToken.getToken());
        userProfileDTO.setToken(token);

        return DelivraResponse.createSuccessfulWithNewToken(userProfileDTO);

    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmailAndDeletedFalse(request.getEmail()).ifPresent(user -> {
            userTokenRepository.deleteAllByUserAndType(user, TokenType.PASSWORD_RESET);
            UserToken token = buildToken(user, TokenType.PASSWORD_RESET, 1);
            userTokenRepository.save(token);
            emailService.sendPasswordResetEmail(user, token.getToken());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException(ApiErrorMessage.MISMATCH_PASSWORDS.getMessage());
        }

        UserToken userToken = userTokenRepository
                .findByTokenAndType(request.getToken(), TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidDataException(ApiErrorMessage.INVALID_OR_EXPIRED_TOKEN.getMessage()));

        if (userToken.isExpired()) {
            throw new InvalidDataException(ApiErrorMessage.INVALID_OR_EXPIRED_TOKEN.getMessage());
        }
        if (userToken.isUsed()) {
            throw new InvalidDataException(ApiErrorMessage.TOKEN_ALREADY_USED.getMessage());
        }

        User user = userToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        userToken.setUsedAt(LocalDateTime.now());
        userTokenRepository.save(userToken);

        log.info("Password reset successful for user id={}", user.getId());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        UserToken userToken = userTokenRepository
                .findByTokenAndType(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new InvalidDataException(ApiErrorMessage.INVALID_OR_EXPIRED_TOKEN.getMessage()));

        if (userToken.isExpired()) {
            throw new InvalidDataException(ApiErrorMessage.INVALID_OR_EXPIRED_TOKEN.getMessage());
        }
        if (userToken.isUsed()) {
            throw new InvalidDataException(ApiErrorMessage.TOKEN_ALREADY_USED.getMessage());
        }

        User user = userToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        userToken.setUsedAt(LocalDateTime.now());
        userTokenRepository.save(userToken);

        log.info("Email verified for user id={}", user.getId());
    }

    @Transactional
    private void sendVerificationEmail(User user) {
        userTokenRepository.deleteAllByUserAndType(user, TokenType.EMAIL_VERIFICATION);
        UserToken token = buildToken(user, TokenType.EMAIL_VERIFICATION, 24);
        userTokenRepository.save(token);
        emailService.sendEmailVerificationEmail(user, token.getToken());
    }

    private UserToken buildToken(User user, TokenType type, int validHours) {
        UserToken token = new UserToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setType(type);
        token.setExpiresAt(LocalDateTime.now().plusHours(validHours));
        return token;
    }
}
