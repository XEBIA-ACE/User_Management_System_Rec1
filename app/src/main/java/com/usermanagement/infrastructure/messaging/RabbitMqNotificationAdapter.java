package com.usermanagement.infrastructure.messaging;

import com.usermanagement.domain.port.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqNotificationAdapter implements NotificationPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notifications:notifications.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.otp:otp.send}")
    private String otpRoutingKey;

    @Override
    public void sendOtp(UUID userId, String email, String otp) {
        Map<String, String> message = Map.of(
                "userId", userId.toString(),
                "email", email,
                "otp", otp
        );
        log.info("Publishing OTP notification for userId={}", userId);
        rabbitTemplate.convertAndSend(exchange, otpRoutingKey, message);
    }
}
