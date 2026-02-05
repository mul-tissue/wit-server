package com.wit.be.terms.dto.response;

import com.wit.be.terms.domain.Terms;
import com.wit.be.terms.domain.TermsType;

public record TermsResponse(
        String publicId,
        TermsType type,
        String title,
        String content,
        String version,
        boolean required) {

    public static TermsResponse from(Terms terms) {
        return new TermsResponse(
                terms.getPublicId(),
                terms.getType(),
                terms.getTitle(),
                terms.getContent(),
                terms.getVersion(),
                terms.isRequired());
    }
}
