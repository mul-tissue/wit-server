package com.wit.be.infra.oauth.exception;

import com.wit.be.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OAuthErrorCode implements ErrorCode {
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH__001", "유효하지 않은 토큰입니다."),
    UNSUPPORTED_SOCIAL_TYPE(HttpStatus.BAD_REQUEST, "AUTH__002", "지원하지 않는 소셜 로그인입니다."),
    OAUTH_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH__003", "OAuth 서버 오류가 발생했습니다."),
    INVALID_APPLE_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH__004", "Apple 토큰 검증에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
