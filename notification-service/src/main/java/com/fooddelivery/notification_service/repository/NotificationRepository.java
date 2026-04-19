package com.fooddelivery.notification_service.repository;

import com.fooddelivery.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByCustomerId(UUID customerId);
    List<Notification> findByOrderId(UUID orderId);
}
