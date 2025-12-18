package com.fooddelivery.payment_service.consumer;

import com.fooddelivery.payment_service.events.OrderCreatedEvent;
import com.fooddelivery.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order.created",
            groupId = "payment-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📥 Received ORDER_CREATED event for order {} of amount {}", event.getOrderId(), event.getAmount());
        paymentService.createPaymentFromOrder(event);
    }
}
