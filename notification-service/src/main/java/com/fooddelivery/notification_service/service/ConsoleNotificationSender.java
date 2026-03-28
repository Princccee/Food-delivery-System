package com.fooddelivery.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsoleNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        log.info("--------------------------------------------------");
        log.info("SENDING NOTIFICATION TO: {}", recipient);
        log.info("MESSAGE: {}", message);
        log.info("--------------------------------------------------");
    }
}
