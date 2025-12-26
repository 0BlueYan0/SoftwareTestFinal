package org.example.restaurant.service;

import org.example.restaurant.model.Restaurant;
import org.example.restaurant.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RatingServiceTest {

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService();
    }

    private Restaurant createRestaurantWithReviews(int... ratings) {
        Restaurant restaurant = new Restaurant("1", "Test Restaurant");
        for (int i = 0; i < ratings.length; i++) {
            Review review = new Review("r" + i, "1", ratings[i], "Comment");
            review.setUserLevel(3);
            review.setCreatedAt(LocalDateTime.now().minusDays(i));
            restaurant.addReview(review);
        }
        return restaurant;
    }

    // calculateAverageRating tests
    @Test
    @DisplayName("Calculate average rating - null restaurant returns 0")
    void calculateAverageRating_NullRestaurant_ReturnsZero() {
        assertEquals(0.0, ratingService.calculateAverageRating(null));
    }

    @Test
    @DisplayName("Calculate average rating - no reviews returns 0")
    void calculateAverageRating_NoReviews_ReturnsZero() {
        Restaurant restaurant = new Restaurant("1", "Test");
        assertEquals(0.0, ratingService.calculateAverageRating(restaurant));
    }

    @Test
    @DisplayName("Calculate average rating - single review")
    void calculateAverageRating_SingleReview_ReturnsRating() {
        Restaurant restaurant = createRestaurantWithReviews(4);
        assertEquals(4.0, ratingService.calculateAverageRating(restaurant));
    }

    @Test
    @DisplayName("Calculate average rating - multiple reviews")
    void calculateAverageRating_MultipleReviews_ReturnsAverage() {
        Restaurant restaurant = createRestaurantWithReviews(4, 5, 3, 4, 4);
        assertEquals(4.0, ratingService.calculateAverageRating(restaurant));
    }

    @Test
    @DisplayName("Calculate average rating - ignores invalid ratings")
    void calculateAverageRating_IgnoresInvalidRatings() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.addReview(new Review("1", "1", 4, "Good"));
        Review invalidReview = new Review("2", "1", 0, "Invalid");
        restaurant.addReview(invalidReview);
        assertEquals(4.0, ratingService.calculateAverageRating(restaurant));
    }

    // calculateWeightedRating tests
    @Test
    @DisplayName("Calculate weighted rating - null restaurant returns 0")
    void calculateWeightedRating_NullRestaurant_ReturnsZero() {
        assertEquals(0.0, ratingService.calculateWeightedRating(null));
    }

    @Test
    @DisplayName("Calculate weighted rating - few reviews uses average")
    void calculateWeightedRating_FewReviews_UsesAverage() {
        Restaurant restaurant = createRestaurantWithReviews(4, 5, 4);
        double avg = ratingService.calculateAverageRating(restaurant);
        double weighted = ratingService.calculateWeightedRating(restaurant);
        assertEquals(avg, weighted);
    }

    @Test
    @DisplayName("Calculate weighted rating - many reviews uses weighted")
    void calculateWeightedRating_ManyReviews_UsesWeighted() {
        Restaurant restaurant = createRestaurantWithReviews(4, 5, 4, 5, 4, 5, 4, 5);
        double weighted = ratingService.calculateWeightedRating(restaurant);
        assertTrue(weighted > 0);
        assertTrue(weighted <= 5);
    }

    // filterByRatingRange tests
    @Test
    @DisplayName("Filter by rating range - null list returns empty")
    void filterByRatingRange_NullList_ReturnsEmpty() {
        List<Restaurant> result = ratingService.filterByRatingRange(null, 3.0, 5.0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Filter by rating range - filters correctly")
    void filterByRatingRange_FiltersCorrectly() {
        Restaurant r1 = createRestaurantWithReviews(5, 5, 5);
        Restaurant r2 = createRestaurantWithReviews(3, 3, 3);
        Restaurant r3 = createRestaurantWithReviews(2, 2, 2);
        List<Restaurant> list = Arrays.asList(r1, r2, r3);

        List<Restaurant> result = ratingService.filterByRatingRange(list, 3.0, 5.0);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Filter by rating range - null min includes all below")
    void filterByRatingRange_NullMin_IncludesAllBelow() {
        Restaurant r1 = createRestaurantWithReviews(5);
        Restaurant r2 = createRestaurantWithReviews(1);
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = ratingService.filterByRatingRange(list, null, 5.0);
        assertEquals(2, result.size());
    }

    // getTopRatedRestaurants tests
    @Test
    @DisplayName("Get top rated - null list returns empty")
    void getTopRatedRestaurants_NullList_ReturnsEmpty() {
        assertTrue(ratingService.getTopRatedRestaurants(null, 5).isEmpty());
    }

    @Test
    @DisplayName("Get top rated - returns sorted by rating")
    void getTopRatedRestaurants_ReturnsSortedByRating() {
        Restaurant r1 = createRestaurantWithReviews(3, 3, 3, 3, 3, 3);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5);
        r2.setId("2");
        Restaurant r3 = createRestaurantWithReviews(4, 4, 4, 4, 4, 4);
        r3.setId("3");
        List<Restaurant> list = Arrays.asList(r1, r2, r3);

        List<Restaurant> result = ratingService.getTopRatedRestaurants(list, 3);
        assertEquals("2", result.get(0).getId());
    }

    @Test
    @DisplayName("Get top rated - respects limit")
    void getTopRatedRestaurants_RespectsLimit() {
        Restaurant r1 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5);
        Restaurant r2 = createRestaurantWithReviews(4, 4, 4, 4, 4, 4);
        Restaurant r3 = createRestaurantWithReviews(3, 3, 3, 3, 3, 3);
        List<Restaurant> list = Arrays.asList(r1, r2, r3);

        List<Restaurant> result = ratingService.getTopRatedRestaurants(list, 2);
        assertEquals(2, result.size());
    }

    // filterByMinReviewCount tests
    @Test
    @DisplayName("Filter by min review count - filters correctly")
    void filterByMinReviewCount_FiltersCorrectly() {
        Restaurant r1 = createRestaurantWithReviews(5, 5, 5, 5, 5);
        Restaurant r2 = createRestaurantWithReviews(5, 5);
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = ratingService.filterByMinReviewCount(list, 3);
        assertEquals(1, result.size());
    }

    // getRatingDistribution tests
    @Test
    @DisplayName("Get rating distribution - null restaurant returns zeros")
    void getRatingDistribution_NullRestaurant_ReturnsZeros() {
        int[] dist = ratingService.getRatingDistribution(null);
        assertEquals(5, dist.length);
        for (int i : dist) {
            assertEquals(0, i);
        }
    }

    @Test
    @DisplayName("Get rating distribution - counts correctly")
    void getRatingDistribution_CountsCorrectly() {
        Restaurant restaurant = createRestaurantWithReviews(5, 5, 4, 4, 4, 3);
        int[] dist = ratingService.getRatingDistribution(restaurant);
        assertEquals(1, dist[2]); // 3-star
        assertEquals(3, dist[3]); // 4-star
        assertEquals(2, dist[4]); // 5-star
    }

    // calculateRatingTrend tests
    @Test
    @DisplayName("Calculate rating trend - insufficient data")
    void calculateRatingTrend_InsufficientData_ReturnsMessage() {
        Restaurant restaurant = createRestaurantWithReviews(5, 5, 5);
        assertEquals("INSUFFICIENT_DATA", ratingService.calculateRatingTrend(restaurant));
    }

    // sortByRating tests
    @Test
    @DisplayName("Sort by rating - ascending")
    void sortByRating_Ascending_SortsCorrectly() {
        Restaurant r1 = createRestaurantWithReviews(5);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithReviews(3);
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = ratingService.sortByRating(list, true);
        assertEquals("2", result.get(0).getId());
    }

    @Test
    @DisplayName("Sort by rating - descending")
    void sortByRating_Descending_SortsCorrectly() {
        Restaurant r1 = createRestaurantWithReviews(3);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithReviews(5);
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = ratingService.sortByRating(list, false);
        assertEquals("2", result.get(0).getId());
    }

    // hasGoodRating tests
    @Test
    @DisplayName("Has good rating - returns true for high rating")
    void hasGoodRating_HighRating_ReturnsTrue() {
        Restaurant restaurant = createRestaurantWithReviews(5, 5, 5, 5, 5);
        assertTrue(ratingService.hasGoodRating(restaurant, 3));
    }

    @Test
    @DisplayName("Has good rating - returns false for low rating")
    void hasGoodRating_LowRating_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithReviews(2, 2, 2, 2, 2);
        assertFalse(ratingService.hasGoodRating(restaurant, 3));
    }

    @Test
    @DisplayName("Has good rating - returns false for insufficient reviews")
    void hasGoodRating_InsufficientReviews_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithReviews(5);
        assertFalse(ratingService.hasGoodRating(restaurant, 3));
    }
}
