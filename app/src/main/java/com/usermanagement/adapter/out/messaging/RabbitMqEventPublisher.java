package com.usermanagement.adapter.out.messaging;

import com.usermanagement.application.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredKey;

    @Value("${app.rabbitmq.routing-key.user-deleted}")
    private String userDeletedKey;

    @Override
    public void publishUserRegistered(String email, String userId) {
        Map<String, String> payload = Map.of("email", email, "userId", userId);
        rabbitTemplate.convertAndSend(exchange, userRegisteredKey, payload);
        log.info("Published user.registered event for userId={}", userId);
    }

    @Override
    public void publishUserDeleted(String userId) {
        Map<String, String> payload = Map.of("userId", userId);
        rabbitTemplate.convertAndSend(exchange, userDeletedKey, payload);
        log.info("Published user.deleted event for userId={}", userId);
    }
}
