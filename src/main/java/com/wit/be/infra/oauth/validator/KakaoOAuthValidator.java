package com.wit.be.infra.oauth.validator;

import com.wit.be.infra.oauth.client.OAuthHttpClient;
import com.wit.be.infra.oauth.dto.KakaoUserResponse;
import com.wit.be.infra.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoOAuthValidator implements OAuthValidator {

    private final OAuthHttpClient oAuthHttpClient;

    @Override
    public OAuthUserInfo validate(String token) {
        // Kakao는 Access Token으로 사용자 정보 조회
        KakaoUserResponse response = oAuthHttpClient.getKakaoUserInfo(token);

        return OAuthUserInfo.builder()
                .providerId(response.getProviderId())
                .email(response.getEmail())
                .build();
    }
}
