package com.wit.be.user.dto.request;

import com.wit.be.user.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OnboardingRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
                @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
                String nickname,
        @NotNull(message = "성별은 필수입니다.") Gender gender,
        @NotNull(message = "생년월일은 필수입니다.") @Past(message = "생년월일은 과거 날짜여야 합니다.")
                LocalDate birthDate) {}
