package com.wit.be.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("user"),
    ADMIN("admin");

    private final String value;
}
