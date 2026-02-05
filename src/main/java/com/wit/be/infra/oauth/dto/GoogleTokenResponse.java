package com.wit.be.infra.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("sub") String sub,
        @JsonProperty("email") String email,
        @JsonProperty("email_verified") String emailVerified,
        @JsonProperty("aud") String aud,
        @JsonProperty("iss") String iss,
        @JsonProperty("exp") String exp) {

    public String getProviderId() {
        return sub;
    }

    public String getEmail() {
        return email;
    }
}
