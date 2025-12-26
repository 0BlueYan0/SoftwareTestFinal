package org.example.restaurant.model;

import org.example.restaurant.exception.RestaurantNotFoundException;
import org.example.restaurant.exception.ValidationException;
import org.example.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    // Restaurant tests
    @Test
    @DisplayName("Restaurant - hasCuisineType with primary type")
    void restaurant_HasCuisineTypePrimary() {
        Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
        assertTrue(r.hasCuisineType(CuisineType.JAPANESE));
    }

    @Test
    @DisplayName("Restaurant - hasCuisineType with additional type")
    void restaurant_HasCuisineTypeAdditional() {
        Restaurant r = new Restaurant("1", "Test");
        r.addCuisineType(CuisineType.SEAFOOD);
        assertTrue(r.hasCuisineType(CuisineType.SEAFOOD));
    }

    @Test
    @DisplayName("Restaurant - hasCuisineType null returns false")
    void restaurant_HasCuisineTypeNull() {
        Restaurant r = new Restaurant("1", "Test");
        assertFalse(r.hasCuisineType(null));
    }

    @Test
    @DisplayName("Restaurant - calculateAverageRating")
    void restaurant_CalculateAverageRating() {
        Restaurant r = new Restaurant("1", "Test");
        r.addReview(new Review("1", "1", 4, "Good"));
        r.addReview(new Review("2", "1", 5, "Great"));
        assertEquals(4.5, r.calculateAverageRating());
    }

    @Test
    @DisplayName("Restaurant - calculateMenuAveragePrice")
    void restaurant_CalculateMenuAveragePrice() {
        Restaurant r = new Restaurant("1", "Test");
        MenuItem item1 = new MenuItem("1", "Item1", 100);
        item1.setAvailable(true);
        MenuItem item2 = new MenuItem("2", "Item2", 200);
        item2.setAvailable(true);
        r.addMenuItem(item1);
        r.addMenuItem(item2);
        assertEquals(150.0, r.calculateMenuAveragePrice());
    }

    @Test
    @DisplayName("Restaurant - matchesKeyword in name")
    void restaurant_MatchesKeywordInName() {
        Restaurant r = new Restaurant("1", "Tokyo Sushi");
        assertTrue(r.matchesKeyword("Tokyo"));
    }

    @Test
    @DisplayName("Restaurant - matchesKeyword in city")
    void restaurant_MatchesKeywordInCity() {
        Restaurant r = new Restaurant("1", "Test");
        r.setLocation(new Location(25.0, 121.0, "Address", "台北市"));
        assertTrue(r.matchesKeyword("台北"));
    }

    @Test
    @DisplayName("Restaurant - matchesKeyword null returns true")
    void restaurant_MatchesKeywordNull() {
        Restaurant r = new Restaurant("1", "Test");
        assertTrue(r.matchesKeyword(null));
    }

    // Location tests
    @Test
    @DisplayName("Location - isValid returns true for valid coords")
    void location_IsValidTrue() {
        Location loc = new Location(25.0, 121.0);
        assertTrue(loc.isValid());
    }

    @Test
    @DisplayName("Location - isValid returns false for invalid latitude")
    void location_IsValidFalseLatitude() {
        Location loc = new Location(91.0, 121.0);
        assertFalse(loc.isValid());
    }

    @Test
    @DisplayName("Location - isValid returns false for invalid longitude")
    void location_IsValidFalseLongitude() {
        Location loc = new Location(25.0, 181.0);
        assertFalse(loc.isValid());
    }

    @Test
    @DisplayName("Location - getFullAddress constructs correctly")
    void location_GetFullAddress() {
        Location loc = new Location(25.0, 121.0, "信義路100號", "台北市");
        loc.setPostalCode("106");
        loc.setDistrict("大安區");
        String fullAddress = loc.getFullAddress();
        assertTrue(fullAddress.contains("106"));
        assertTrue(fullAddress.contains("台北市"));
    }

    // MenuItem tests
    @Test
    @DisplayName("MenuItem - matchesDietaryRestrictions all true")
    void menuItem_MatchesDietaryRestrictionsAllTrue() {
        MenuItem item = new MenuItem("1", "Salad", 150);
        item.setVegetarian(true);
        item.setVegan(true);
        item.setGlutenFree(true);
        assertTrue(item.matchesDietaryRestrictions(true, true, true));
    }

    @Test
    @DisplayName("MenuItem - matchesDietaryRestrictions fails when not vegan")
    void menuItem_MatchesDietaryRestrictionsFailsNotVegan() {
        MenuItem item = new MenuItem("1", "Cheese", 150);
        item.setVegetarian(true);
        item.setVegan(false);
        assertFalse(item.matchesDietaryRestrictions(false, true, false));
    }

    @Test
    @DisplayName("MenuItem - isInPriceRange true")
    void menuItem_IsInPriceRangeTrue() {
        MenuItem item = new MenuItem("1", "Pizza", 350);
        assertTrue(item.isInPriceRange(200, 500));
    }

    @Test
    @DisplayName("MenuItem - isInPriceRange invalid range")
    void menuItem_IsInPriceRangeInvalid() {
        MenuItem item = new MenuItem("1", "Pizza", 350);
        assertFalse(item.isInPriceRange(500, 200));
    }

    @Test
    @DisplayName("MenuItem - isInPriceRange negative values")
    void menuItem_IsInPriceRangeNegative() {
        MenuItem item = new MenuItem("1", "Pizza", 350);
        assertFalse(item.isInPriceRange(-100, 500));
    }

    // Review tests
    @Test
    @DisplayName("Review - isValid true for valid review")
    void review_IsValidTrue() {
        Review r = new Review("1", "r1", 4, "Good");
        assertTrue(r.isValid());
    }

    @Test
    @DisplayName("Review - isValid false for invalid rating")
    void review_IsValidFalseInvalidRating() {
        Review r = new Review("1", "r1", 6, "Good");
        assertFalse(r.isValid());
    }

    @Test
    @DisplayName("Review - getWeight increases with verification")
    void review_GetWeightVerified() {
        Review verified = new Review("1", "r1", 4, "Good");
        verified.setUserLevel(3);
        verified.setVerified(true);

        Review unverified = new Review("2", "r1", 4, "Good");
        unverified.setUserLevel(3);
        unverified.setVerified(false);

        assertTrue(verified.getWeight() > unverified.getWeight());
    }

    @Test
    @DisplayName("Review - isRecent true for new review")
    void review_IsRecentTrue() {
        Review r = new Review("1", "r1", 4, "Good");
        r.setCreatedAt(LocalDateTime.now().minusDays(30));
        assertTrue(r.isRecent());
    }

    @Test
    @DisplayName("Review - isRecent false for old review")
    void review_IsRecentFalse() {
        Review r = new Review("1", "r1", 4, "Good");
        r.setCreatedAt(LocalDateTime.now().minusMonths(7));
        assertFalse(r.isRecent());
    }

    // BusinessHours tests
    @Test
    @DisplayName("BusinessHours - TimeSlot contains overnight")
    void businessHours_TimeSlotContainsOvernight() {
        BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(
                LocalTime.of(22, 0), LocalTime.of(2, 0));
        assertTrue(slot.contains(LocalTime.of(23, 0)));
        assertTrue(slot.contains(LocalTime.of(1, 0)));
        assertFalse(slot.contains(LocalTime.of(12, 0)));
    }

    @Test
    @DisplayName("BusinessHours - TimeSlot contains null returns false")
    void businessHours_TimeSlotContainsNull() {
        BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(
                LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertFalse(slot.contains(null));
    }

    // CuisineType tests
    @Test
    @DisplayName("CuisineType - fromDisplayName finds type")
    void cuisineType_FromDisplayNameFinds() {
        assertEquals(CuisineType.JAPANESE, CuisineType.fromDisplayName("日式料理"));
    }

    @Test
    @DisplayName("CuisineType - fromDisplayName null returns null")
    void cuisineType_FromDisplayNameNull() {
        assertNull(CuisineType.fromDisplayName(null));
    }

    @Test
    @DisplayName("CuisineType - fromDisplayName unknown returns OTHER")
    void cuisineType_FromDisplayNameUnknown() {
        assertEquals(CuisineType.OTHER, CuisineType.fromDisplayName("Unknown Cuisine"));
    }

    // SearchCriteria tests
    @Test
    @DisplayName("SearchCriteria - builder pattern")
    void searchCriteria_BuilderPattern() {
        SearchCriteria criteria = new SearchCriteria()
                .keyword("sushi")
                .city("台北")
                .cuisineType(CuisineType.JAPANESE)
                .minRating(4.0)
                .limit(10);

        assertEquals("sushi", criteria.getKeyword());
        assertEquals("台北", criteria.getCity());
        assertEquals(CuisineType.JAPANESE, criteria.getCuisineType());
        assertEquals(4.0, criteria.getMinRating());
        assertEquals(10, criteria.getLimit());
    }

    @Test
    @DisplayName("SearchCriteria - hasLocationFilter")
    void searchCriteria_HasLocationFilter() {
        SearchCriteria criteria = new SearchCriteria()
                .nearLocation(25.0, 121.0, 5.0);
        assertTrue(criteria.hasLocationFilter());
    }

    @Test
    @DisplayName("SearchCriteria - isEmpty true when empty")
    void searchCriteria_IsEmptyTrue() {
        SearchCriteria criteria = new SearchCriteria();
        assertTrue(criteria.isEmpty());
    }

    @Test
    @DisplayName("SearchCriteria - isEmpty false with keyword")
    void searchCriteria_IsEmptyFalse() {
        SearchCriteria criteria = new SearchCriteria().keyword("test");
        assertFalse(criteria.isEmpty());
    }

    // UserPreferences tests
    @Test
    @DisplayName("UserPreferences - calculatePreferenceScore with favorites")
    void userPreferences_CalculatePreferenceScore() {
        UserPreferences prefs = new UserPreferences();
        prefs.addFavoriteCuisine(CuisineType.JAPANESE);
        prefs.setMaxPriceLevel(3);
        prefs.setMinAcceptableRating(3.0);

        Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
        r.setPriceLevel(2);
        r.addReview(new Review("1", "1", 4, "Good"));

        double score = prefs.calculatePreferenceScore(r);
        assertTrue(score > 50); // Should be higher than base score
    }

    @Test
    @DisplayName("UserPreferences - addFavoriteCuisine removes from disliked")
    void userPreferences_AddFavoriteRemovesDisliked() {
        UserPreferences prefs = new UserPreferences();
        prefs.addDislikedCuisine(CuisineType.JAPANESE);
        prefs.addFavoriteCuisine(CuisineType.JAPANESE);

        assertTrue(prefs.likesCuisine(CuisineType.JAPANESE));
        assertFalse(prefs.dislikesCuisine(CuisineType.JAPANESE));
    }

    // Repository tests
    @Test
    @DisplayName("Repository - save and find")
    void repository_SaveAndFind() {
        RestaurantRepository repo = new RestaurantRepository();
        Restaurant r = new Restaurant("1", "Test");
        repo.save(r);

        assertTrue(repo.findById("1").isPresent());
        assertEquals("Test", repo.findById("1").get().getName());
    }

    @Test
    @DisplayName("Repository - getById throws when not found")
    void repository_GetByIdThrows() {
        RestaurantRepository repo = new RestaurantRepository();
        assertThrows(RestaurantNotFoundException.class, () -> repo.getById("nonexistent"));
    }

    @Test
    @DisplayName("Repository - count")
    void repository_Count() {
        RestaurantRepository repo = new RestaurantRepository();
        repo.save(new Restaurant("1", "Test1"));
        repo.save(new Restaurant("2", "Test2"));
        assertEquals(2, repo.count());
    }

    // Exception tests
    @Test
    @DisplayName("ValidationException - getField returns field")
    void validationException_GetField() {
        ValidationException ex = new ValidationException("Error", "fieldName");
        assertEquals("fieldName", ex.getField());
    }

    @Test
    @DisplayName("RestaurantNotFoundException - getRestaurantId returns id")
    void restaurantNotFoundException_GetRestaurantId() {
        RestaurantNotFoundException ex = new RestaurantNotFoundException("Not found", "123");
        assertEquals("123", ex.getRestaurantId());
    }
}
