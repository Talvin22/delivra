package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.kafka.service.KafkaMessageService;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.entities.RefreshToken;
import com.post_hub.iam_service.model.entities.User;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.repository.RefreshTokenRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.security.JwtTokenProvider;
import com.post_hub.iam_service.service.RefreshTokenService;
import com.post_hub.iam_service.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final KafkaMessageService kafkaMessageService;

    @Override
    public RefreshToken generateOrUpdateRefreshToken(User user) {
        return refreshTokenRepository.findByUserId(user.getId())
                .map(refreshToken ->
                {
                    refreshToken.setCreated(LocalDateTime.now());
                    refreshToken.setToken(ApiUtils.generateUuidWithoutDash());
                    return refreshTokenRepository.save(refreshToken);
                })
                .orElseGet(() -> {
                    RefreshToken newToken = new RefreshToken();
                    newToken.setUser(user);
                    newToken.setCreated(LocalDateTime.now());
                    newToken.setToken(ApiUtils.generateUuidWithoutDash());
                    return refreshTokenRepository.save(newToken);
                });

    }

    @Override
    public RefreshToken validateAndRefreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.NOT_FOUND_REFRESH_TOKEN.getMessage()));

        refreshToken.setCreated(LocalDateTime.now());
        refreshToken.setToken(ApiUtils.generateUuidWithoutDash());
        return refreshTokenRepository.save(refreshToken);
    }
}
