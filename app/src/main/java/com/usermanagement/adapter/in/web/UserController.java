package com.usermanagement.adapter.in.web;

import com.usermanagement.adapter.in.web.dto.AuthRequest;
import com.usermanagement.adapter.in.web.dto.AuthResponse;
import com.usermanagement.adapter.in.web.dto.RegisterRequest;
import com.usermanagement.adapter.in.web.dto.RegisterResponse;
import com.usermanagement.adapter.in.web.dto.VerifyOtpRequest;
import com.usermanagement.application.port.in.AuthenticateUserUseCase;
import com.usermanagement.application.port.in.DeleteUserUseCase;
import com.usermanagement.application.port.in.RegisterUserUseCase;
import com.usermanagement.application.port.in.VerifyOtpUseCase;
import com.usermanagement.application.port.in.command.AuthenticateUserCommand;
import com.usermanagement.application.port.in.command.RegisterUserCommand;
import com.usermanagement.application.port.in.command.VerifyOtpCommand;
import com.usermanagement.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );
        User user = registerUserUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(user.getId().toString(), user.getEmail()));
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        String token = authenticateUserUseCase.authenticate(
                new AuthenticateUserCommand(request.email(), request.password())
        );
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        verifyOtpUseCase.verify(new VerifyOtpCommand(request.email(), request.otp()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        deleteUserUseCase.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
