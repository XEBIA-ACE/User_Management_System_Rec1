package com.usermanagement.domain.port.in;

import com.usermanagement.application.dto.VerifyOtpRequest;

public interface VerifyAccountUseCase {
    void verify(VerifyOtpRequest request);
}
