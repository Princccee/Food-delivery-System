package com.fooddelivery.notification_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.notification_service.events.PaymentEvent;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-status", groupId = "notification-group")
    public void consume(String messageJson) {
        try {
            PaymentEvent event = objectMapper.readValue(messageJson, PaymentEvent.class);
            log.info("Received PaymentEvent for order: {} with status: {}", event.orderId(), event.paymentStatus());
            
            boolean isSuccess = "SUCCESS".equalsIgnoreCase(event.paymentStatus()) || "PAID".equalsIgnoreCase(event.paymentStatus());
            NotificationType type = isSuccess ? NotificationType.PAYMENT_SUCCESS : NotificationType.PAYMENT_FAILED;
            
            String message = isSuccess 
                    ? String.format("Payment successful for order #%s! We are starting the delivery.", event.orderId().toString().substring(0, 8))
                    : String.format("Payment failed for order #%s. Please try again or use a different method.", event.orderId().toString().substring(0, 8));

            String recipient = event.customerEmail() != null ? event.customerEmail() : "customer@example.com";

            notificationService.createNotification(event.orderId(), event.customerId(), type, message, recipient);
        } catch (Exception e) {
            log.error("Error processing PaymentEvent: {}", e.getMessage(), e);
        }
    }
}
