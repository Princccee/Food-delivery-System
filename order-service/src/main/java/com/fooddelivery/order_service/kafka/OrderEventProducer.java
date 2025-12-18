package com.fooddelivery.order_service.kafka;

import com.fooddelivery.order_service.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String ORDER_CREATED_TOPIC = "order.created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(
                ORDER_CREATED_TOPIC,
                event.orderId().toString(),
                event
        );
    }
}
