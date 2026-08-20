package com.usermanagement.application.port.in.command;

/**
 * Immutable command object for user registration.
 */
public record RegisterUserCommand(
        String email,
        String password,
        String firstName,
        String lastName
) {}
