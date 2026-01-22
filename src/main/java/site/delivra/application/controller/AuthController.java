package com.post_hub.iam_service.controller;

import com.post_hub.iam_service.model.constants.ApiLogMassage;
import com.post_hub.iam_service.model.request.user.LoginRequest;
import com.post_hub.iam_service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.service.AuthService;
import com.post_hub.iam_service.utils.ApiUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        log.trace(ApiLogMassage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserProfileDTO> result = authService.login(loginRequest);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);


    }

    @GetMapping("refresh/token")
    public ResponseEntity<IamResponse<UserProfileDTO>> refreshToken(
            @RequestParam(name = "token") String refreshToken,
            HttpServletResponse response) {
        log.trace(ApiLogMassage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserProfileDTO> userProfileDTOIamResponse = authService.refreshAccessToken(refreshToken);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(userProfileDTOIamResponse.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(userProfileDTOIamResponse);

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationUserRequest registrationUserRequest,
            HttpServletResponse response) {

        log.trace(ApiLogMassage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        IamResponse<UserProfileDTO> result= authService.register(registrationUserRequest);
        Cookie authorizationCookie = ApiUtils.createAuthCookie(result.getPayload().getToken());
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(result);



    }
}
