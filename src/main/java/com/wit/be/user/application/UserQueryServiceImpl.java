package com.wit.be.user.application;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import com.wit.be.user.exception.UserErrorCode;
import com.wit.be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    public User findById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean existsBySocialTypeAndProviderId(SocialType socialType, String providerId) {
        return userRepository.findBySocialTypeAndProviderId(socialType, providerId).isPresent();
    }
}
