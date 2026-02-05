package com.wit.be.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.user.domain.Gender;
import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import com.wit.be.user.domain.UserStatus;
import com.wit.be.user.exception.UserErrorCode;
import com.wit.be.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired private UserService userService;

    @Autowired private UserQueryService userQueryService;

    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("소셜 로그인 사용자 신규 생성")
    void findOrCreateUser_WhenNewUser_ShouldCreateUser() {
        // Given
        SocialType socialType = SocialType.KAKAO;
        String providerId = "kakao123";
        String email = "test@example.com";

        // When
        User user = userService.findOrCreateUser(socialType, providerId, email);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getPublicId()).isNotNull();
        assertThat(user.getPublicId()).hasSize(26); // ULID is 26 characters
        assertThat(user.getSocialType()).isEqualTo(socialType);
        assertThat(user.getProviderId()).isEqualTo(providerId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_AGREEMENT);
    }

    @Test
    @DisplayName("소셜 로그인 사용자 기존 사용자 조회")
    void findOrCreateUser_WhenExistingUser_ShouldReturnExistingUser() {
        // Given
        SocialType socialType = SocialType.GOOGLE;
        String providerId = "google456";
        String email = "existing@example.com";
        User existingUser = userService.findOrCreateUser(socialType, providerId, email);

        // When
        User foundUser = userService.findOrCreateUser(socialType, providerId, email);

        // Then
        assertThat(foundUser.getId()).isEqualTo(existingUser.getId());
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("온보딩 완료 시 사용자 활성화")
    void completeOnboarding_ShouldActivateUser() {
        // Given
        User user =
                userService.findOrCreateUser(SocialType.KAKAO, "kakao789", "onboard@example.com");
        String nickname = "테스터";
        Gender gender = Gender.MALE;
        LocalDate birthDate = LocalDate.of(1990, 1, 1);

        // When
        User completedUser =
                userService.completeOnboarding(user.getId(), nickname, gender, birthDate);

        // Then
        assertThat(completedUser.getNickname()).isEqualTo(nickname);
        assertThat(completedUser.getGender()).isEqualTo(gender);
        assertThat(completedUser.getBirthDate()).isEqualTo(birthDate);
        assertThat(completedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("사용자 삭제 시 소프트 삭제")
    void deleteUser_ShouldSoftDelete() {
        // Given
        User user =
                userService.findOrCreateUser(SocialType.APPLE, "apple999", "delete@example.com");
        userService.completeOnboarding(
                user.getId(), "닉네임", Gender.FEMALE, LocalDate.of(1995, 5, 15));

        // When
        userService.deleteUser(user.getId());

        // Then
        User deletedUser = userQueryService.findById(user.getId());
        assertThat(deletedUser.getStatus()).isEqualTo(UserStatus.DELETED);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void findById_WhenUserNotFound_ShouldThrowException() {
        // Given
        Long nonExistentUserId = 999999L;

        // When & Then
        assertThatThrownBy(() -> userQueryService.findById(nonExistentUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제된 사용자 온보딩 시도 시 예외 발생")
    void completeOnboarding_WhenUserDeleted_ShouldThrowException() {
        // Given
        User user =
                userService.findOrCreateUser(SocialType.KAKAO, "deleted123", "deleted@example.com");
        userService.completeOnboarding(user.getId(), "닉네임", Gender.MALE, LocalDate.of(1990, 1, 1));
        userService.deleteUser(user.getId());

        // When & Then
        assertThatThrownBy(
                        () ->
                                userService.completeOnboarding(
                                        user.getId(),
                                        "새닉네임",
                                        Gender.FEMALE,
                                        LocalDate.of(1995, 5, 15)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_ALREADY_DELETED);
    }
}
