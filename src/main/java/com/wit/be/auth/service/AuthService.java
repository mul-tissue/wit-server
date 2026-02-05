package com.wit.be.auth.service;

import com.wit.be.auth.dto.request.SocialLoginRequest;
import com.wit.be.auth.dto.response.SocialLoginResponse;
import com.wit.be.infra.oauth.dto.OAuthUserInfo;
import com.wit.be.infra.oauth.validator.AppleOAuthValidator;
import com.wit.be.infra.oauth.validator.GoogleOAuthValidator;
import com.wit.be.infra.oauth.validator.KakaoOAuthValidator;
import com.wit.be.infra.oauth.validator.OAuthValidator;
import com.wit.be.infra.redis.RefreshTokenRepository;
import com.wit.be.infra.security.jwt.JwtUtil;
import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import com.wit.be.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    private final KakaoOAuthValidator kakaoOAuthValidator;
    private final GoogleOAuthValidator googleOAuthValidator;
    private final AppleOAuthValidator appleOAuthValidator;

    /**
     * 소셜 로그인을 수행합니다.
     *
     * @param request 소셜 로그인 요청 (socialType, token)
     * @return 로그인 응답 (publicId, accessToken, refreshToken, isNewUser)
     */
    @Transactional
    public SocialLoginResponse socialLogin(SocialLoginRequest request) {
        // 1. OAuth 토큰 검증 및 사용자 정보 조회
        OAuthUserInfo oAuthUserInfo = validateOAuthToken(request.socialType(), request.token());

        log.info(
                "OAuth validated - socialType: {}, providerId: {}, email: {}",
                request.socialType(),
                oAuthUserInfo.providerId(),
                oAuthUserInfo.email());

        // 2. 사용자 조회 또는 생성
        boolean isNewUser =
                !userService.existsBySocialTypeAndProviderId(
                        request.socialType(), oAuthUserInfo.providerId());

        User user =
                userService.findOrCreateUser(
                        request.socialType(), oAuthUserInfo.providerId(), oAuthUserInfo.email());

        // 3. JWT 토큰 생성
        List<String> roles = List.of("ROLE_" + user.getRole().getValue().toUpperCase());
        String accessToken = jwtUtil.generateAccessToken(user.getId(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), roles);

        // 4. Refresh Token 저장
        refreshTokenRepository.save(
                user.getId(), refreshToken, jwtUtil.getRefreshTokenExpiration());

        log.info("Social login successful - userId: {}, isNewUser: {}", user.getId(), isNewUser);

        return new SocialLoginResponse(
                user.getPublicId(),
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpiration(),
                jwtUtil.getRefreshTokenExpiration(),
                isNewUser);
    }

    /**
     * 로그아웃을 수행합니다.
     *
     * @param userId 사용자 ID
     */
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("User logged out - userId: {}", userId);
    }

    private OAuthUserInfo validateOAuthToken(SocialType socialType, String token) {
        OAuthValidator validator = getOAuthValidator(socialType);
        return validator.validate(token);
    }

    private OAuthValidator getOAuthValidator(SocialType socialType) {
        return switch (socialType) {
            case KAKAO -> kakaoOAuthValidator;
            case GOOGLE -> googleOAuthValidator;
            case APPLE -> appleOAuthValidator;
        };
    }
}
