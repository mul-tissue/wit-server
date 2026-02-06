package com.wit.be.auth.application;

import com.wit.be.auth.dto.request.SocialLoginRequest;
import com.wit.be.auth.dto.response.SocialLoginResponse;

/** 인증 서비스 인터페이스 */
public interface AuthService {

    /**
     * 소셜 로그인을 수행합니다.
     *
     * @param request 소셜 로그인 요청 (socialType, token)
     * @return 로그인 응답 (publicId, accessToken, refreshToken, isNewUser)
     */
    SocialLoginResponse socialLogin(SocialLoginRequest request);

    /**
     * 로그아웃을 수행합니다.
     *
     * @param userId 사용자 ID
     */
    void logout(Long userId);
}
