package com.fooddelivery.order_service.events;

import java.util.UUID;

public record PaymentEvent(
        UUID orderId,
        String paymentStatus
) {}

