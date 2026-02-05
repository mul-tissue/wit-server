package com.wit.be.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.terms.domain.Terms;
import com.wit.be.terms.domain.TermsType;
import com.wit.be.terms.dto.request.TermsAgreementRequest;
import com.wit.be.terms.dto.response.TermsResponse;
import com.wit.be.terms.exception.TermsErrorCode;
import com.wit.be.terms.repository.TermsRepository;
import com.wit.be.terms.repository.UserTermsAgreementRepository;
import com.wit.be.user.domain.SocialType;
import com.wit.be.user.domain.User;
import com.wit.be.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TermsServiceTest {

    @Autowired private TermsService termsService;

    @Autowired private TermsRepository termsRepository;

    @Autowired private UserTermsAgreementRepository userTermsAgreementRepository;

    @Autowired private UserService userService;

    private Terms serviceTerms;
    private Terms privacyTerms;
    private Terms marketingTerms;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 기존 약관 데이터 삭제 (Flyway로 생성된 데이터)
        userTermsAgreementRepository.deleteAll();
        termsRepository.deleteAll();

        // 테스트용 약관 생성
        serviceTerms =
                termsRepository.save(
                        Terms.builder()
                                .type(TermsType.SERVICE)
                                .title("서비스 이용약관")
                                .content("서비스 이용약관 내용")
                                .version("1.0")
                                .required(true)
                                .active(true)
                                .build());

        privacyTerms =
                termsRepository.save(
                        Terms.builder()
                                .type(TermsType.PRIVACY)
                                .title("개인정보 처리방침")
                                .content("개인정보 처리방침 내용")
                                .version("1.0")
                                .required(true)
                                .active(true)
                                .build());

        marketingTerms =
                termsRepository.save(
                        Terms.builder()
                                .type(TermsType.MARKETING)
                                .title("마케팅 정보 수신")
                                .content("마케팅 정보 수신 동의 내용")
                                .version("1.0")
                                .required(false)
                                .active(true)
                                .build());

        // 테스트용 사용자 생성
        testUser = userService.findOrCreateUser(SocialType.KAKAO, "test123", "test@example.com");
    }

    @Test
    @DisplayName("활성화된 약관 목록 조회")
    void getActiveTerms_ShouldReturnActiveTerms() {
        // When
        List<TermsResponse> activeTerms = termsService.getActiveTerms();

        // Then
        assertThat(activeTerms).hasSize(3);
        assertThat(activeTerms)
                .extracting(TermsResponse::type)
                .containsExactlyInAnyOrder(
                        TermsType.SERVICE, TermsType.PRIVACY, TermsType.MARKETING);
    }

    @Test
    @DisplayName("모든 필수 약관에 동의")
    void agreeToTerms_WhenAllRequiredAgreed_ShouldSucceed() {
        // Given
        TermsAgreementRequest request =
                new TermsAgreementRequest(
                        List.of(
                                new TermsAgreementRequest.TermsAgreement(
                                        serviceTerms.getPublicId(), true),
                                new TermsAgreementRequest.TermsAgreement(
                                        privacyTerms.getPublicId(), true),
                                new TermsAgreementRequest.TermsAgreement(
                                        marketingTerms.getPublicId(), false)));

        // When
        termsService.agreeToTerms(testUser.getId(), request);

        // Then
        assertThat(termsService.hasAgreedToAllRequiredTerms(testUser.getId())).isTrue();
    }

    @Test
    @DisplayName("필수 약관 미동의 시 예외 발생")
    void agreeToTerms_WhenRequiredNotAgreed_ShouldThrowException() {
        // Given
        TermsAgreementRequest request =
                new TermsAgreementRequest(
                        List.of(
                                new TermsAgreementRequest.TermsAgreement(
                                        serviceTerms.getPublicId(), true),
                                new TermsAgreementRequest.TermsAgreement(
                                        privacyTerms.getPublicId(), false), // 필수인데 미동의
                                new TermsAgreementRequest.TermsAgreement(
                                        marketingTerms.getPublicId(), false)));

        // When & Then
        assertThatThrownBy(() -> termsService.agreeToTerms(testUser.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
    }

    @Test
    @DisplayName("존재하지 않는 약관 동의 시 예외 발생")
    void agreeToTerms_WhenTermsNotFound_ShouldThrowException() {
        // Given
        TermsAgreementRequest request =
                new TermsAgreementRequest(
                        List.of(
                                new TermsAgreementRequest.TermsAgreement(
                                        "INVALID_PUBLIC_ID", true)));

        // When & Then
        assertThatThrownBy(() -> termsService.agreeToTerms(testUser.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TermsErrorCode.TERMS_NOT_FOUND);
    }

    @Test
    @DisplayName("선택 약관만 미동의해도 성공")
    void agreeToTerms_WhenOnlyOptionalNotAgreed_ShouldSucceed() {
        // Given
        TermsAgreementRequest request =
                new TermsAgreementRequest(
                        List.of(
                                new TermsAgreementRequest.TermsAgreement(
                                        serviceTerms.getPublicId(), true),
                                new TermsAgreementRequest.TermsAgreement(
                                        privacyTerms.getPublicId(), true)));
        // 마케팅 약관은 아예 안 보냄 (선택이므로 OK)

        // When
        termsService.agreeToTerms(testUser.getId(), request);

        // Then
        assertThat(termsService.hasAgreedToAllRequiredTerms(testUser.getId())).isTrue();
    }
}
