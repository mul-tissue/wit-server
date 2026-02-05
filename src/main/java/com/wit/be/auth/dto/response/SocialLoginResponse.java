package com.wit.be.auth.dto.response;

public record SocialLoginResponse(
        String publicId,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean isNewUser) {}
