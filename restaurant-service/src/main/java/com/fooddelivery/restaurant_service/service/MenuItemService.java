package com.fooddelivery.restaurant_service.service;

import com.fooddelivery.restaurant_service.DTO.MenuItemRequest;
import com.fooddelivery.restaurant_service.Repository.MenuItemRepository;
import com.fooddelivery.restaurant_service.Repository.RestaurantRepository;
import com.fooddelivery.restaurant_service.restaurant.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @CacheEvict(value = "menus", key = "#restaurantId")
    public MenuItem createMenuItem(UUID restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to modify this restaurant's menu.");
        }

        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .available(request.isAvailable())
                .category(request.getCategory())
                .build();

        return menuItemRepository.save(item);
    }

    @Cacheable(value = "menus", key = "#restaurantId")
    public List<MenuItem> getMenuForRestaurant(UUID restaurantId) {
        return menuItemRepository.findByRestaurant_Id(restaurantId);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getCredentials().toString());
    }
}
