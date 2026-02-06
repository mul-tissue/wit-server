package com.wit.be.terms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TermsAgreementRequest(
        @Valid @NotEmpty(message = "약관 동의 목록은 비어있을 수 없습니다.") List<TermsAgreement> agreements) {

    public record TermsAgreement(
            @jakarta.validation.constraints.NotBlank(message = "약관 ID는 필수입니다.")
                    String termsPublicId,
            boolean agreed) {}
}
