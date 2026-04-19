package com.fooddelivery.notification_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.notification_service.events.OrderCreatedEvent;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "notification-group")
    public void consume(String messageJson) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(messageJson, OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent for order: {}", event.orderId());
            
            String message = String.format("Hi! Your order #%s has been successfully received and is being prepared.", 
                    event.orderId().toString().substring(0, 8));
            
            String recipient = event.customerEmail() != null ? event.customerEmail() : "customer@example.com"; 

            notificationService.createNotification(event.orderId(), event.customerId(), NotificationType.ORDER_RECEIVED, message, recipient);
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", e.getMessage(), e);
        }
    }
}
