package com.construction.construction_management.repository;

import com.construction.construction_management.entity.OtpPurpose;
import com.construction.construction_management.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findFirstByIdentifierOrderByCreatedAtDesc(String identifier);
    long countByIdentifierAndCreatedAtAfter(String identifier, LocalDateTime after);
    List<OtpToken> findByIdentifierAndPurposeAndConsumedFalse(String identifier, OtpPurpose purpose);
    Optional<OtpToken> findFirstByIdentifierAndPurposeAndConsumedFalseOrderByCreatedAtDesc(String identifier, OtpPurpose purpose);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
