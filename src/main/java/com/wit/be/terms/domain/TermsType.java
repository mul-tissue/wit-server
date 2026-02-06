package com.wit.be.terms.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 약관 유형 */
@Getter
@RequiredArgsConstructor
public enum TermsType {
    TERMS_OF_SERVICE("서비스 이용약관"),
    PRIVACY_POLICY("개인정보 처리방침"),
    MARKETING("마케팅 정보 수신");

    private final String value;
}
