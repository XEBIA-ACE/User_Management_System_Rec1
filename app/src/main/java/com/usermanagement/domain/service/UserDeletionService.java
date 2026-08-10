package com.usermanagement.domain.service;

import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.in.DeleteUserUseCase;
import com.usermanagement.domain.port.out.EventPublisher;
import com.usermanagement.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserDeletionService implements DeleteUserUseCase {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public UserDeletionService(UserRepository userRepository, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        userRepository.deleteById(userId);
        eventPublisher.publishUserDeleted(user.getEmail());
    }
}
