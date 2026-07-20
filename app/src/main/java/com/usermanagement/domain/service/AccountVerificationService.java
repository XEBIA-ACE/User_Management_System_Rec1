package com.usermanagement.domain.service;

import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import com.usermanagement.domain.port.in.VerifyAccountUseCase;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountVerificationService implements VerifyAccountUseCase {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;

    @Override
    @Transactional
    public void verify(VerifyAccountCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));

        OtpToken token = otpRepository.findByUserIdAndCode(command.userId(), command.otpCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP code"));

        if (token.isUsed()) {
            throw new IllegalStateException("OTP code has already been used");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("OTP code has expired");
        }

        token.setUsed(true);
        otpRepository.save(token);

        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }
}
