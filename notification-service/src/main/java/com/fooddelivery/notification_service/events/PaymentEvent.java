package com.fooddelivery.notification_service.events;

import java.util.UUID;

// Mirroring payment-service
public record PaymentEvent(
         UUID orderId,
         UUID customerId,
         String customerEmail,
         String paymentStatus
) {}
