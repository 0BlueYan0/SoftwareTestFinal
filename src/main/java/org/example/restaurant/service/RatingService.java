package org.example.restaurant.service;

import org.example.restaurant.model.Location;
import org.example.restaurant.model.Restaurant;
import org.example.restaurant.model.Review;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for rating-related operations.
 * This class handles rating calculations with various algorithms.
 */
public class RatingService {

    private static final double DEFAULT_WEIGHT = 1.0;
    private static final int MIN_REVIEWS_FOR_WEIGHTED = 5;

    /**
     * Calculate simple average rating for a restaurant.
     * v(G) = ~6
     */
    public double calculateAverageRating(Restaurant restaurant) {
        if (restaurant == null) {
            return 0.0;
        }

        List<Review> reviews = restaurant.getReviews();
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        int validCount = 0;

        for (Review review : reviews) {
            if (review != null && review.getRating() >= 1 && review.getRating() <= 5) {
                sum += review.getRating();
                validCount++;
            }
        }

        if (validCount == 0) {
            return 0.0;
        }

        return Math.round((sum / validCount) * 10.0) / 10.0;
    }

    /**
     * Calculate weighted average rating based on reviewer level and verification.
     * v(G) = ~10
     */
    public double calculateWeightedRating(Restaurant restaurant) {
        if (restaurant == null) {
            return 0.0;
        }

        List<Review> reviews = restaurant.getReviews();
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        // Not enough reviews for weighted calculation
        if (reviews.size() < MIN_REVIEWS_FOR_WEIGHTED) {
            return calculateAverageRating(restaurant);
        }

        double weightedSum = 0;
        double totalWeight = 0;

        for (Review review : reviews) {
            if (review != null && review.getRating() >= 1 && review.getRating() <= 5) {
                double weight = calculateReviewWeight(review);
                weightedSum += review.getRating() * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0) {
            return 0.0;
        }

        return Math.round((weightedSum / totalWeight) * 10.0) / 10.0;
    }

    /**
     * Calculate weight for a single review.
     * v(G) = ~8
     */
    private double calculateReviewWeight(Review review) {
        if (review == null) {
            return DEFAULT_WEIGHT;
        }

        double weight = 1.0;

        // Factor 1: User level (1-5)
        int userLevel = review.getUserLevel();
        if (userLevel >= 1 && userLevel <= 5) {
            weight *= (0.5 + userLevel * 0.2); // 0.7 to 1.5
        }

        // Factor 2: Verification status
        if (review.isVerified()) {
            weight *= 1.3;
        }

        // Factor 3: Helpful count
        int helpfulCount = review.getHelpfulCount();
        if (helpfulCount > 20) {
            weight *= 1.4;
        } else if (helpfulCount > 10) {
            weight *= 1.2;
        } else if (helpfulCount > 5) {
            weight *= 1.1;
        }

        // Factor 4: Recency
        if (review.isRecent()) {
            weight *= 1.2;
        } else {
            weight *= 0.9;
        }

        // Cap the weight
        return Math.min(weight, 3.0);
    }

    /**
     * Filter restaurants by rating range.
     * v(G) = ~8
     */
    public List<Restaurant> filterByRatingRange(List<Restaurant> restaurants,
            Double minRating, Double maxRating) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        List<Restaurant> result = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            if (restaurant == null) {
                continue;
            }

            double rating = calculateAverageRating(restaurant);

            boolean meetsMin = (minRating == null) || (rating >= minRating);
            boolean meetsMax = (maxRating == null) || (rating <= maxRating);

            if (meetsMin && meetsMax) {
                result.add(restaurant);
            }
        }

        return result;
    }

    /**
     * Get top rated restaurants.
     * v(G) = ~6
     */
    public List<Restaurant> getTopRatedRestaurants(List<Restaurant> restaurants, int limit) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (limit <= 0) {
            limit = 10;
        }

        return restaurants.stream()
                .filter(r -> r != null)
                .filter(r -> r.getReviews() != null && !r.getReviews().isEmpty())
                .sorted((r1, r2) -> {
                    double rating1 = calculateWeightedRating(r1);
                    double rating2 = calculateWeightedRating(r2);
                    int ratingCompare = Double.compare(rating2, rating1);
                    if (ratingCompare != 0) {
                        return ratingCompare;
                    }
                    // Secondary sort by review count
                    return Integer.compare(r2.getReviewCount(), r1.getReviewCount());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get restaurants with minimum review count.
     * v(G) = ~4
     */
    public List<Restaurant> filterByMinReviewCount(List<Restaurant> restaurants, int minCount) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (minCount < 0) {
            minCount = 0;
        }

        final int threshold = minCount;
        return restaurants.stream()
                .filter(r -> r != null)
                .filter(r -> r.getReviewCount() >= threshold)
                .collect(Collectors.toList());
    }

    /**
     * Get rating distribution for a restaurant (count of 1-5 star ratings).
     * v(G) = ~6
     */
    public int[] getRatingDistribution(Restaurant restaurant) {
        int[] distribution = new int[5]; // Index 0-4 for ratings 1-5

        if (restaurant == null || restaurant.getReviews() == null) {
            return distribution;
        }

        for (Review review : restaurant.getReviews()) {
            if (review != null) {
                int rating = review.getRating();
                if (rating >= 1 && rating <= 5) {
                    distribution[rating - 1]++;
                }
            }
        }

        return distribution;
    }

    /**
     * Calculate rating trend (improving, declining, stable).
     * v(G) = ~10
     */
    public String calculateRatingTrend(Restaurant restaurant) {
        if (restaurant == null || restaurant.getReviews() == null) {
            return "UNKNOWN";
        }

        List<Review> reviews = restaurant.getReviews();
        if (reviews.size() < 10) {
            return "INSUFFICIENT_DATA";
        }

        // Sort by date
        List<Review> sortedReviews = reviews.stream()
                .filter(r -> r != null && r.getCreatedAt() != null)
                .sorted(Comparator.comparing(Review::getCreatedAt))
                .collect(Collectors.toList());

        if (sortedReviews.size() < 10) {
            return "INSUFFICIENT_DATA";
        }

        // Compare first half vs second half
        int midPoint = sortedReviews.size() / 2;

        double firstHalfAvg = calculateAverage(sortedReviews.subList(0, midPoint));
        double secondHalfAvg = calculateAverage(sortedReviews.subList(midPoint, sortedReviews.size()));

        double difference = secondHalfAvg - firstHalfAvg;

        if (difference > 0.3) {
            return "IMPROVING";
        } else if (difference < -0.3) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    private double calculateAverage(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        int count = 0;
        for (Review r : reviews) {
            if (r != null && r.getRating() >= 1 && r.getRating() <= 5) {
                sum += r.getRating();
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    /**
     * Sort restaurants by rating.
     * v(G) = ~4
     */
    public List<Restaurant> sortByRating(List<Restaurant> restaurants, boolean ascending) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        Comparator<Restaurant> comparator = (r1, r2) -> {
            double rating1 = r1 != null ? calculateAverageRating(r1) : 0;
            double rating2 = r2 != null ? calculateAverageRating(r2) : 0;
            return Double.compare(rating1, rating2);
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return restaurants.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Check if restaurant has good rating (4.0+) with sufficient reviews.
     * v(G) = ~5
     */
    public boolean hasGoodRating(Restaurant restaurant, int minReviewCount) {
        if (restaurant == null) {
            return false;
        }

        if (restaurant.getReviewCount() < minReviewCount) {
            return false;
        }

        double rating = calculateAverageRating(restaurant);
        return rating >= 4.0;
    }
}
