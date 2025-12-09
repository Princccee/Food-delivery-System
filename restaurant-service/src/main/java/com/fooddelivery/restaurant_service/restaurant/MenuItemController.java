package com.fooddelivery.restaurant_service.restaurant;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItem> addMenuItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return ResponseEntity.ok(menuItemService.createMenuItem(restaurantId, request));
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItem>> getMenuForRestaurant(
            @PathVariable UUID restaurantId
    ) {
        return ResponseEntity.ok(menuItemService.getMenuForRestaurant(restaurantId));
    }
}
