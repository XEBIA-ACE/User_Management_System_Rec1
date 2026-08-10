package com.usermanagement.domain.port.out;

import java.util.UUID;

/**
 * Outbound port — send notifications (e.g. OTP emails) via message broker.
 */
public interface NotificationPort {

    /**
     * Send an OTP verification code to the user.
     *
     * @param userId the target user's ID
     * @param email  the target email address
     * @param otp    the one-time password code
     */
    void sendOtp(UUID userId, String email, String otp);
}
