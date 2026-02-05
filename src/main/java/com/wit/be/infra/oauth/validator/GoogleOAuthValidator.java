package com.wit.be.infra.oauth.validator;

import com.wit.be.infra.oauth.client.OAuthHttpClient;
import com.wit.be.infra.oauth.dto.GoogleTokenResponse;
import com.wit.be.infra.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleOAuthValidator implements OAuthValidator {

    private final OAuthHttpClient oAuthHttpClient;

    @Override
    public OAuthUserInfo validate(String idToken) {
        // Google은 ID Token을 tokeninfo 엔드포인트로 검증
        GoogleTokenResponse response = oAuthHttpClient.getGoogleTokenInfo(idToken);

        return OAuthUserInfo.builder()
                .providerId(response.getProviderId())
                .email(response.getEmail())
                .build();
    }
}
