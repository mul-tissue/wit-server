package com.wit.be.terms.api;

import com.wit.be.common.annotation.CurrentUserId;
import com.wit.be.terms.application.TermsQueryService;
import com.wit.be.terms.application.TermsService;
import com.wit.be.terms.dto.request.TermsAgreementRequest;
import com.wit.be.terms.dto.response.TermsResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;
    private final TermsQueryService termsQueryService;

    @GetMapping("/active")
    public ResponseEntity<List<TermsResponse>> getActiveTerms() {
        List<TermsResponse> terms = termsQueryService.getActiveTerms();
        return ResponseEntity.ok(terms);
    }

    @PostMapping("/agree")
    public ResponseEntity<Void> agreeToTerms(
            @CurrentUserId Long userId, @Valid @RequestBody TermsAgreementRequest request) {
        termsService.agreeToTerms(userId, request);
        return ResponseEntity.ok().build();
    }
}
