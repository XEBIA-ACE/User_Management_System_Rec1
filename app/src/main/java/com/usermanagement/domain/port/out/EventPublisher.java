package com.usermanagement.domain.port.out;

public interface EventPublisher {
    void publishUserRegistered(String email, String otpToken);
    void publishUserDeleted(String email);
}
