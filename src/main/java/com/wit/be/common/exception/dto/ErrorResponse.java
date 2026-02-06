package com.wit.be.common.exception.dto;

public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }
}
