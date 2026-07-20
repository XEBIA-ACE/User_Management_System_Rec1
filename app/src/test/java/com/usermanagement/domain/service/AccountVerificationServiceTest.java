package com.usermanagement.domain.service;

import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import com.usermanagement.domain.port.in.VerifyAccountUseCase;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountVerificationService")
class AccountVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpRepository otpRepository;

    private AccountVerificationService service;

    @BeforeEach
    void setUp() {
        service = new AccountVerificationService(userRepository, otpRepository);
    }

    private User pendingUser(UUID id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .passwordHash("hash")
                .status(UserStatus.PENDING_VERIFICATION)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private OtpToken validToken(UUID userId) {
        return OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .code("123456")
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build();
    }

    @Test
    @DisplayName("verify — activates user on valid OTP")
    void verify_activatesUser() {
        UUID userId = UUID.randomUUID();
        User user = pendingUser(userId);
        OtpToken token = validToken(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndCode(userId, "123456")).thenReturn(Optional.of(token));
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.verify(new VerifyAccountUseCase.VerifyAccountCommand(userId, "123456"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("verify — throws for expired OTP")
    void verify_throwsForExpiredOtp() {
        UUID userId = UUID.randomUUID();
        User user = pendingUser(userId);
        OtpToken expiredToken = OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .code("999999")
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndCode(userId, "999999")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> service.verify(
                new VerifyAccountUseCase.VerifyAccountCommand(userId, "999999")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("verify — throws for already-used OTP")
    void verify_throwsForUsedOtp() {
        UUID userId = UUID.randomUUID();
        User user = pendingUser(userId);
        OtpToken usedToken = validToken(userId);
        usedToken.setUsed(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndCode(userId, "123456")).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> service.verify(
                new VerifyAccountUseCase.VerifyAccountCommand(userId, "123456")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been used");
    }
}
