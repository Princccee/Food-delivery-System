package com.fooddelivery.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateResponse {
    private UUID paymentId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Double amount;
    private String currency;
}
