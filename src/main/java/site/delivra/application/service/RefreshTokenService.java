package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.entities.RefreshToken;
import com.post_hub.iam_service.model.entities.User;

public interface RefreshTokenService {

    RefreshToken generateOrUpdateRefreshToken(User user);

    RefreshToken validateAndRefreshToken(String refreshToken);

}
