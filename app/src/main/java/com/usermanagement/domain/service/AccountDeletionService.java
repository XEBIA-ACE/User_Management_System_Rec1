package com.usermanagement.domain.service;

import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import com.usermanagement.domain.port.in.DeleteAccountUseCase;
import com.usermanagement.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountDeletionService implements DeleteAccountUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.DELETED);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }
}
