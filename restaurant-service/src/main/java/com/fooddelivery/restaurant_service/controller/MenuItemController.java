package com.fooddelivery.restaurant_service.controller;

import com.fooddelivery.restaurant_service.restaurant.MenuItem;
import com.fooddelivery.restaurant_service.DTO.MenuItemRequest;
import com.fooddelivery.restaurant_service.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<MenuItem> addMenuItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return ResponseEntity.ok(menuItemService.createMenuItem(restaurantId, request));
    }

    @GetMapping("/{restaurantId}/")
    public ResponseEntity<List<MenuItem>> getMenuForRestaurant(
            @PathVariable UUID restaurantId
    ) {
        return ResponseEntity.ok(menuItemService.getMenuForRestaurant(restaurantId));
    }
}
