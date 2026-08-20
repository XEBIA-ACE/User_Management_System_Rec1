package com.usermanagement.application.port.out;

import com.usermanagement.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — persistence operations for User aggregate.
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteById(UUID id);
}
