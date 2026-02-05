package com.wit.be.terms.service;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.terms.domain.Terms;
import com.wit.be.terms.domain.UserTermsAgreement;
import com.wit.be.terms.dto.request.TermsAgreementRequest;
import com.wit.be.terms.dto.response.TermsResponse;
import com.wit.be.terms.exception.TermsErrorCode;
import com.wit.be.terms.repository.TermsRepository;
import com.wit.be.terms.repository.UserTermsAgreementRepository;
import com.wit.be.user.application.UserQueryService;
import com.wit.be.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final UserQueryService userQueryService;

    /** 활성화된 약관 목록을 조회합니다. */
    public List<TermsResponse> getActiveTerms() {
        return termsRepository.findByActiveTrue().stream().map(TermsResponse::from).toList();
    }

    /** 약관 동의를 처리합니다. */
    @Transactional
    public void agreeToTerms(Long userId, TermsAgreementRequest request) {
        User user = userQueryService.findById(userId);

        List<Terms> requiredTerms = termsRepository.findByRequiredTrueAndActiveTrue();

        for (TermsAgreementRequest.TermsAgreement agreement : request.agreements()) {
            Terms terms =
                    termsRepository
                            .findByPublicId(agreement.termsPublicId())
                            .orElseThrow(
                                    () -> new BusinessException(TermsErrorCode.TERMS_NOT_FOUND));

            // 필수 약관인데 동의하지 않은 경우
            if (terms.isRequired() && !agreement.agreed()) {
                throw new BusinessException(TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
            }

            saveOrUpdateAgreement(user, terms, agreement.agreed());
        }

        // 모든 필수 약관에 동의했는지 검증
        validateAllRequiredTermsAgreed(requiredTerms, request);
    }

    /** 사용자가 모든 필수 약관에 동의했는지 확인합니다. */
    public boolean hasAgreedToAllRequiredTerms(Long userId) {
        return userTermsAgreementRepository.hasAgreedToAllRequiredTerms(userId);
    }

    private void saveOrUpdateAgreement(User user, Terms terms, boolean agreed) {
        userTermsAgreementRepository
                .findByUserIdAndTermsId(user.getId(), terms.getId())
                .ifPresentOrElse(
                        existingAgreement -> {
                            if (agreed) {
                                existingAgreement.agree();
                            } else {
                                existingAgreement.withdraw();
                            }
                        },
                        () -> {
                            UserTermsAgreement newAgreement =
                                    UserTermsAgreement.create(user, terms, agreed);
                            userTermsAgreementRepository.save(newAgreement);
                        });
    }

    private void validateAllRequiredTermsAgreed(
            List<Terms> requiredTerms, TermsAgreementRequest request) {
        for (Terms requiredTerm : requiredTerms) {
            boolean isAgreed =
                    request.agreements().stream()
                            .filter(a -> a.termsPublicId().equals(requiredTerm.getPublicId()))
                            .findFirst()
                            .map(TermsAgreementRequest.TermsAgreement::agreed)
                            .orElse(false);

            if (!isAgreed) {
                throw new BusinessException(TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
            }
        }
    }
}
