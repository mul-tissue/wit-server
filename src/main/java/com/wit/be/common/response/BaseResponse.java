package com.wit.be.common.response;

import java.time.LocalDateTime;

public record BaseResponse<T>(boolean success, int status, T data, LocalDateTime timestamp) {

    public static <T> BaseResponse<T> success(int status, T data) {
        return new BaseResponse<>(true, status, data, LocalDateTime.now());
    }

    public static <T> BaseResponse<T> fail(int status, T data) {
        return new BaseResponse<>(false, status, data, LocalDateTime.now());
    }
}
