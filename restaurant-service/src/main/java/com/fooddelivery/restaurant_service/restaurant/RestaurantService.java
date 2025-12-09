package com.fooddelivery.restaurant_service.restaurant;

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

    private UUID getCurrentOwnerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // For now, we only have email in principal. You can later map userId via another call if needed.
        // For demo, we'll just simulate ownerId with a random UUID or store email string instead.
        // Better: store ownerId as String email, but keeping UUID as placeholder.
        // To keep it consistent, let's just use a dummy UUID here in demo.
        // In a real setup, you might include userId as claim in JWT.
        return UUID.nameUUIDFromBytes(auth.getName().getBytes());
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
        return toResponse(restaurant);
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
}
