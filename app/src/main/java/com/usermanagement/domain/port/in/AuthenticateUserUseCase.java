package com.usermanagement.domain.port.in;

/**
 * Inbound port — authenticate a user and return a JWT.
 */
public interface AuthenticateUserUseCase {

    /**
     * Authenticate a user with email and password.
     *
     * @param command the authentication command
     * @return a signed JWT access token
     */
    String authenticate(AuthenticateUserCommand command);

    record AuthenticateUserCommand(
            String email,
            String password
    ) {}
}
