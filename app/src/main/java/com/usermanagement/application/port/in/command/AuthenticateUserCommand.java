package com.usermanagement.application.port.in.command;

/**
 * Immutable command object for user authentication.
 */
public record AuthenticateUserCommand(
        String email,
        String password
) {}
