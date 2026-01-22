package site.delivra.application.service;


import site.delivra.application.model.entities.RefreshToken;
import site.delivra.application.model.entities.User;

public interface RefreshTokenService {

    RefreshToken generateOrUpdateRefreshToken(User user);

    RefreshToken validateAndRefreshToken(String refreshToken);

}
