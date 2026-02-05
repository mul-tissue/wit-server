package com.wit.be.user.repository;

import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialTypeAndProviderId(SocialType socialType, String providerId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
