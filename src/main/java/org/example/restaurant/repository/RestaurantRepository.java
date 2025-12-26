package org.example.restaurant.repository;

import org.example.restaurant.model.Restaurant;
import org.example.restaurant.exception.RestaurantNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository for restaurants.
 */
public class RestaurantRepository {
    private final Map<String, Restaurant> restaurants;

    public RestaurantRepository() {
        this.restaurants = new HashMap<>();
    }

    public Restaurant save(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        if (restaurant.getId() == null || restaurant.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant ID cannot be null or empty");
        }
        restaurants.put(restaurant.getId(), restaurant);
        return restaurant;
    }

    public Optional<Restaurant> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(restaurants.get(id));
    }

    public Restaurant getById(String id) {
        return findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + id, id));
    }

    public List<Restaurant> findAll() {
        return new ArrayList<>(restaurants.values());
    }

    public List<Restaurant> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Restaurant> result = new ArrayList<>();
        String lowerName = name.toLowerCase();
        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getName() != null &&
                    restaurant.getName().toLowerCase().contains(lowerName)) {
                result.add(restaurant);
            }
        }
        return result;
    }

    public void delete(String id) {
        if (id != null) {
            restaurants.remove(id);
        }
    }

    public void deleteAll() {
        restaurants.clear();
    }

    public boolean exists(String id) {
        return id != null && restaurants.containsKey(id);
    }

    public long count() {
        return restaurants.size();
    }
}
