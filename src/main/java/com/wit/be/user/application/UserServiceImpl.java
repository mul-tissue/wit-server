package com.wit.be.user.application;

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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;

    @Override
    @Transactional
    public User findOrCreateUser(SocialType socialType, String providerId, String email) {
        return userRepository
                .findBySocialTypeAndProviderId(socialType, providerId)
                .orElseGet(() -> createUser(socialType, providerId, email));
    }

    @Override
    @Transactional
    public User completeOnboarding(
            Long userId, String nickname, Gender gender, LocalDate birthDate) {
        User user = userQueryService.findById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_DELETED);
        }

        user.completeOnboarding(nickname, gender, birthDate);
        return user;
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = userQueryService.findById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_DELETED);
        }

        user.updateProfile(nickname, profileImageUrl);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userQueryService.findById(userId);

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
                        .status(UserStatus.PENDING_AGREEMENT)
                        .role(UserRole.USER)
                        .build();

        return userRepository.save(user);
    }
}
