package com.fooddelivery.order_service.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlaceOrderRequest {

    @NotNull
    private UUID restaurantId;

    @NotEmpty
    private List<OrderItemRequest> items;

    private String deliveryAddress; // optional
}
