package com.wit.be.terms.repository;

import com.wit.be.terms.domain.Terms;
import com.wit.be.terms.domain.TermsType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findByActiveTrue();

    Optional<Terms> findByPublicId(String publicId);

    Optional<Terms> findByTypeAndActiveTrue(TermsType type);

    List<Terms> findByRequiredTrueAndActiveTrue();
}
