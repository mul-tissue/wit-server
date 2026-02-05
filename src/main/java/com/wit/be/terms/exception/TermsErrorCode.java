package com.wit.be.terms.exception;

import com.wit.be.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TermsErrorCode implements ErrorCode {
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS__001", "약관을 찾을 수 없습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS__002", "필수 약관에 동의해야 합니다."),
    ALREADY_AGREED(HttpStatus.CONFLICT, "TERMS__003", "이미 동의한 약관입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
