package com.usermanagement.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;

    @Value("${app.rabbitmq.queue.user-deleted}")
    private String userDeletedQueue;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredKey;

    @Value("${app.rabbitmq.routing-key.user-deleted}")
    private String userDeletedKey;

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(userRegisteredQueue).build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(userDeletedQueue).build();
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue()).to(userExchange()).with(userRegisteredKey);
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder.bind(userDeletedQueue()).to(userExchange()).with(userDeletedKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
