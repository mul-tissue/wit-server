package com.wit.be.infra.oauth.client;

import com.wit.be.infra.oauth.dto.GoogleTokenResponse;
import com.wit.be.infra.oauth.dto.KakaoUserResponse;

/**
 * OAuth 공급자와의 HTTP 통신을 추상화한 인터페이스.
 *
 * <p>구현체를 교체하여 RestClient, WebClient, Feign 등 다양한 HTTP 클라이언트를 사용할 수 있습니다.
 */
public interface OAuthHttpClient {

    /**
     * Kakao 사용자 정보를 조회합니다.
     *
     * @param accessToken Kakao Access Token (Bearer 접두사 없이)
     * @return Kakao 사용자 정보
     */
    KakaoUserResponse getKakaoUserInfo(String accessToken);

    /**
     * Google ID Token을 검증하고 토큰 정보를 조회합니다.
     *
     * @param idToken Google ID Token
     * @return Google 토큰 정보
     */
    GoogleTokenResponse getGoogleTokenInfo(String idToken);
}
