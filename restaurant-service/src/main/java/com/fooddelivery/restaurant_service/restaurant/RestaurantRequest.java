package com.fooddelivery.restaurant_service.restaurant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    private String cuisineType;

    private boolean open;
}
