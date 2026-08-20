package com.usermanagement.application.service;

import com.usermanagement.application.port.in.command.VerifyOtpCommand;
import com.usermanagement.application.port.out.OtpRepository;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.InvalidOtpException;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyOtpServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpRepository otpRepository;

    @InjectMocks
    private VerifyOtpService verifyOtpService;

    @Test
    @DisplayName("verify() activates user and deletes OTP on valid code")
    void verify_activatesUserAndDeletesOtp() {
        User user = User.create("carol@example.com", "hashed", "Carol", "White");
        when(otpRepository.findOtp("carol@example.com")).thenReturn(Optional.of("123456"));
        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        verifyOtpService.verify(new VerifyOtpCommand("carol@example.com", "123456"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(otpRepository).deleteOtp("carol@example.com");
    }

    @Test
    @DisplayName("verify() throws InvalidOtpException when OTP not found")
    void verify_throwsWhenOtpNotFound() {
        when(otpRepository.findOtp(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyOtpService.verify(
                new VerifyOtpCommand("carol@example.com", "000000")
        )).isInstanceOf(InvalidOtpException.class);
    }

    @Test
    @DisplayName("verify() throws InvalidOtpException when OTP does not match")
    void verify_throwsWhenOtpMismatch() {
        when(otpRepository.findOtp("carol@example.com")).thenReturn(Optional.of("999999"));

        assertThatThrownBy(() -> verifyOtpService.verify(
                new VerifyOtpCommand("carol@example.com", "000000")
        )).isInstanceOf(InvalidOtpException.class);
    }
}
