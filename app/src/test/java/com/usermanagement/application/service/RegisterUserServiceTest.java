package com.usermanagement.application.service;

import com.usermanagement.application.port.in.command.RegisterUserCommand;
import com.usermanagement.application.port.out.EventPublisher;
import com.usermanagement.application.port.out.OtpRepository;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.UserAlreadyExistsException;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpRepository otpRepository;
    @Mock private EventPublisher eventPublisher;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserService registerUserService;

    private RegisterUserCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new RegisterUserCommand(
                "alice@example.com", "P@ssw0rd!", "Alice", "Smith"
        );
    }

    @Test
    @DisplayName("register() creates user with PENDING_VERIFICATION status")
    void register_createsUserWithPendingStatus() {
        when(userRepository.existsByEmail(validCommand.email())).thenReturn(false);
        when(passwordEncoder.encode(validCommand.password())).thenReturn("hashed");

        User savedUser = User.create(
                validCommand.email(), "hashed", validCommand.firstName(), validCommand.lastName()
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = registerUserService.register(validCommand);

        assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(result.getEmail()).isEqualTo(validCommand.email());
    }

    @Test
    @DisplayName("register() saves OTP with 10-minute TTL")
    void register_savesOtpWithTtl() {
        when(userRepository.existsByEmail(validCommand.email())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        User savedUser = User.create(
                validCommand.email(), "hashed", validCommand.firstName(), validCommand.lastName()
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        registerUserService.register(validCommand);

        verify(otpRepository).saveOtp(
                eq(validCommand.email()),
                anyString(),
                eq(Duration.ofMinutes(10))
        );
    }

    @Test
    @DisplayName("register() publishes user.registered event")
    void register_publishesEvent() {
        when(userRepository.existsByEmail(validCommand.email())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        User savedUser = User.create(
                validCommand.email(), "hashed", validCommand.firstName(), validCommand.lastName()
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        registerUserService.register(validCommand);

        verify(eventPublisher).publishUserRegistered(
                eq(validCommand.email()), anyString()
        );
    }

    @Test
    @DisplayName("register() throws UserAlreadyExistsException when email is taken")
    void register_throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(validCommand.email())).thenReturn(true);

        assertThatThrownBy(() -> registerUserService.register(validCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(validCommand.email());

        verifyNoInteractions(passwordEncoder, otpRepository, eventPublisher);
    }
}
