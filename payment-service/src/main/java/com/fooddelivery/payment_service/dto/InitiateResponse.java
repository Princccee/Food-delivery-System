package com.fooddelivery.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class InitiateResponse {
    private UUID paymentId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private double amount;
    private String currency;
}
