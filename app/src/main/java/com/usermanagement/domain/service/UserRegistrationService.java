package com.usermanagement.domain.service;

import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import com.usermanagement.domain.port.in.RegisterUserUseCase;
import com.usermanagement.domain.port.out.NotificationPort;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRegistrationService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final NotificationPort notificationPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already registered: " + command.email());
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .status(UserStatus.PENDING_VERIFICATION)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User saved = userRepository.save(user);

        String otpCode = generateOtp();
        OtpToken token = OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(saved.getId())
                .code(otpCode)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();
        otpRepository.save(token);

        notificationPort.sendOtp(saved.getId(), saved.getEmail(), otpCode);

        return saved;
    }

    private String generateOtp() {
        int code = (int) (Math.random() * 900_000) + 100_000;
        return String.valueOf(code);
    }
}
