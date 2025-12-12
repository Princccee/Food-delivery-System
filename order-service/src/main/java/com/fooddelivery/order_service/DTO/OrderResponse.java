package com.fooddelivery.order_service.DTO;

import com.fooddelivery.order_service.order.OrderStatus;
import com.fooddelivery.order_service.order.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private UUID customerId;
    private UUID restaurantId;
    private List<OrderItemResponse> items;
    private Double totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String deliveryAddress;
    private Instant createdAt;
    private Instant updatedAt;
}
