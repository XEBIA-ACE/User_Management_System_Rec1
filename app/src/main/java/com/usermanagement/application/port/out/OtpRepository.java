package com.usermanagement.application.port.out;

import java.time.Duration;

/**
 * Outbound port — OTP cache operations backed by Redis.
 */
public interface OtpRepository {
    void saveOtp(String email, String otp, Duration ttl);
    java.util.Optional<String> findOtp(String email);
    void deleteOtp(String email);
}
