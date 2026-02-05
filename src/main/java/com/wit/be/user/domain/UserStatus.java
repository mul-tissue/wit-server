package com.wit.be.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    PENDING_AGREEMENT("약관 동의 대기 중"),
    PENDING_ONBOARDING("온보딩 대기 중"),
    ACTIVE("활성"),
    INACTIVE("비활성"),
    DELETED("탈퇴");

    private final String value;
}
