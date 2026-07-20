package com.usermanagement.domain.service;

import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import com.usermanagement.domain.port.in.RegisterUserUseCase;
import com.usermanagement.domain.port.out.NotificationPort;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationService")
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private NotificationPort notificationPort;

    private PasswordEncoder passwordEncoder;
    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new UserRegistrationService(userRepository, otpRepository, notificationPort, passwordEncoder);
    }

    @Test
    @DisplayName("register — creates user with PENDING_VERIFICATION status")
    void register_createsUserWithPendingStatus() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserUseCase.RegisterUserCommand command =
                new RegisterUserUseCase.RegisterUserCommand("alice@example.com", "password123", "Alice", "Smith");

        User result = service.register(command);

        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    @DisplayName("register — throws when email already exists")
    void register_throwsWhenEmailExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        RegisterUserUseCase.RegisterUserCommand command =
                new RegisterUserUseCase.RegisterUserCommand("alice@example.com", "password123", "Alice", "Smith");

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("register — sends OTP notification after registration")
    void register_sendsOtpNotification() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserUseCase.RegisterUserCommand command =
                new RegisterUserUseCase.RegisterUserCommand("bob@example.com", "password123", "Bob", "Jones");

        service.register(command);

        verify(notificationPort).sendOtp(any(UUID.class), anyString(), anyString());
    }

    @Test
    @DisplayName("register — password is stored as hash, not plaintext")
    void register_passwordIsHashed() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserUseCase.RegisterUserCommand command =
                new RegisterUserUseCase.RegisterUserCommand("carol@example.com", "mySecret99", "Carol", "White");

        service.register(command);

        User saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isNotEqualTo("mySecret99");
        assertThat(passwordEncoder.matches("mySecret99", saved.getPasswordHash())).isTrue();
    }
}
