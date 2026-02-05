package com.wit.be.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자의 ID를 주입받기 위한 어노테이션.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * @GetMapping("/me")
 * public UserResponse getMe(@CurrentUserId Long userId) {
 *     return userQueryService.findById(userId);
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {}
