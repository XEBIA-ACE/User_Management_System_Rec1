package com.usermanagement.domain.port.out;

import com.usermanagement.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    void deleteById(UUID id);
    boolean existsByEmail(String email);
}
