package com.post_hub.iam_service.utils;

import com.post_hub.iam_service.model.constants.ApiConstants;

import com.post_hub.iam_service.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApiUtils {
    private final JwtTokenProvider jwtTokenProvider;

    public static String getMethodName(){
        try {
            return Thread.currentThread().getStackTrace()[1].getMethodName();
        } catch (Exception e) {
            return ApiConstants.UNDEFINED;
        }
    }

    public static Cookie createAuthCookie(String value){
        Cookie authenticationCookie = new Cookie(HttpHeaders.AUTHORIZATION, value);
        authenticationCookie.setHttpOnly(true);
        authenticationCookie.setSecure(true);
        authenticationCookie.setPath("/");
        authenticationCookie.setMaxAge(60 * 60 * 24 * 30);
        return authenticationCookie;

    }

    public static String generateUuidWithoutDash(){
        return UUID.randomUUID().toString().replace(ApiConstants.DASH, StringUtils.EMPTY);
    }

    public static String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Integer getUserIdFromAuthentication() {
        String jwtToken = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

        return Integer.parseInt(jwtTokenProvider.getUserId(jwtToken));
    }

}
