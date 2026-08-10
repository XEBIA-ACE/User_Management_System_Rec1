package com.usermanagement.domain.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {
    void deleteUser(UUID userId);
}
