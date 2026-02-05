package com.wit.be.terms.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 약관 유형 */
@Getter
@RequiredArgsConstructor
public enum TermsType {
    SERVICE("서비스 이용약관"),
    PRIVACY("개인정보 처리방침"),
    MARKETING("마케팅 정보 수신");

    private final String value;
}
