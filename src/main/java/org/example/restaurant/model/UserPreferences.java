package org.example.restaurant.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents user preferences for restaurant recommendations.
 */
public class UserPreferences {
    private Set<CuisineType> favoriteCuisines;
    private Set<CuisineType> dislikedCuisines;
    private int maxPriceLevel; // 1-4
    private double minAcceptableRating;
    private boolean preferVegetarian;
    private boolean preferVegan;
    private boolean preferGlutenFree;
    private boolean requiresParking;
    private boolean preferDelivery;
    private boolean preferTakeout;
    private double maxDistanceKm;
    private Location userLocation;

    public UserPreferences() {
        this.favoriteCuisines = new HashSet<>();
        this.dislikedCuisines = new HashSet<>();
        this.maxPriceLevel = 4;
        this.minAcceptableRating = 0.0;
        this.maxDistanceKm = 10.0;
    }

    public Set<CuisineType> getFavoriteCuisines() {
        return favoriteCuisines;
    }

    public void setFavoriteCuisines(Set<CuisineType> favoriteCuisines) {
        this.favoriteCuisines = favoriteCuisines;
        // Mutual exclusion: Remove these from disliked
        if (this.dislikedCuisines != null && favoriteCuisines != null) {
            this.dislikedCuisines.removeAll(favoriteCuisines);
        }
    }

    public void addFavoriteCuisine(CuisineType type) {
        if (type != null) {
            favoriteCuisines.add(type);
            dislikedCuisines.remove(type);
        }
    }

    public Set<CuisineType> getDislikedCuisines() {
        return dislikedCuisines;
    }

    public void setDislikedCuisines(Set<CuisineType> dislikedCuisines) {
        this.dislikedCuisines = dislikedCuisines;
        // Mutual exclusion: Remove these from favorites
        if (this.favoriteCuisines != null && dislikedCuisines != null) {
            this.favoriteCuisines.removeAll(dislikedCuisines);
        }
    }

    public void addDislikedCuisine(CuisineType type) {
        if (type != null) {
            dislikedCuisines.add(type);
            favoriteCuisines.remove(type);
        }
    }

    public int getMaxPriceLevel() {
        return maxPriceLevel;
    }

    public void setMaxPriceLevel(int maxPriceLevel) {
        this.maxPriceLevel = Math.max(1, Math.min(4, maxPriceLevel));
    }

    public double getMinAcceptableRating() {
        return minAcceptableRating;
    }

    public void setMinAcceptableRating(double minAcceptableRating) {
        this.minAcceptableRating = Math.max(0, Math.min(5, minAcceptableRating));
    }

    public boolean isPreferVegetarian() {
        return preferVegetarian;
    }

    public void setPreferVegetarian(boolean preferVegetarian) {
        this.preferVegetarian = preferVegetarian;
    }

    public boolean isPreferVegan() {
        return preferVegan;
    }

    public void setPreferVegan(boolean preferVegan) {
        this.preferVegan = preferVegan;
    }

    public boolean isPreferGlutenFree() {
        return preferGlutenFree;
    }

    public void setPreferGlutenFree(boolean preferGlutenFree) {
        this.preferGlutenFree = preferGlutenFree;
    }

    public boolean isRequiresParking() {
        return requiresParking;
    }

    public void setRequiresParking(boolean requiresParking) {
        this.requiresParking = requiresParking;
    }

    public boolean isPreferDelivery() {
        return preferDelivery;
    }

    public void setPreferDelivery(boolean preferDelivery) {
        this.preferDelivery = preferDelivery;
    }

    public boolean isPreferTakeout() {
        return preferTakeout;
    }

    public void setPreferTakeout(boolean preferTakeout) {
        this.preferTakeout = preferTakeout;
    }

    public double getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(double maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public boolean likesCuisine(CuisineType type) {
        if (type == null) {
            return false;
        }
        return favoriteCuisines.contains(type);
    }

    public boolean dislikesCuisine(CuisineType type) {
        if (type == null) {
            return false;
        }
        return dislikedCuisines.contains(type);
    }

    public double calculatePreferenceScore(Restaurant restaurant) {
        if (restaurant == null) {
            return 0.0;
        }

        double score = 50.0; // Base score

        // Cuisine preference
        if (likesCuisine(restaurant.getCuisineType())) {
            score += 20;
        } else if (dislikesCuisine(restaurant.getCuisineType())) {
            score -= 30;
        }

        // Check additional cuisine types
        for (CuisineType type : restaurant.getAdditionalCuisineTypes()) {
            if (likesCuisine(type)) {
                score += 10;
            } else if (dislikesCuisine(type)) {
                score -= 15;
            }
        }

        // Price level
        if (restaurant.getPriceLevel() > 0 && restaurant.getPriceLevel() <= maxPriceLevel) {
            score += 10;
        } else if (restaurant.getPriceLevel() > maxPriceLevel) {
            score -= 20;
        }

        // Rating
        double rating = restaurant.calculateAverageRating();
        if (rating >= minAcceptableRating) {
            score += (rating / 5.0) * 15;
        } else {
            score -= 25;
        }

        // Features
        if (requiresParking && restaurant.isHasParking()) {
            score += 10;
        } else if (requiresParking && !restaurant.isHasParking()) {
            score -= 20;
        }

        if (preferDelivery && restaurant.isHasDelivery()) {
            score += 5;
        }

        if (preferTakeout && restaurant.isHasTakeout()) {
            score += 5;
        }

        return Math.max(0, Math.min(100, score));
    }
}
