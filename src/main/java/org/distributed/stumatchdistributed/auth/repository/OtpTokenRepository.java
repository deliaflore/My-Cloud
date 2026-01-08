package org.distributed.stumatchdistributed.auth.repository;

import org.distributed.stumatchdistributed.auth.entity.OtpPurpose;
import org.distributed.stumatchdistributed.auth.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findFirstByUserIdAndPurposeAndUsedIsFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            UUID userId,
            OtpPurpose purpose,
            LocalDateTime now
    );
}

