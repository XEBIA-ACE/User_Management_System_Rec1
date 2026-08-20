package com.usermanagement.application.service;

import com.usermanagement.application.port.in.DeleteUserUseCase;
import com.usermanagement.application.port.out.EventPublisher;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.UserNotFoundException;
import com.usermanagement.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        user.markDeleted();
        userRepository.save(user);
        eventPublisher.publishUserDeleted(userId.toString());

        log.info("User soft-deleted: {}", userId);
    }
}
