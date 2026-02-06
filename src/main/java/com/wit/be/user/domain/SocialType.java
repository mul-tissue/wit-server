package com.wit.be.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialType {
    KAKAO("kakao"),
    GOOGLE("google"),
    APPLE("apple");

    private final String value;
}
