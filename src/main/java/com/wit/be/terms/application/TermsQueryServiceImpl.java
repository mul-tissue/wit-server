package com.wit.be.terms.application;

import com.wit.be.terms.dto.response.TermsResponse;
import com.wit.be.terms.repository.TermsRepository;
import com.wit.be.terms.repository.UserTermsAgreementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsQueryServiceImpl implements TermsQueryService {

    private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;

    @Override
    public List<TermsResponse> getActiveTerms() {
        return termsRepository.findByActiveTrue().stream().map(TermsResponse::from).toList();
    }

    @Override
    public boolean hasAgreedToAllRequiredTerms(Long userId) {
        return userTermsAgreementRepository.hasAgreedToAllRequiredTerms(userId);
    }
}
