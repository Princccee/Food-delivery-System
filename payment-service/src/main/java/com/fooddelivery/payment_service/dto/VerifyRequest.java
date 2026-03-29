package com.fooddelivery.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequest {
    @NotNull
    private UUID paymentId;
    
    @NotBlank
    private String razorpayOrderId;
    
    @NotBlank
    private String razorpayPaymentId;
    
    @NotBlank
    private String razorpaySignature;
}
