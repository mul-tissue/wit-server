package com.wit.be.infra.oauth.dto;

import lombok.Builder;

@Builder
public record OAuthUserInfo(String providerId, String email) {}
