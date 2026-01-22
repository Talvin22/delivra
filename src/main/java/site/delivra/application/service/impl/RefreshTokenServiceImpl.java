package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.entities.RefreshToken;
import site.delivra.application.model.entities.User;
import site.delivra.application.repository.RefreshTokenRepository;
import site.delivra.application.service.RefreshTokenService;
import site.delivra.application.utils.ApiUtils;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

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
