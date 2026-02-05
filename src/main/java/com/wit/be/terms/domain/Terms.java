package com.wit.be.terms.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 약관 엔티티 */
@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermsType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private boolean active;

    @Builder
    private Terms(TermsType type, String title, String version, boolean required, boolean active) {
        this.publicId = PublicIdGenerator.generate();
        this.type = type;
        this.title = title;
        this.version = version;
        this.required = required;
        this.active = active;
    }

    // 비즈니스 메서드
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isOptional() {
        return !this.required;
    }
}
