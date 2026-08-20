package com.usermanagement.application.port.in.command;

/**
 * Immutable command object for OTP verification.
 */
public record VerifyOtpCommand(
        String email,
        String otp
) {}
