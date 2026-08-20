package com.usermanagement.application.port.in;

import com.usermanagement.application.port.in.command.AuthenticateUserCommand;

/**
 * Inbound port — authentication use case.
 */
public interface AuthenticateUserUseCase {
    /**
     * Authenticates a user and returns a signed JWT token.
     *
     * @param command credentials
     * @return signed JWT string
     */
    String authenticate(AuthenticateUserCommand command);
}
