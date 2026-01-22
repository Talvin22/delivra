package site.delivra.application.service;


import site.delivra.application.model.dto.user.UserProfileDTO;
import site.delivra.application.model.request.user.LoginRequest;
import site.delivra.application.model.request.user.RegistrationUserRequest;
import site.delivra.application.model.response.DelivraResponse;

public interface AuthService {

    DelivraResponse<UserProfileDTO> login(LoginRequest loginRequest);

    DelivraResponse<UserProfileDTO> refreshAccessToken(String refreshToken);

    DelivraResponse<UserProfileDTO> register(RegistrationUserRequest registrationUserRequest);
}
