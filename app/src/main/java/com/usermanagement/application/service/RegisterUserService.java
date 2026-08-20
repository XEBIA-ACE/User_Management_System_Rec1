package com.usermanagement.application.service;

import com.usermanagement.application.port.in.RegisterUserUseCase;
import com.usermanagement.application.port.in.command.RegisterUserCommand;
import com.usermanagement.application.port.out.EventPublisher;
import com.usermanagement.application.port.out.OtpRepository;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.UserAlreadyExistsException;
import com.usermanagement.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private static final Duration OTP_TTL = Duration.ofMinutes(10);

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        User user = User.create(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.firstName(),
                command.lastName()
        );

        User saved = userRepository.save(user);

        String otp = generateOtp();
        otpRepository.saveOtp(saved.getEmail(), otp, OTP_TTL);

        eventPublisher.publishUserRegistered(saved.getEmail(), saved.getId().toString());

        log.info("User registered: {}", saved.getId());
        return saved;
    }

    private String generateOtp() {
        int code = (int) (Math.random() * 900_000) + 100_000;
        return String.valueOf(code);
    }
}
