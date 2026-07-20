package com.usermanagement.domain.service;

import com.usermanagement.domain.model.User;
import com.usermanagement.domain.model.UserStatus;
import com.usermanagement.domain.port.in.AuthenticateUserUseCase;
import com.usermanagement.domain.port.out.TokenPort;
import com.usermanagement.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final TokenPort tokenPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String authenticate(AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active. Current status: " + user.getStatus());
        }

        return tokenPort.generateToken(user);
    }
}
