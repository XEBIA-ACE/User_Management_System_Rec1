package com.usermanagement.application.port.in;

import java.util.UUID;

/**
 * Inbound port — account deletion use case.
 */
public interface DeleteUserUseCase {
    void delete(UUID userId);
}
