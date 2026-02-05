package com.wit.be.user.application;

import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;

/** 사용자 조회 서비스 인터페이스 (Read 작업) */
public interface UserQueryService {

    /**
     * 사용자 ID로 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자
     * @throws com.wit.be.common.exception.BusinessException 사용자를 찾을 수 없는 경우
     */
    User findById(Long userId);

    /**
     * 소셜 로그인 사용자 존재 여부를 확인합니다.
     *
     * @param socialType 소셜 타입
     * @param providerId 소셜 제공자 ID
     * @return 존재 여부
     */
    boolean existsBySocialTypeAndProviderId(SocialType socialType, String providerId);
}
