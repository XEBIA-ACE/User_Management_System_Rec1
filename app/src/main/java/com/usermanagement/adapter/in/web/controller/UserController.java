package com.usermanagement.adapter.in.web.controller;

import com.usermanagement.domain.port.in.DeleteAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final DeleteAccountUseCase deleteAccountUseCase;

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal String userId) {
        deleteAccountUseCase.delete(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
