package com.usermanagement.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class VerifyOtpRequest {
    private UUID userId;

    @NotBlank
    private String otp;

    public VerifyOtpRequest() {}

    public VerifyOtpRequest(UUID userId, String otp) {
        this.userId = userId;
        this.otp = otp;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
