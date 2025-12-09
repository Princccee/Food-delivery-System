package com.fooddelivery.restaurant_service.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RestaurantResponse {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String cuisineType;
    private Double rating;
    private boolean open;
}
