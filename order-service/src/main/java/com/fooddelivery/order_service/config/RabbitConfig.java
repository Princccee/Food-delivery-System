package com.fooddelivery.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    @Bean
    public Declarables orderDeclarables() {
        DirectExchange exchange =
                new DirectExchange(ORDER_EXCHANGE, true, false);

        Queue queue =
                QueueBuilder.durable(ORDER_CREATED_QUEUE).build();

        Binding binding =
                BindingBuilder.bind(queue)
                        .to(exchange)
                        .with(ORDER_CREATED_ROUTING_KEY);

        return new Declarables(exchange, queue, binding);
    }

    /**
     * 🔥 THIS IS THE MISSING PIECE
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
}
