package com.fooddelivery.order_service.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;
    private UUID menuItemId;
    private int quantity;
    private double price;
    private double lineTotal;
}
