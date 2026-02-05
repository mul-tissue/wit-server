package com.wit.be.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret, String issuer, Long accessTokenExpiration, Long refreshTokenExpiration) {}
