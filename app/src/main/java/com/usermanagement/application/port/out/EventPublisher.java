package com.usermanagement.application.port.out;

/**
 * Outbound port — event publishing to RabbitMQ.
 */
public interface EventPublisher {
    void publishUserRegistered(String email, String userId);
    void publishUserDeleted(String userId);
}
