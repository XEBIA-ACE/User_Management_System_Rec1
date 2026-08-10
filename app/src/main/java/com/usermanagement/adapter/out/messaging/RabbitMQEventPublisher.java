package com.usermanagement.adapter.out.messaging;

import com.usermanagement.domain.port.out.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishUserRegistered(String email, String otpToken) {
        rabbitTemplate.convertAndSend(
                "user.exchange",
                "user.registered",
                Map.of("email", email, "otp", otpToken)
        );
    }

    @Override
    public void publishUserDeleted(String email) {
        rabbitTemplate.convertAndSend(
                "user.exchange",
                "user.deleted",
                Map.of("email", email)
        );
    }
}
