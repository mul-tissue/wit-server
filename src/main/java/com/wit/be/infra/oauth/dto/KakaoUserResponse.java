package com.wit.be.infra.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {

    public record KakaoAccount(String email) {}

    public String getProviderId() {
        return String.valueOf(id);
    }

    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }
}
