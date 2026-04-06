package site.delivra.application.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.constants.ApiLogMessage;
import site.delivra.application.model.dto.user.UserProfileDTO;
import site.delivra.application.model.request.user.ForgotPasswordRequest;
import site.delivra.application.model.request.user.LoginRequest;
import site.delivra.application.model.request.user.RegistrationUserRequest;
import site.delivra.application.model.request.user.ResetPasswordRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.service.AuthService;
import site.delivra.application.utils.ApiUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<UserProfileDTO> result = authService.login(loginRequest);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);


    }

    @GetMapping("refresh/token")
    public ResponseEntity<DelivraResponse<UserProfileDTO>> refreshToken(
            @RequestParam(name = "token") String refreshToken,
            HttpServletResponse response) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<UserProfileDTO> userProfileDTOIamResponse = authService.refreshAccessToken(refreshToken);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(userProfileDTOIamResponse.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(userProfileDTOIamResponse);

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationUserRequest registrationUserRequest,
            HttpServletResponse response) {

        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        DelivraResponse<UserProfileDTO> result= authService.register(registrationUserRequest);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(DelivraResponse.createSuccessful("If this email exists, a reset link has been sent."));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(DelivraResponse.createSuccessful("Password updated successfully."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(DelivraResponse.createSuccessful("Email verified successfully."));
    }
}
