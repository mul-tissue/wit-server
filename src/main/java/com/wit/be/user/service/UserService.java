package com.wit.be.user.service;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.user.domain.Gender;
import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import com.wit.be.user.domain.UserRole;
import com.wit.be.user.domain.UserStatus;
import com.wit.be.user.exception.UserErrorCode;
import com.wit.be.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 소셜 로그인 사용자를 조회하거나 신규 생성합니다.
     *
     * @param socialType 소셜 타입
     * @param providerId 소셜 제공자 ID
     * @param email 이메일
     * @return 조회하거나 생성된 사용자
     */
    @Transactional
    public User findOrCreateUser(SocialType socialType, String providerId, String email) {
        return userRepository
                .findBySocialTypeAndProviderId(socialType, providerId)
                .orElseGet(() -> createUser(socialType, providerId, email));
    }

    /**
     * 사용자 ID로 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자
     * @throws BusinessException 사용자를 찾을 수 없는 경우
     */
    public User findById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * 소셜 로그인 사용자 존재 여부를 확인합니다.
     *
     * @param socialType 소셜 타입
     * @param providerId 소셜 제공자 ID
     * @return 존재 여부
     */
    public boolean existsBySocialTypeAndProviderId(SocialType socialType, String providerId) {
        return userRepository.findBySocialTypeAndProviderId(socialType, providerId).isPresent();
    }

    /**
     * 온보딩을 완료하고 사용자를 활성화합니다.
     *
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param gender 성별
     * @param birthDate 생년월일
     * @return 온보딩이 완료된 사용자
     */
    @Transactional
    public User completeOnboarding(
            Long userId, String nickname, Gender gender, LocalDate birthDate) {
        User user = findById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_DELETED);
        }

        user.completeOnboarding(nickname, gender, birthDate);
        return user;
    }

    /**
     * 사용자 프로필을 수정합니다.
     *
     * @param userId 사용자 ID
     * @param nickname 닉네임 (선택)
     * @param profileImageUrl 프로필 이미지 URL (선택)
     * @return 수정된 사용자
     */
    @Transactional
    public User updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = findById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_DELETED);
        }

        user.updateProfile(nickname, profileImageUrl);
        return user;
    }

    /**
     * 사용자를 삭제 처리합니다 (소프트 삭제).
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_DELETED);
        }

        user.delete();
    }

    private User createUser(SocialType socialType, String providerId, String email) {
        log.info(
                "Creating new user - socialType: {}, providerId: {}, email: {}",
                socialType,
                providerId,
                email);

        User user =
                User.builder()
                        .socialType(socialType)
                        .providerId(providerId)
                        .email(email)
                        .status(UserStatus.PENDING)
                        .role(UserRole.USER)
                        .build();

        return userRepository.save(user);
    }
}
