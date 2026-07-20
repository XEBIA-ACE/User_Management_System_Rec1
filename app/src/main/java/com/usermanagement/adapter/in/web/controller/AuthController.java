package com.usermanagement.adapter.in.web.controller;

import com.usermanagement.adapter.in.web.dto.AuthRequest;
import com.usermanagement.adapter.in.web.dto.AuthResponse;
import com.usermanagement.adapter.in.web.dto.RegisterRequest;
import com.usermanagement.adapter.in.web.dto.UserResponse;
import com.usermanagement.adapter.in.web.dto.VerifyRequest;
import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.in.AuthenticateUserUseCase;
import com.usermanagement.domain.port.in.RegisterUserUseCase;
import com.usermanagement.domain.port.in.VerifyAccountUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final VerifyAccountUseCase verifyAccountUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUserUseCase.register(
                new RegisterUserUseCase.RegisterUserCommand(
                        request.email(),
                        request.password(),
                        request.firstName(),
                        request.lastName()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        String token = authenticateUserUseCase.authenticate(
                new AuthenticateUserUseCase.AuthenticateUserCommand(
                        request.email(),
                        request.password()
                )
        );
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyRequest request) {
        verifyAccountUseCase.verify(
                new VerifyAccountUseCase.VerifyAccountCommand(
                        request.userId(),
                        request.otpCode()
                )
        );
        return ResponseEntity.noContent().build();
    }
}
