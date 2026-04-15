package com.fooddelivery.order_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.order_service.events.PaymentEvent;
import com.fooddelivery.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;
    // REMOVE the ObjectMapper declaration completely

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Processing PaymentEvent for Order ID: {} with Status: {}", event.orderId(), event.paymentStatus());

        try {
            if ("SUCCESS".equalsIgnoreCase(event.paymentStatus()) || "PAID".equalsIgnoreCase(event.paymentStatus())) {
                orderService.markPaid(event.orderId());
            } else if ("FAILED".equalsIgnoreCase(event.paymentStatus())) {
                orderService.markPaymentFailed(event.orderId());
            }
        } catch (Exception e) {
            log.error("Error processing PaymentEvent: {}", e.getMessage());
        }
    }
}
