package com.usermanagement.application.service;

import com.usermanagement.application.port.in.command.AuthenticateUserCommand;
import com.usermanagement.application.port.out.TokenProvider;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.InvalidCredentialsException;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProvider tokenProvider;

    @InjectMocks
    private AuthenticateUserService authenticateUserService;

    private User activeUser() {
        User u = User.create("bob@example.com", "hashed", "Bob", "Jones");
        u.activate();
        return u;
    }

    @Test
    @DisplayName("authenticate() returns JWT token for valid credentials")
    void authenticate_returnsToken() {
        User user = activeUser();
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(tokenProvider.generateToken(anyString(), anyMap())).thenReturn("jwt.token.here");

        String token = authenticateUserService.authenticate(
                new AuthenticateUserCommand("bob@example.com", "secret")
        );

        assertThat(token).isEqualTo("jwt.token.here");
    }

    @Test
    @DisplayName("authenticate() throws InvalidCredentialsException for unknown email")
    void authenticate_throwsForUnknownEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticateUserService.authenticate(
                new AuthenticateUserCommand("nobody@example.com", "pass")
        )).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("authenticate() throws InvalidCredentialsException for wrong password")
    void authenticate_throwsForWrongPassword() {
        User user = activeUser();
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authenticateUserService.authenticate(
                new AuthenticateUserCommand("bob@example.com", "wrong")
        )).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("authenticate() throws InvalidCredentialsException for non-active user")
    void authenticate_throwsForPendingUser() {
        User user = User.create("bob@example.com", "hashed", "Bob", "Jones");
        // status is PENDING_VERIFICATION — not activated
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticateUserService.authenticate(
                new AuthenticateUserCommand("bob@example.com", "secret")
        )).isInstanceOf(InvalidCredentialsException.class);
    }
}
