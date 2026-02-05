package com.wit.be.user.dto.request;

import com.wit.be.user.domain.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OnboardingRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
                @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
                String nickname,
        @NotNull(message = "성별은 필수입니다.") Gender gender,
        @NotNull(message = "출생연도는 필수입니다.")
                @Min(value = 1900, message = "올바른 출생연도를 입력해주세요.")
                @Max(value = 2025, message = "올바른 출생연도를 입력해주세요.")
                Integer birthYear) {}
