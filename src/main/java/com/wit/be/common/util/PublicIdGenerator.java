package com.wit.be.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * 외부 노출용 공개 ID 생성 유틸리티.
 *
 * <p>ULID(Universally Unique Lexicographically Sortable Identifier)를 사용합니다.
 *
 * <ul>
 *   <li>26자 길이의 문자열
 *   <li>시간순 정렬 가능 (Lexicographically Sortable)
 *   <li>URL-safe (Base32 인코딩)
 *   <li>UUID보다 짧고 가독성이 좋음
 * </ul>
 */
public final class PublicIdGenerator {

    private PublicIdGenerator() {}

    /**
     * 새로운 ULID를 생성합니다.
     *
     * <p>Monotonic ULID를 사용하여 같은 밀리초 내에서도 유일성을 보장합니다.
     *
     * @return 26자 ULID 문자열
     */
    public static String generate() {
        return UlidCreator.getMonotonicUlid().toString();
    }
}
