package com.wit.be.user.application;

import com.wit.be.user.domain.Gender;
import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import java.time.LocalDate;

/** 사용자 서비스 인터페이스 (CUD 작업) */
public interface UserService {

    /**
     * 소셜 로그인 사용자를 조회하거나 신규 생성합니다.
     *
     * @param socialType 소셜 타입
     * @param providerId 소셜 제공자 ID
     * @param email 이메일
     * @return 조회하거나 생성된 사용자
     */
    User findOrCreateUser(SocialType socialType, String providerId, String email);

    /**
     * 온보딩을 완료하고 사용자를 활성화합니다.
     *
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param gender 성별
     * @param birthDate 생년월일
     * @return 온보딩이 완료된 사용자
     */
    User completeOnboarding(Long userId, String nickname, Gender gender, LocalDate birthDate);

    /**
     * 사용자 프로필을 수정합니다.
     *
     * @param userId 사용자 ID
     * @param nickname 닉네임 (선택)
     * @param profileImageUrl 프로필 이미지 URL (선택)
     * @return 수정된 사용자
     */
    User updateProfile(Long userId, String nickname, String profileImageUrl);

    /**
     * 사용자를 삭제 처리합니다 (소프트 삭제).
     *
     * @param userId 사용자 ID
     */
    void deleteUser(Long userId);
}
