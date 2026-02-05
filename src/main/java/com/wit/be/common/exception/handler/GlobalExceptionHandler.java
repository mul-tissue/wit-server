package com.wit.be.common.exception.handler;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.common.exception.code.ErrorCode;
import com.wit.be.common.exception.code.GlobalErrorCode;
import com.wit.be.common.exception.dto.ErrorResponse;
import com.wit.be.common.response.BaseResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** BusinessException 처리 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleBusinessException(
            BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage(), e);

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), e.getMessage());
        BaseResponse<ErrorResponse> response =
                BaseResponse.fail(errorCode.getHttpStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /** 지원하지 않는 HTTP Method */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ErrorCode errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage());
        BaseResponse<ErrorResponse> response =
                BaseResponse.fail(errorCode.getHttpStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * @Valid 검증 실패
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        List<String> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toList();
        ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.getCode(), String.join(", ", errors));
        BaseResponse<ErrorResponse> response =
                BaseResponse.fail(errorCode.getHttpStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /** JSON 파싱 오류 */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.warn("HttpMessageNotReadable: {}", ex.getMessage());

        ErrorCode errorCode = GlobalErrorCode.HTTP_MESSAGE_NOT_READABLE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage());
        BaseResponse<ErrorResponse> response =
                BaseResponse.fail(errorCode.getHttpStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /** 타입 불일치 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = GlobalErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage());
        BaseResponse<ErrorResponse> response =
                BaseResponse.fail(errorCode.getHttpStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /** RuntimeException */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleRuntimeException(RuntimeException e) {
        log.error("Unexpected RuntimeException: ", e);

        return createInternalServerErrorResponse();
    }

    /** Exception (최종 fallback) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);

        return createInternalServerErrorResponse();
    }

    private ResponseEntity<BaseResponse<ErrorResponse>> createInternalServerErrorResponse() {
        ErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage());
        BaseResponse<ErrorResponse> response =
                BaseResponse.fail(errorCode.getHttpStatus().value(), errorResponse);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
