package com.wit.be.user.exception;

import com.wit.be.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER__001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER__002", "이미 삭제된 사용자입니다."),
    USER_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "USER__003", "활성화되지 않은 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER__004", "이미 사용 중인 이메일입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
