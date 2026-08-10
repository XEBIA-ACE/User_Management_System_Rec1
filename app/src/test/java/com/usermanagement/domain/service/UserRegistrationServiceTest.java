package com.usermanagement.domain.service;

import com.usermanagement.application.dto.RegisterRequest;
import com.usermanagement.application.dto.UserResponse;
import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.out.EventPublisher;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRegistrationServiceTest {

    private UserRepository userRepository;
    private OtpRepository otpRepository;
    private EventPublisher eventPublisher;
    private PasswordEncoder passwordEncoder;
    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        otpRepository = mock(OtpRepository.class);
        eventPublisher = mock(EventPublisher.class);
        passwordEncoder = new BCryptPasswordEncoder();
        service = new UserRegistrationService(userRepository, otpRepository, eventPublisher, passwordEncoder);
    }

    @Test
    void register_successfullyCreatesUser() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) u.setId(UUID.randomUUID());
            if (u.getCreatedAt() == null) u.setCreatedAt(LocalDateTime.now());
            return u;
        });
        when(otpRepository.save(any(OtpToken.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = service.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.isVerified()).isFalse();
        verify(eventPublisher).publishUserRegistered(eq("test@example.com"), anyString());
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "Existing User");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }
}
