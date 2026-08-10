package com.usermanagement.domain.port.in;

import java.util.UUID;

/**
 * Inbound port — soft-delete a user account.
 */
public interface DeleteAccountUseCase {

    /**
     * Delete (soft-delete) a user account.
     *
     * @param userId the ID of the user to delete
     */
    void delete(UUID userId);
}
