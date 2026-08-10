package com.usermanagement.domain.port.out;

import com.usermanagement.domain.model.OtpToken;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository {
    OtpToken save(OtpToken otpToken);
    Optional<OtpToken> findByUserIdAndToken(UUID userId, String token);
    void deleteByUserId(UUID userId);
}
