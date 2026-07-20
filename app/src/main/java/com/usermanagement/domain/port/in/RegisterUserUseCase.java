package com.usermanagement.domain.port.in;

import com.usermanagement.domain.model.User;

/**
 * Inbound port — register a new user.
 */
public interface RegisterUserUseCase {

    /**
     * Register a new user account.
     *
     * @param command the registration command containing user details
     * @return the newly created {@link User}
     */
    User register(RegisterUserCommand command);

    record RegisterUserCommand(
            String email,
            String password,
            String firstName,
            String lastName
    ) {}
}
