package com.usermanagement.application.port.in;

import com.usermanagement.application.port.in.command.RegisterUserCommand;
import com.usermanagement.domain.model.User;

/**
 * Inbound port — user registration use case.
 */
public interface RegisterUserUseCase {
    User register(RegisterUserCommand command);
}
