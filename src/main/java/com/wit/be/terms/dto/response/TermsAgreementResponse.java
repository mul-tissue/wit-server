package com.wit.be.terms.dto.response;

import com.wit.be.user.domain.UserStatus;

/**
 * 약관 동의 응답 DTO.
 *
 * @param userStatus 약관 동의 후 사용자 상태 (PENDING_ONBOARDING)
 */
public record TermsAgreementResponse(UserStatus userStatus) {}
