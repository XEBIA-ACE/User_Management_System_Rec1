package com.usermanagement.application.service;

import com.usermanagement.application.port.in.AuthenticateUserUseCase;
import com.usermanagement.application.port.in.command.AuthenticateUserCommand;
import com.usermanagement.application.port.out.TokenProvider;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.InvalidCredentialsException;
import com.usermanagement.domain.exception.UserNotFoundException;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    public String authenticate(AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName()
        );

        String token = tokenProvider.generateToken(user.getId().toString(), claims);
        log.info("User authenticated: {}", user.getId());
        return token;
    }
}
