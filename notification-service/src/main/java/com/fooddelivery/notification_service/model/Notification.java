package com.fooddelivery.notification_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID orderId;

    private UUID customerId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String recipient;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status; // PENDING, SENT, FAILED

    private Instant createdAt;
}
