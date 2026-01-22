package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.request.user.LoginRequest;
import com.post_hub.iam_service.model.dto.user.UserProfileDTO;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;

public interface AuthService {

    IamResponse<UserProfileDTO> login(LoginRequest loginRequest);

    IamResponse<UserProfileDTO> refreshAccessToken(String refreshToken);

    IamResponse<UserProfileDTO> register(RegistrationUserRequest registrationUserRequest);
}
