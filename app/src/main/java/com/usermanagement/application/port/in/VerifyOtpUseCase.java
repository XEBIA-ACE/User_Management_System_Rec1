package com.usermanagement.application.port.in;

import com.usermanagement.application.port.in.command.VerifyOtpCommand;

/**
 * Inbound port — OTP verification use case.
 */
public interface VerifyOtpUseCase {
    void verify(VerifyOtpCommand command);
}
