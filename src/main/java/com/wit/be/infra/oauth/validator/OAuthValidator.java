package com.wit.be.infra.oauth.validator;

import com.wit.be.infra.oauth.dto.OAuthUserInfo;

public interface OAuthValidator {

    /**
     * ID 토큰 또는 Access Token을 검증하고 사용자 정보를 반환합니다.
     *
     * @param token ID 토큰 또는 Access Token
     * @return OAuth 사용자 정보
     */
    OAuthUserInfo validate(String token);
}
