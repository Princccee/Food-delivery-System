package com.fooddelivery.restaurant_service.service;

import com.fooddelivery.restaurant_service.DTO.MenuItemResponse;
import com.fooddelivery.restaurant_service.DTO.RestaurantRequest;
import com.fooddelivery.restaurant_service.DTO.RestaurantResponse;
import com.fooddelivery.restaurant_service.Repository.MenuItemRepository;
import com.fooddelivery.restaurant_service.Repository.RestaurantRepository;
import com.fooddelivery.restaurant_service.restaurant.MenuItem;
import com.fooddelivery.restaurant_service.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    private UUID getCurrentOwnerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getCredentials().toString());
    }

    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .isOpen(request.isOpen())
                .rating(0.0)
                .ownerId(getCurrentOwnerId())
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponse(saved);
    }

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RestaurantResponse getRestaurant(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        List<MenuItemResponse> menuItems = menuItemRepository.findByRestaurant_Id(id).stream()
                .map(this::toMenuItemResponse)
                .toList();

        RestaurantResponse resp = toResponse(restaurant);
        resp.setMenuItems(menuItems);
        return resp;
    }

    private RestaurantResponse toResponse(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .city(r.getCity())
                .cuisineType(r.getCuisineType())
                .rating(r.getRating())
                .open(r.isOpen())
                .build();
    }

    private MenuItemResponse toMenuItemResponse(MenuItem m) {
        return MenuItemResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .category(m.getCategory())
                .available(m.isAvailable())
                .build();
    }
}
