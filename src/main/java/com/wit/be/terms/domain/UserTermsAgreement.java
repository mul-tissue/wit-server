package com.wit.be.terms.domain;

import com.wit.be.common.entity.BaseTimeEntity;
import com.wit.be.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 약관 동의 엔티티 */
@Getter
@Entity
@Table(
        name = "user_terms_agreements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "terms_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermsAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(nullable = false)
    private boolean agreed;

    @Builder
    private UserTermsAgreement(User user, Terms terms, boolean agreed) {
        this.user = user;
        this.terms = terms;
        this.agreed = agreed;
    }

    // 비즈니스 메서드
    public void agree() {
        this.agreed = true;
    }

    public void withdraw() {
        this.agreed = false;
    }

    public static UserTermsAgreement create(User user, Terms terms, boolean agreed) {
        return UserTermsAgreement.builder().user(user).terms(terms).agreed(agreed).build();
    }
}
