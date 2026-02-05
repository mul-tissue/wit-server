package com.wit.be.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    PENDING("pending"), // 온보딩 대기 중
    ACTIVE("active"), // 활성 사용자
    INACTIVE("inactive"), // 비활성 사용자
    DELETED("deleted"); // 탈퇴한 사용자

    private final String value;
}
