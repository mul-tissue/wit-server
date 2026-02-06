package com.wit.be.terms.repository;

import com.wit.be.terms.domain.UserTermsAgreement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {

    Optional<UserTermsAgreement> findByUserIdAndTermsId(Long userId, Long termsId);

    List<UserTermsAgreement> findByUserId(Long userId);

    @Query(
            "SELECT uta FROM UserTermsAgreement uta WHERE uta.user.id = :userId AND uta.agreed = true")
    List<UserTermsAgreement> findAgreedByUserId(@Param("userId") Long userId);

    @Query(
            "SELECT COUNT(uta) = "
                    + "(SELECT COUNT(t) FROM Terms t WHERE t.required = true AND t.active = true) "
                    + "FROM UserTermsAgreement uta "
                    + "WHERE uta.user.id = :userId AND uta.agreed = true AND uta.terms.required = true")
    boolean hasAgreedToAllRequiredTerms(@Param("userId") Long userId);

    boolean existsByUserIdAndTermsId(Long userId, Long termsId);
}
