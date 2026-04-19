package com.fooddelivery.notification_service.service;

import com.fooddelivery.notification_service.model.Notification;
import com.fooddelivery.notification_service.model.NotificationStatus;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    public void createNotification(UUID orderId, UUID customerId, NotificationType type, String message, String recipient) {
        Notification notification = Notification.builder()
                .orderId(orderId)
                .customerId(customerId)
                .type(type)
                .content(message)
                .recipient(recipient)
                .status(NotificationStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created with status PENDING for order: {}", orderId);

        try {
            notificationSender.send(recipient, message);
            notification.setStatus(NotificationStatus.SENT);
            log.info("Notification successfully sent to {}", recipient);
        } catch (Exception e) {
            log.error("Failed to send notification to {}: {}", recipient, e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
        } finally {
            notificationRepository.save(notification);
        }
    }
}
