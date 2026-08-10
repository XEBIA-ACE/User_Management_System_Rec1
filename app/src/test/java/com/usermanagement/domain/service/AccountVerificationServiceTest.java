package com.usermanagement.domain.service;

import com.usermanagement.application.dto.VerifyOtpRequest;
import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountVerificationServiceTest {

    private UserRepository userRepository;
    private OtpRepository otpRepository;
    private AccountVerificationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        otpRepository = mock(OtpRepository.class);
        service = new AccountVerificationService(userRepository, otpRepository);
    }

    @Test
    void verify_successfullyVerifiesUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "u@example.com", "hash", "Name",
                false, true, LocalDateTime.now(), LocalDateTime.now());
        OtpToken otp = new OtpToken(UUID.randomUUID(), userId, "123456",
                LocalDateTime.now().plusMinutes(5), false, LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndToken(userId, "123456")).thenReturn(Optional.of(otp));
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.verify(new VerifyOtpRequest(userId, "123456"));

        assertThat(user.isVerified()).isTrue();
        assertThat(otp.isUsed()).isTrue();
    }

    @Test
    void verify_throwsForExpiredOtp() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "u@example.com", "hash", "Name",
                false, true, LocalDateTime.now(), LocalDateTime.now());
        OtpToken otp = new OtpToken(UUID.randomUUID(), userId, "123456",
                LocalDateTime.now().minusMinutes(1), false, LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndToken(userId, "123456")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.verify(new VerifyOtpRequest(userId, "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTP expired");
    }

    @Test
    void verify_throwsForAlreadyUsedOtp() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "u@example.com", "hash", "Name",
                false, true, LocalDateTime.now(), LocalDateTime.now());
        OtpToken otp = new OtpToken(UUID.randomUUID(), userId, "123456",
                LocalDateTime.now().plusMinutes(5), true, LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(otpRepository.findByUserIdAndToken(userId, "123456")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.verify(new VerifyOtpRequest(userId, "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTP already used");
    }
}
