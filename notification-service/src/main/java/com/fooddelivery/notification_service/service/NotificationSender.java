package com.fooddelivery.notification_service.service;

public interface NotificationSender {
    void send(String recipient, String message);
}
