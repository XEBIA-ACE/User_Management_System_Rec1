package com.usermanagement.domain.service;

import com.usermanagement.application.dto.RegisterRequest;
import com.usermanagement.application.dto.UserResponse;
import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.in.RegisterUserUseCase;
import com.usermanagement.domain.port.out.EventPublisher;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.domain.port.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserRegistrationService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom RANDOM = new SecureRandom();

    public UserRegistrationService(UserRepository userRepository,
                                   OtpRepository otpRepository,
                                   EventPublisher eventPublisher,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setVerified(false);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        OtpToken otpToken = new OtpToken();
        otpToken.setId(UUID.randomUUID());
        otpToken.setUserId(saved.getId());
        otpToken.setToken(otp);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpToken.setUsed(false);
        otpToken.setCreatedAt(LocalDateTime.now());
        otpRepository.save(otpToken);

        eventPublisher.publishUserRegistered(saved.getEmail(), otp);

        return new UserResponse(saved.getId(), saved.getEmail(), saved.getFullName(),
                saved.isVerified(), saved.getCreatedAt());
    }
}
