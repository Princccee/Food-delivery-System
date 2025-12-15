package com.fooddelivery.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiateRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    private Long amount; // in paise (e.g., INR 250 -> 25000)

    private String currency = "INR";
}
