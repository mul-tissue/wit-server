package com.wit.be.auth.dto.request;

import com.wit.be.user.domain.SocialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
        @NotNull(message = "소셜 타입은 필수입니다.") SocialType socialType,
        @NotBlank(message = "토큰은 필수입니다.") String token) {}
