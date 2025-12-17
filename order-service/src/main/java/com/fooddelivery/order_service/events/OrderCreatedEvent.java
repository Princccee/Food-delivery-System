package com.fooddelivery.order_service.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        Double amount
) {}
