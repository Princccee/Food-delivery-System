package com.fooddelivery.restaurant_service.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Double price;

    private boolean available = true;

    private String category;
}
