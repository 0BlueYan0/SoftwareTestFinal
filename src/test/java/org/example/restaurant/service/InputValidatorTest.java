package org.example.restaurant.service;

import org.example.restaurant.exception.ValidationException;
import org.example.restaurant.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    private InputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }

    // Restaurant validation tests
    @Test
    @DisplayName("Validate restaurant - null restaurant throws exception")
    void validateRestaurant_NullRestaurant_ThrowsException() {
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(null));
    }

    @Test
    @DisplayName("Validate restaurant - null ID throws exception")
    void validateRestaurant_NullId_ThrowsException() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test");
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - empty ID throws exception")
    void validateRestaurant_EmptyId_ThrowsException() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("  ");
        restaurant.setName("Test");
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - ID too long throws exception")
    void validateRestaurant_IdTooLong_ThrowsException() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("a".repeat(51));
        restaurant.setName("Test");
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - null name throws exception")
    void validateRestaurant_NullName_ThrowsException() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("1");
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - name too short throws exception")
    void validateRestaurant_NameTooShort_ThrowsException() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("1");
        restaurant.setName("A");
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - name too long throws exception")
    void validateRestaurant_NameTooLong_ThrowsException() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("1");
        restaurant.setName("a".repeat(101));
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - invalid price level throws exception")
    void validateRestaurant_InvalidPriceLevel_ThrowsException() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setPriceLevel(5);
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - negative average price throws exception")
    void validateRestaurant_NegativeAveragePrice_ThrowsException() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setAveragePrice(-100);
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - valid phone number passes")
    void validateRestaurant_ValidPhoneNumber_Passes() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setPhoneNumber("02-1234-5678");
        assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - invalid phone number throws exception")
    void validateRestaurant_InvalidPhoneNumber_ThrowsException() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setPhoneNumber("abc");
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - valid website passes")
    void validateRestaurant_ValidWebsite_Passes() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setWebsite("https://example.com");
        assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - invalid website throws exception")
    void validateRestaurant_InvalidWebsite_ThrowsException() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setWebsite("not-a-url");
        assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
    }

    @Test
    @DisplayName("Validate restaurant - valid restaurant passes")
    void validateRestaurant_ValidRestaurant_Passes() {
        Restaurant restaurant = new Restaurant("1", "Valid Restaurant");
        restaurant.setPriceLevel(2);
        restaurant.setAveragePrice(300);
        assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
    }

    // Location validation tests
    @Test
    @DisplayName("Validate location - null location throws exception")
    void validateLocation_NullLocation_ThrowsException() {
        assertThrows(ValidationException.class, () -> validator.validateLocation(null));
    }

    @Test
    @DisplayName("Validate location - invalid latitude throws exception")
    void validateLocation_InvalidLatitude_ThrowsException() {
        Location location = new Location(91, 0);
        assertThrows(ValidationException.class, () -> validator.validateLocation(location));
    }

    @Test
    @DisplayName("Validate location - invalid longitude throws exception")
    void validateLocation_InvalidLongitude_ThrowsException() {
        Location location = new Location(0, 181);
        assertThrows(ValidationException.class, () -> validator.validateLocation(location));
    }

    @Test
    @DisplayName("Validate location - city too long throws exception")
    void validateLocation_CityTooLong_ThrowsException() {
        Location location = new Location(25.0, 121.5);
        location.setCity("a".repeat(101));
        assertThrows(ValidationException.class, () -> validator.validateLocation(location));
    }

    @Test
    @DisplayName("Validate location - valid location passes")
    void validateLocation_ValidLocation_Passes() {
        Location location = new Location(25.0330, 121.5654, "台北市信義區", "台北市");
        assertDoesNotThrow(() -> validator.validateLocation(location));
    }

    // Review validation tests
    @Test
    @DisplayName("Validate review - null review throws exception")
    void validateReview_NullReview_ThrowsException() {
        assertThrows(ValidationException.class, () -> validator.validateReview(null));
    }

    @Test
    @DisplayName("Validate review - invalid rating throws exception")
    void validateReview_InvalidRating_ThrowsException() {
        Review review = new Review("1", "r1", 6, "Good");
        assertThrows(ValidationException.class, () -> validator.validateReview(review));
    }

    @Test
    @DisplayName("Validate review - rating below 1 throws exception")
    void validateReview_RatingBelowOne_ThrowsException() {
        Review review = new Review("1", "r1", 0, "Good");
        assertThrows(ValidationException.class, () -> validator.validateReview(review));
    }

    @Test
    @DisplayName("Validate review - comment too long throws exception")
    void validateReview_CommentTooLong_ThrowsException() {
        Review review = new Review("1", "r1", 4, "a".repeat(2001));
        assertThrows(ValidationException.class, () -> validator.validateReview(review));
    }

    @Test
    @DisplayName("Validate review - profanity in comment throws exception")
    void validateReview_ProfanityInComment_ThrowsException() {
        Review review = new Review("1", "r1", 4, "This is spam content");
        assertThrows(ValidationException.class, () -> validator.validateReview(review));
    }

    @Test
    @DisplayName("Validate review - valid review passes")
    void validateReview_ValidReview_Passes() {
        Review review = new Review("1", "r1", 4, "Great food!");
        assertDoesNotThrow(() -> validator.validateReview(review));
    }

    // SearchCriteria validation tests
    @Test
    @DisplayName("Validate search criteria - null criteria throws exception")
    void validateSearchCriteria_NullCriteria_ThrowsException() {
        assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(null));
    }

    @Test
    @DisplayName("Validate search criteria - keyword too long throws exception")
    void validateSearchCriteria_KeywordTooLong_ThrowsException() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("a".repeat(101));
        assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
    }

    @Test
    @DisplayName("Validate search criteria - invalid rating range throws exception")
    void validateSearchCriteria_InvalidRatingRange_ThrowsException() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setMinRating(4.0);
        criteria.setMaxRating(2.0);
        assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
    }

    @Test
    @DisplayName("Validate search criteria - invalid price level throws exception")
    void validateSearchCriteria_InvalidPriceLevel_ThrowsException() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setPriceLevel(5);
        assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
    }

    @Test
    @DisplayName("Validate search criteria - valid criteria passes")
    void validateSearchCriteria_ValidCriteria_Passes() {
        SearchCriteria criteria = new SearchCriteria()
                .keyword("pizza")
                .minRating(3.0)
                .maxRating(5.0);
        assertDoesNotThrow(() -> validator.validateSearchCriteria(criteria));
    }

    // MenuItem validation tests
    @Test
    @DisplayName("Validate menu item - null item throws exception")
    void validateMenuItem_NullItem_ThrowsException() {
        assertThrows(ValidationException.class, () -> validator.validateMenuItem(null));
    }

    @Test
    @DisplayName("Validate menu item - negative price throws exception")
    void validateMenuItem_NegativePrice_ThrowsException() {
        MenuItem item = new MenuItem("1", "Pizza", -50);
        assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
    }

    @Test
    @DisplayName("Validate menu item - price too high throws exception")
    void validateMenuItem_PriceTooHigh_ThrowsException() {
        MenuItem item = new MenuItem("1", "Pizza", 200000);
        assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
    }

    @Test
    @DisplayName("Validate menu item - valid item passes")
    void validateMenuItem_ValidItem_Passes() {
        MenuItem item = new MenuItem("1", "Pizza", 350);
        assertDoesNotThrow(() -> validator.validateMenuItem(item));
    }

    // BusinessHours validation tests
    @Test
    @DisplayName("Validate business hours - null hours throws exception")
    void validateBusinessHours_NullHours_ThrowsException() {
        assertThrows(ValidationException.class, () -> validator.validateBusinessHours(null));
    }

    @Test
    @DisplayName("Validate business hours - valid hours passes")
    void validateBusinessHours_ValidHours_Passes() {
        BusinessHours hours = new BusinessHours();
        hours.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertDoesNotThrow(() -> validator.validateBusinessHours(hours));
    }
}
