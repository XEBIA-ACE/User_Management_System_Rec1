package com.usermanagement.domain.port.out;

import com.usermanagement.domain.model.OtpToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — persistence contract for {@link OtpToken}.
 */
public interface OtpRepository {

    OtpToken save(OtpToken token);

    Optional<OtpToken> findByUserIdAndCode(UUID userId, String code);

    void deleteByUserId(UUID userId);
}
