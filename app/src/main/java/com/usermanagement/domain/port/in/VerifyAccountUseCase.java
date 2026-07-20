package com.usermanagement.domain.port.in;

import java.util.UUID;

/**
 * Inbound port — verify a user account using an OTP code.
 */
public interface VerifyAccountUseCase {

    /**
     * Verify a user account using the provided OTP code.
     *
     * @param command the verification command
     */
    void verify(VerifyAccountCommand command);

    record VerifyAccountCommand(
            UUID userId,
            String otpCode
    ) {}
}
