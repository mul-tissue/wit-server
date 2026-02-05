package com.wit.be.user.domain;

import com.wit.be.common.entity.BaseTimeEntity;
import com.wit.be.common.util.PublicIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialType socialType;

    @Column(nullable = false, length = 100)
    private String providerId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private LocalDate birthDate;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Builder
    private User(
            SocialType socialType,
            String providerId,
            String email,
            String nickname,
            Gender gender,
            LocalDate birthDate,
            String profileImageUrl,
            UserStatus status,
            UserRole role) {
        this.publicId = PublicIdGenerator.generate();
        this.socialType = socialType;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.profileImageUrl = profileImageUrl;
        this.status = status != null ? status : UserStatus.PENDING;
        this.role = role != null ? role : UserRole.USER;
    }

    // 비즈니스 메서드
    public void completeOnboarding(String nickname, Gender gender, LocalDate birthDate) {
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.status = UserStatus.ACTIVE;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void delete() {
        this.status = UserStatus.DELETED;
    }

    public boolean isPending() {
        return this.status == UserStatus.PENDING;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == UserStatus.DELETED;
    }
}
