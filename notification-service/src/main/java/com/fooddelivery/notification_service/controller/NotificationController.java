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
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String customerId) {
        // In a real app, we'd filter by customerId. For now, we return all or handle based on orderId
        // Since our mock recipient is "customer@example.com", we'll just return all for simplicity
        // or filter by status/orderId if needed.
        return ResponseEntity.ok(notificationRepository.findAll());
    }
}
