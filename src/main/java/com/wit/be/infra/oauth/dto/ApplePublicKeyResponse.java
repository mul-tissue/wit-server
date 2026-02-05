package com.wit.be.infra.oauth.dto;

import java.util.List;

public record ApplePublicKeyResponse(List<AppleKey> keys) {

    public record AppleKey(String kty, String kid, String use, String alg, String n, String e) {}

    public AppleKey getMatchingKey(String kid) {
        return keys.stream().filter(key -> key.kid().equals(kid)).findFirst().orElse(null);
    }
}
