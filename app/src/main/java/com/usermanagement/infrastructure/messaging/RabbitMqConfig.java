package com.usermanagement.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange.notifications:notifications.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.otp:otp.queue}")
    private String otpQueue;

    @Value("${rabbitmq.routing-key.otp:otp.send}")
    private String otpRoutingKey;

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue otpQueue() {
        return new Queue(otpQueue, true);
    }

    @Bean
    public Binding otpBinding(Queue otpQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(otpQueue).to(notificationsExchange).with(otpRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
