package com.wit.be.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wit.be.auth.dto.request.SocialLoginRequest;
import com.wit.be.auth.dto.response.SocialLoginResponse;
import com.wit.be.infra.oauth.dto.OAuthUserInfo;
import com.wit.be.infra.oauth.validator.AppleOAuthValidator;
import com.wit.be.infra.oauth.validator.GoogleOAuthValidator;
import com.wit.be.infra.oauth.validator.KakaoOAuthValidator;
import com.wit.be.infra.redis.RefreshTokenRepository;
import com.wit.be.infra.security.jwt.JwtUtil;
import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import com.wit.be.user.domain.UserRole;
import com.wit.be.user.domain.UserStatus;
import com.wit.be.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @InjectMocks private AuthServiceImpl authService;

    @Mock private UserService userService;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private KakaoOAuthValidator kakaoOAuthValidator;
    @Mock private GoogleOAuthValidator googleOAuthValidator;
    @Mock private AppleOAuthValidator appleOAuthValidator;

    @Test
    @DisplayName("Kakao 소셜 로그인 성공 - 신규 사용자")
    void socialLogin_Kakao_NewUser_Success() {
        // Given
        SocialLoginRequest request = new SocialLoginRequest(SocialType.KAKAO, "test-token");
        OAuthUserInfo oAuthUserInfo = new OAuthUserInfo("kakao123", "test@kakao.com");
        User user = createTestUser(SocialType.KAKAO, "kakao123", "test@kakao.com");

        when(kakaoOAuthValidator.validate("test-token")).thenReturn(oAuthUserInfo);
        when(userService.existsBySocialTypeAndProviderId(eq(SocialType.KAKAO), eq("kakao123")))
                .thenReturn(false);
        when(userService.findOrCreateUser(
                        eq(SocialType.KAKAO), eq("kakao123"), eq("test@kakao.com")))
                .thenReturn(user);
        // user.getId()가 null이므로 isNull() 매처 사용
        when(jwtUtil.generateAccessToken(isNull(), any(List.class))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(isNull(), any(List.class))).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2592000000L);

        // When
        SocialLoginResponse response = authService.socialLogin(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.publicId()).isEqualTo(user.getPublicId());
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.isNewUser()).isTrue();
    }

    @Test
    @DisplayName("Google 소셜 로그인 성공 - 기존 사용자")
    void socialLogin_Google_ExistingUser_Success() {
        // Given
        SocialLoginRequest request = new SocialLoginRequest(SocialType.GOOGLE, "google-id-token");
        OAuthUserInfo oAuthUserInfo = new OAuthUserInfo("google456", "test@gmail.com");
        User user = createTestUser(SocialType.GOOGLE, "google456", "test@gmail.com");

        when(googleOAuthValidator.validate("google-id-token")).thenReturn(oAuthUserInfo);
        when(userService.existsBySocialTypeAndProviderId(eq(SocialType.GOOGLE), eq("google456")))
                .thenReturn(true);
        when(userService.findOrCreateUser(
                        eq(SocialType.GOOGLE), eq("google456"), eq("test@gmail.com")))
                .thenReturn(user);
        when(jwtUtil.generateAccessToken(isNull(), any(List.class))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(isNull(), any(List.class))).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2592000000L);

        // When
        SocialLoginResponse response = authService.socialLogin(request);

        // Then
        assertThat(response.isNewUser()).isFalse();
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // Given
        Long userId = 1L;

        // When
        authService.logout(userId);

        // Then
        verify(refreshTokenRepository).deleteByUserId(userId);
    }

    private User createTestUser(SocialType socialType, String providerId, String email) {
        return User.builder()
                .socialType(socialType)
                .providerId(providerId)
                .email(email)
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();
    }
}
