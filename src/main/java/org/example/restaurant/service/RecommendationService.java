package org.example.restaurant.service;

import org.example.restaurant.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for restaurant recommendations.
 */
public class RecommendationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private final RatingService ratingService;
    private final PriceAnalyzer priceAnalyzer;

    public RecommendationService() {
        this.ratingService = new RatingService();
        this.priceAnalyzer = new PriceAnalyzer();
    }

    public RecommendationService(RatingService ratingService, PriceAnalyzer priceAnalyzer) {
        this.ratingService = ratingService;
        this.priceAnalyzer = priceAnalyzer;
    }

    /**
     * Recommend restaurants based on user preferences.
     * v(G) = ~15
     */
    public List<Restaurant> recommendByPreferences(UserPreferences prefs,
            List<Restaurant> restaurants) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (prefs == null) {
            return getPopularRestaurants(restaurants, 10);
        }

        List<ScoredRestaurant> scored = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            if (restaurant == null || !restaurant.isActive()) {
                continue;
            }

            double score = calculateMatchScore(restaurant, prefs);

            if (score > 0) {
                scored.add(new ScoredRestaurant(restaurant, score));
            }
        }

        // Sort by score descending
        scored.sort((a, b) -> Double.compare(b.score, a.score));

        return scored.stream()
                .map(s -> s.restaurant)
                .collect(Collectors.toList());
    }

    /**
     * Calculate match score for a restaurant against user preferences.
     * v(G) = ~20
     */
    private double calculateMatchScore(Restaurant restaurant, UserPreferences prefs) {
        double score = 50.0; // Base score

        // Cuisine match
        CuisineType mainCuisine = restaurant.getCuisineType();
        if (mainCuisine != null) {
            if (prefs.likesCuisine(mainCuisine)) {
                score += 25;
            } else if (prefs.dislikesCuisine(mainCuisine)) {
                score -= 40;
            }
        }

        // Additional cuisines
        for (CuisineType type : restaurant.getAdditionalCuisineTypes()) {
            if (prefs.likesCuisine(type)) {
                score += 10;
            } else if (prefs.dislikesCuisine(type)) {
                score -= 15;
            }
        }

        // Price level check
        int priceLevel = priceAnalyzer.categorizePriceLevel(restaurant);
        if (priceLevel > 0) {
            if (priceLevel <= prefs.getMaxPriceLevel()) {
                score += 10;
            } else {
                score -= 25;
            }
        }

        // Rating check
        double rating = ratingService.calculateAverageRating(restaurant);
        if (rating >= prefs.getMinAcceptableRating()) {
            score += rating * 5;
        } else if (rating > 0) {
            score -= 20;
        }

        // Feature preferences
        if (prefs.isRequiresParking()) {
            if (restaurant.isHasParking()) {
                score += 15;
            } else {
                score -= 25;
            }
        }

        if (prefs.isPreferDelivery() && restaurant.isHasDelivery()) {
            score += 10;
        }

        if (prefs.isPreferTakeout() && restaurant.isHasTakeout()) {
            score += 10;
        }

        // Distance check (if location available)
        if (prefs.getUserLocation() != null && restaurant.getLocation() != null) {
            double distance = calculateDistance(prefs.getUserLocation(), restaurant.getLocation());
            if (distance <= prefs.getMaxDistanceKm()) {
                score += 15 - (distance / prefs.getMaxDistanceKm() * 10);
            } else {
                score -= 20;
            }
        }

        return Math.max(0, score);
    }

    /**
     * Recommend similar restaurants based on a reference restaurant.
     * v(G) = ~12
     */
    public List<Restaurant> recommendSimilar(Restaurant reference,
            List<Restaurant> candidates) {
        if (reference == null || candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScoredRestaurant> scored = new ArrayList<>();

        for (Restaurant candidate : candidates) {
            if (candidate == null || !candidate.isActive()) {
                continue;
            }
            if (candidate.getId() != null && candidate.getId().equals(reference.getId())) {
                continue; // Skip the reference restaurant itself
            }

            double similarity = calculateSimilarity(reference, candidate);
            if (similarity > 0.2) { // Minimum similarity threshold
                scored.add(new ScoredRestaurant(candidate, similarity));
            }
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        return scored.stream()
                .limit(10)
                .map(s -> s.restaurant)
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity score between two restaurants.
     * v(G) = ~14
     */
    private double calculateSimilarity(Restaurant r1, Restaurant r2) {
        double score = 0.0;
        double maxScore = 0.0;

        // Cuisine type match (weight: 30)
        maxScore += 30;
        if (r1.getCuisineType() != null && r2.getCuisineType() != null) {
            if (r1.getCuisineType() == r2.getCuisineType()) {
                score += 30;
            } else if (r2.hasCuisineType(r1.getCuisineType())) {
                score += 15;
            }
        }

        // Price level match (weight: 20)
        maxScore += 20;
        int p1 = priceAnalyzer.categorizePriceLevel(r1);
        int p2 = priceAnalyzer.categorizePriceLevel(r2);
        if (p1 > 0 && p2 > 0) {
            int diff = Math.abs(p1 - p2);
            score += 20 - (diff * 7);
        }

        // Rating similarity (weight: 20)
        maxScore += 20;
        double rating1 = ratingService.calculateAverageRating(r1);
        double rating2 = ratingService.calculateAverageRating(r2);
        if (rating1 > 0 && rating2 > 0) {
            double ratingDiff = Math.abs(rating1 - rating2);
            score += Math.max(0, 20 - (ratingDiff * 5));
        }

        // Location proximity (weight: 15)
        maxScore += 15;
        if (r1.getLocation() != null && r2.getLocation() != null) {
            String city1 = r1.getLocation().getCity();
            String city2 = r2.getLocation().getCity();
            if (city1 != null && city1.equals(city2)) {
                score += 15;
            }
        }

        // Feature match (weight: 15)
        maxScore += 15;
        int featureMatch = 0;
        if (r1.isHasDelivery() == r2.isHasDelivery())
            featureMatch++;
        if (r1.isHasTakeout() == r2.isHasTakeout())
            featureMatch++;
        if (r1.isHasParking() == r2.isHasParking())
            featureMatch++;
        score += featureMatch * 5;

        return maxScore > 0 ? score / maxScore : 0;
    }

    /**
     * Get popular restaurants based on review count and ratings.
     * v(G) = ~8
     */
    public List<Restaurant> getPopularRestaurants(List<Restaurant> restaurants, int limit) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (limit <= 0) {
            limit = 10;
        }

        return restaurants.stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> r.getReviewCount() > 0)
                .sorted((r1, r2) -> {
                    // Calculate popularity score
                    double pop1 = calculatePopularityScore(r1);
                    double pop2 = calculatePopularityScore(r2);
                    return Double.compare(pop2, pop1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Calculate popularity score.
     * v(G) = ~6
     */
    private double calculatePopularityScore(Restaurant restaurant) {
        if (restaurant == null) {
            return 0;
        }

        double rating = ratingService.calculateAverageRating(restaurant);
        int reviewCount = restaurant.getReviewCount();

        // Bayesian average to handle restaurants with few reviews
        double avgRating = 3.5; // Platform average
        int minReviews = 5; // Minimum reviews for full weight

        double weightedRating = (reviewCount * rating + minReviews * avgRating)
                / (reviewCount + minReviews);

        // Combine with review count (log scale)
        double countFactor = Math.log10(reviewCount + 1) * 10;

        return weightedRating * 20 + countFactor;
    }

    /**
     * Find nearby restaurants.
     * v(G) = ~10
     */
    public List<Restaurant> findNearby(Location userLocation,
            List<Restaurant> restaurants,
            double radiusKm) {
        if (userLocation == null || restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (radiusKm <= 0) {
            radiusKm = 5.0; // Default 5km
        }

        final double radius = radiusKm;
        List<DistancedRestaurant> nearby = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            if (restaurant == null || !restaurant.isActive()) {
                continue;
            }
            if (restaurant.getLocation() == null) {
                continue;
            }

            double distance = calculateDistance(userLocation, restaurant.getLocation());
            if (distance <= radius) {
                nearby.add(new DistancedRestaurant(restaurant, distance));
            }
        }

        // Sort by distance
        nearby.sort(Comparator.comparingDouble(d -> d.distance));

        return nearby.stream()
                .map(d -> d.restaurant)
                .collect(Collectors.toList());
    }

    /**
     * Calculate distance between two locations using Haversine formula.
     * v(G) = ~4
     */
    public double calculateDistance(Location from, Location to) {
        if (from == null || to == null) {
            return Double.MAX_VALUE;
        }

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double deltaLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double deltaLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                        * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Get restaurants sorted by distance.
     * v(G) = ~4
     */
    public List<Restaurant> sortByDistance(Location userLocation,
            List<Restaurant> restaurants) {
        if (userLocation == null || restaurants == null) {
            return new ArrayList<>();
        }

        List<DistancedRestaurant> distanced = new ArrayList<>();

        for (Restaurant r : restaurants) {
            if (r != null && r.getLocation() != null) {
                double distance = calculateDistance(userLocation, r.getLocation());
                distanced.add(new DistancedRestaurant(r, distance));
            }
        }

        distanced.sort(Comparator.comparingDouble(d -> d.distance));

        return distanced.stream()
                .map(d -> d.restaurant)
                .collect(Collectors.toList());
    }

    /**
     * Get top picks combining multiple factors.
     * v(G) = ~8
     */
    public List<Restaurant> getTopPicks(List<Restaurant> restaurants,
            Location userLocation,
            int limit) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (limit <= 0) {
            limit = 5;
        }

        List<ScoredRestaurant> scored = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            if (restaurant == null || !restaurant.isActive()) {
                continue;
            }

            double score = 0;

            // Rating factor (0-50)
            double rating = ratingService.calculateAverageRating(restaurant);
            score += rating * 10;

            // Popularity factor (0-20)
            score += Math.min(20, Math.log10(restaurant.getReviewCount() + 1) * 10);

            // Distance factor (0-30, if location available)
            if (userLocation != null && restaurant.getLocation() != null) {
                double distance = calculateDistance(userLocation, restaurant.getLocation());
                if (distance <= 1) {
                    score += 30;
                } else if (distance <= 3) {
                    score += 20;
                } else if (distance <= 5) {
                    score += 10;
                }
            }

            scored.add(new ScoredRestaurant(restaurant, score));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        return scored.stream()
                .limit(limit)
                .map(s -> s.restaurant)
                .collect(Collectors.toList());
    }

    // Helper classes
    private static class ScoredRestaurant {
        Restaurant restaurant;
        double score;

        ScoredRestaurant(Restaurant restaurant, double score) {
            this.restaurant = restaurant;
            this.score = score;
        }
    }

    private static class DistancedRestaurant {
        Restaurant restaurant;
        double distance;

        DistancedRestaurant(Restaurant restaurant, double distance) {
            this.restaurant = restaurant;
            this.distance = distance;
        }
    }
}
