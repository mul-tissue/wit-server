package com.wit.be.auth.dto.response;

import com.wit.be.user.domain.User;
import com.wit.be.user.domain.UserStatus;

public record SocialLoginResponse(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        UserStatus status,
        String nickname,
        String profileImageUrl) {

    public static SocialLoginResponse from(
            User user,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn) {
        return new SocialLoginResponse(
                accessToken,
                accessTokenExpiresIn,
                refreshToken,
                refreshTokenExpiresIn,
                user.getStatus(),
                user.getNickname(),
                user.getProfileImageUrl());
    }
}
