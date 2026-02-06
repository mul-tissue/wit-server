package com.wit.be.infra.oauth.client;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.infra.oauth.dto.GoogleTokenResponse;
import com.wit.be.infra.oauth.dto.KakaoUserResponse;
import com.wit.be.infra.oauth.exception.OAuthErrorCode;
import com.wit.be.infra.properties.GoogleOAuthProperties;
import com.wit.be.infra.properties.KakaoOAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * RestClient 기반 OAuthHttpClient 구현체.
 *
 * <p>Spring 6.1+의 RestClient를 사용하여 OAuth 공급자와 통신합니다.
 */
@Slf4j
@Component
public class RestClientOAuthHttpClient implements OAuthHttpClient {

    private final RestClient kakaoRestClient;
    private final RestClient googleRestClient;

    public RestClientOAuthHttpClient(
            RestClient.Builder restClientBuilder,
            KakaoOAuthProperties kakaoProperties,
            GoogleOAuthProperties googleProperties) {
        this.kakaoRestClient =
                restClientBuilder.clone().baseUrl(kakaoProperties.userInfoUrl()).build();
        this.googleRestClient =
                restClientBuilder.clone().baseUrl(googleProperties.tokenInfoUrl()).build();
    }

    @Override
    public KakaoUserResponse getKakaoUserInfo(String accessToken) {
        return kakaoRestClient
                .get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            log.warn(
                                    "Kakao OAuth client error - status: {}, uri: {}",
                                    response.getStatusCode(),
                                    request.getURI());
                            throw new BusinessException(OAuthErrorCode.INVALID_ID_TOKEN);
                        })
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            log.error(
                                    "Kakao OAuth server error - status: {}, uri: {}",
                                    response.getStatusCode(),
                                    request.getURI());
                            throw new BusinessException(OAuthErrorCode.OAUTH_SERVER_ERROR);
                        })
                .body(KakaoUserResponse.class);
    }

    @Override
    public GoogleTokenResponse getGoogleTokenInfo(String idToken) {
        return googleRestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/tokeninfo")
                                        .queryParam("id_token", idToken)
                                        .build())
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            log.warn(
                                    "Google OAuth client error - status: {}, uri: {}",
                                    response.getStatusCode(),
                                    request.getURI());
                            throw new BusinessException(OAuthErrorCode.INVALID_ID_TOKEN);
                        })
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            log.error(
                                    "Google OAuth server error - status: {}, uri: {}",
                                    response.getStatusCode(),
                                    request.getURI());
                            throw new BusinessException(OAuthErrorCode.OAUTH_SERVER_ERROR);
                        })
                .body(GoogleTokenResponse.class);
    }
}
