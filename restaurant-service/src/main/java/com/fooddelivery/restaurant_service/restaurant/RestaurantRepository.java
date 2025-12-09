package com.fooddelivery.restaurant_service.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByCityIgnoreCase(String city);

    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);

    List<Restaurant> findByOwnerId(UUID ownerId);
}
