package com.usermanagement.domain.exception;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException() {
        super("OTP is invalid or has expired.");
    }
}
