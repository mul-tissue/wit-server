package com.wit.be.user.api;

import com.wit.be.common.annotation.CurrentUserId;
import com.wit.be.user.application.UserService;
import com.wit.be.user.dto.request.OnboardingRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "03. 사용자", description = "사용자 온보딩 및 프로필 관리 API")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/onboarding")
    public ResponseEntity<Void> completeOnboarding(
            @CurrentUserId Long userId, @Valid @RequestBody OnboardingRequest request) {
        userService.completeOnboarding(
                userId, request.nickname(), request.gender(), request.birthDate());
        return ResponseEntity.ok().build();
    }
}
