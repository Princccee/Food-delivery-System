package com.fooddelivery.payment_service.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


public record PaymentEvent(
         UUID orderId,
         String paymentStatus
) {}
