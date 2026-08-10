package com.usermanagement.domain.service;

import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import com.usermanagement.domain.port.in.AuthenticateUserUseCase;
import com.usermanagement.domain.port.out.TokenPort;
import com.usermanagement.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthenticationService")
class UserAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenPort tokenPort;

    private PasswordEncoder passwordEncoder;
    private UserAuthenticationService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new UserAuthenticationService(userRepository, tokenPort, passwordEncoder);
    }

    private User activeUser(String email, String rawPassword) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("authenticate — returns JWT for valid credentials")
    void authenticate_returnsToken() {
        User user = activeUser("alice@example.com", "password123");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(tokenPort.generateToken(any(User.class))).thenReturn("jwt-token");

        String token = service.authenticate(
                new AuthenticateUserUseCase.AuthenticateUserCommand("alice@example.com", "password123"));

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("authenticate — throws for wrong password")
    void authenticate_throwsForWrongPassword() {
        User user = activeUser("alice@example.com", "password123");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.authenticate(
                new AuthenticateUserUseCase.AuthenticateUserCommand("alice@example.com", "wrongpass")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("authenticate — throws for non-existent user")
    void authenticate_throwsForUnknownEmail() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(
                new AuthenticateUserUseCase.AuthenticateUserCommand("ghost@example.com", "pass")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("authenticate — throws when account is not ACTIVE")
    void authenticate_throwsForInactiveAccount() {
        User user = activeUser("pending@example.com", "password123");
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.authenticate(
                new AuthenticateUserUseCase.AuthenticateUserCommand("pending@example.com", "password123")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");
    }
}
