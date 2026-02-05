package com.wit.be.user.api;

import com.wit.be.common.annotation.CurrentUserId;
import com.wit.be.user.application.UserService;
import com.wit.be.user.dto.request.OnboardingRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
