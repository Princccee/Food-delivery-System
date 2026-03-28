package com.fooddelivery.notification_service.events;

import java.util.UUID;

// Mirroring order-service
public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        Double amount
) {}
