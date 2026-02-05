package com.wit.be.auth.api;

import com.wit.be.auth.application.AuthService;
import com.wit.be.auth.dto.request.SocialLoginRequest;
import com.wit.be.auth.dto.response.SocialLoginResponse;
import com.wit.be.common.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "01. 인증", description = "소셜 로그인 및 로그아웃 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/social")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @Valid @RequestBody SocialLoginRequest request) {
        SocialLoginResponse response = authService.socialLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUserId Long userId) {
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }
}
