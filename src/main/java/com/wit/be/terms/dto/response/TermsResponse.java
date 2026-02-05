package com.wit.be.terms.dto.response;

import com.wit.be.terms.domain.Terms;
import com.wit.be.terms.domain.TermsType;

public record TermsResponse(
        String publicId,
        TermsType type,
        String title,
        String url,
        String version,
        boolean required) {

    public static TermsResponse from(Terms terms) {
        String url = generateTermsUrl(terms.getType(), terms.getVersion());
        return new TermsResponse(
                terms.getPublicId(),
                terms.getType(),
                terms.getTitle(),
                url,
                terms.getVersion(),
                terms.isRequired());
    }

    private static String generateTermsUrl(TermsType type, String version) {
        // TODO: 실제 약관 URL로 변경 필요 (현재는 임시 노션 URL)
        String typePath =
                switch (type) {
                    case TERMS_OF_SERVICE -> "terms-of-service";
                    case PRIVACY_POLICY -> "privacy-policy";
                    case MARKETING -> "marketing";
                };
        return String.format("https://wit.notion.site/terms/%s/%s", typePath, version);
    }
}
