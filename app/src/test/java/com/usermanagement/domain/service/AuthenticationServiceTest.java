package com.usermanagement.domain.service;

import com.usermanagement.application.dto.AuthRequest;
import com.usermanagement.application.dto.AuthResponse;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.out.UserRepository;
import com.usermanagement.infrastructure.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = mock(JwtService.class);
        service = new AuthenticationService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void authenticate_returnsTokenForValidCredentials() {
        String rawPassword = "password123";
        User user = new User(UUID.randomUUID(), "user@example.com",
                passwordEncoder.encode(rawPassword), "User Name",
                true, true, LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = service.authenticate(new AuthRequest("user@example.com", rawPassword));

        assertThat(response.getAccessToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void authenticate_throwsForWrongPassword() {
        User user = new User(UUID.randomUUID(), "user@example.com",
                passwordEncoder.encode("correctPassword"), "User Name",
                true, true, LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.authenticate(new AuthRequest("user@example.com", "wrongPassword")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void authenticate_throwsForUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(new AuthRequest("unknown@example.com", "pass")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
