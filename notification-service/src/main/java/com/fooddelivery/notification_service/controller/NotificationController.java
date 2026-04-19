package com.fooddelivery.notification_service.controller;

import com.fooddelivery.notification_service.model.Notification;
import com.fooddelivery.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Notification>> getNotificationsByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(notificationRepository.findByCustomerId(customerId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Notification>> getNotificationsByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(notificationRepository.findByOrderId(orderId));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }
}
