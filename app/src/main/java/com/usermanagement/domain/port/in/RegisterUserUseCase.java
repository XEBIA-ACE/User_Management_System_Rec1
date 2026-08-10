package com.usermanagement.domain.port.in;

import com.usermanagement.application.dto.RegisterRequest;
import com.usermanagement.application.dto.UserResponse;

public interface RegisterUserUseCase {
    UserResponse register(RegisterRequest request);
}
