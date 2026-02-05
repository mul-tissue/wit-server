package com.wit.be.infra.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.be.common.exception.BusinessException;
import com.wit.be.common.exception.code.ErrorCode;
import com.wit.be.common.exception.dto.ErrorResponse;
import com.wit.be.common.response.BaseResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            log.warn("JWT Exception: {}", e.getMessage());
            setErrorResponse(response, e.getErrorCode());
        }
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage());
        BaseResponse<ErrorResponse> baseResponse =
                BaseResponse.fail(errorCode.getHttpStatus().value(), errorResponse);

        response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
    }
}
