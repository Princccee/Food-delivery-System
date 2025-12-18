package com.fooddelivery.payment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;            // your Order Service order id

    @Column(nullable = true, unique = true)
    private String razorpayOrderId;

    @Column(nullable = true)
    private String razorpayPaymentId;

    @Column(nullable = false)
    private double amount;            // in paise

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

//    @Lob
//    @Basic(fetch = FetchType.LAZY)
//    @Column(name = "raw_payload", columnDefinition = "LONGTEXT")
//    private String rawPayload;
}
