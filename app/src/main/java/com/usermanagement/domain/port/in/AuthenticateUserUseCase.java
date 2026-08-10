package com.usermanagement.domain.port.in;

import com.usermanagement.application.dto.AuthRequest;
import com.usermanagement.application.dto.AuthResponse;

public interface AuthenticateUserUseCase {
    AuthResponse authenticate(AuthRequest request);
}
