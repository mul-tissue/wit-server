package com.wit.be.terms.application;

import com.wit.be.terms.dto.response.TermsResponse;
import java.util.List;

/** 약관 조회 서비스 인터페이스 (Read 작업) */
public interface TermsQueryService {

    /**
     * 활성화된 약관 목록을 조회합니다.
     *
     * @return 활성화된 약관 목록
     */
    List<TermsResponse> getActiveTerms();

    /**
     * 사용자가 모든 필수 약관에 동의했는지 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 모든 필수 약관 동의 여부
     */
    boolean hasAgreedToAllRequiredTerms(Long userId);
}
