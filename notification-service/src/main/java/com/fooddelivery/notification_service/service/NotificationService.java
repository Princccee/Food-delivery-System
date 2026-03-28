package com.fooddelivery.notification_service.service;

import com.fooddelivery.notification_service.model.Notification;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    public void createNotification(UUID orderId, NotificationType type, String message, String recipient) {
        Notification notification = Notification.builder()
                .orderId(orderId)
                .type(type)
                .content(message)
                .recipient(recipient)
                .status("SENT")
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
        notificationSender.send(recipient, message);
    }
}
