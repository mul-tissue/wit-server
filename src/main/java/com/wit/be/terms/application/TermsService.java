package com.wit.be.terms.application;

import com.wit.be.terms.dto.request.TermsAgreementRequest;

/** 약관 서비스 인터페이스 (CUD 작업) */
public interface TermsService {

    /**
     * 약관 동의를 처리합니다.
     *
     * @param userId 사용자 ID
     * @param request 약관 동의 요청
     */
    void agreeToTerms(Long userId, TermsAgreementRequest request);
}
