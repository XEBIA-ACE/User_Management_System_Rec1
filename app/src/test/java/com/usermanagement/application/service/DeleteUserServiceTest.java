package com.usermanagement.application.service;

import com.usermanagement.application.port.out.EventPublisher;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.UserNotFoundException;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks
    private DeleteUserService deleteUserService;

    @Test
    @DisplayName("delete() soft-deletes user and publishes event")
    void delete_softDeletesAndPublishesEvent() {
        User user = User.create("dave@example.com", "hashed", "Dave", "Brown");
        user.activate();
        UUID userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        deleteUserService.delete(userId);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(eventPublisher).publishUserDeleted(userId.toString());
    }

    @Test
    @DisplayName("delete() throws UserNotFoundException for unknown id")
    void delete_throwsForUnknownUser() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteUserService.delete(unknownId))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(eventPublisher);
    }
}
