package com.usermanagement.domain.service;

import com.usermanagement.application.dto.VerifyOtpRequest;
import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.in.VerifyAccountUseCase;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountVerificationService implements VerifyAccountUseCase {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;

    public AccountVerificationService(UserRepository userRepository, OtpRepository otpRepository) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
    }

    @Override
    @Transactional
    public void verify(VerifyOtpRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        OtpToken otpToken = otpRepository.findByUserIdAndToken(request.getUserId(), request.getOtp())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));

        if (otpToken.isUsed()) {
            throw new IllegalArgumentException("OTP already used");
        }

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        otpToken.setUsed(true);
        otpRepository.save(otpToken);

        user.setVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
