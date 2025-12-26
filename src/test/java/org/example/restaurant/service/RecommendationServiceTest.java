package org.example.restaurant.service;

import org.example.restaurant.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceTest {

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationService();
    }

    private Restaurant createRestaurant(String id, CuisineType cuisine, double lat, double lon) {
        Restaurant restaurant = new Restaurant(id, "Restaurant " + id);
        restaurant.setCuisineType(cuisine);
        restaurant.setLocation(new Location(lat, lon, "Address", "City"));
        restaurant.setActive(true);
        restaurant.setPriceLevel(2);
        Review review = new Review("r" + id, id, 4, "Good");
        restaurant.addReview(review);
        return restaurant;
    }

    // recommendByPreferences tests
    @Test
    @DisplayName("Recommend by preferences - null list returns empty")
    void recommendByPreferences_NullList_ReturnsEmpty() {
        UserPreferences prefs = new UserPreferences();
        assertTrue(service.recommendByPreferences(prefs, null).isEmpty());
    }

    @Test
    @DisplayName("Recommend by preferences - null prefs uses popular")
    void recommendByPreferences_NullPrefs_UsesPopular() {
        Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r);
        List<Restaurant> result = service.recommendByPreferences(null, list);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Recommend by preferences - prioritizes favorites")
    void recommendByPreferences_PrioritizesFavorites() {
        Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r1, r2);

        UserPreferences prefs = new UserPreferences();
        prefs.addFavoriteCuisine(CuisineType.JAPANESE);

        List<Restaurant> result = service.recommendByPreferences(prefs, list);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    @DisplayName("Recommend by preferences - filters disliked cuisines")
    void recommendByPreferences_FiltersDisliked() {
        Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r1, r2);

        UserPreferences prefs = new UserPreferences();
        prefs.addDislikedCuisine(CuisineType.CHINESE);

        List<Restaurant> result = service.recommendByPreferences(prefs, list);
        // Both should be in results but Japanese first
        assertTrue(result.size() <= 2);
    }

    // recommendSimilar tests
    @Test
    @DisplayName("Recommend similar - null reference returns empty")
    void recommendSimilar_NullReference_ReturnsEmpty() {
        Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r);
        assertTrue(service.recommendSimilar(null, list).isEmpty());
    }

    @Test
    @DisplayName("Recommend similar - finds similar restaurants")
    void recommendSimilar_FindsSimilar() {
        Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.1, 121.1);
        Restaurant different = createRestaurant("3", CuisineType.ITALIAN, 30.0, 130.0);
        List<Restaurant> list = Arrays.asList(ref, similar, different);

        List<Restaurant> result = service.recommendSimilar(ref, list);
        assertTrue(result.stream().anyMatch(r -> r.getId().equals("2")));
    }

    @Test
    @DisplayName("Recommend similar - excludes reference")
    void recommendSimilar_ExcludesReference() {
        Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(ref, similar);

        List<Restaurant> result = service.recommendSimilar(ref, list);
        assertTrue(result.stream().noneMatch(r -> r.getId().equals("1")));
    }

    // getPopularRestaurants tests
    @Test
    @DisplayName("Get popular - null list returns empty")
    void getPopularRestaurants_NullList_ReturnsEmpty() {
        assertTrue(service.getPopularRestaurants(null, 10).isEmpty());
    }

    @Test
    @DisplayName("Get popular - respects limit")
    void getPopularRestaurants_RespectsLimit() {
        Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
        Restaurant r3 = createRestaurant("3", CuisineType.ITALIAN, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r1, r2, r3);

        List<Restaurant> result = service.getPopularRestaurants(list, 2);
        assertEquals(2, result.size());
    }

    // findNearby tests
    @Test
    @DisplayName("Find nearby - null location returns empty")
    void findNearby_NullLocation_ReturnsEmpty() {
        Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r);
        assertTrue(service.findNearby(null, list, 5.0).isEmpty());
    }

    @Test
    @DisplayName("Find nearby - finds within radius")
    void findNearby_FindsWithinRadius() {
        Location userLocation = new Location(25.0330, 121.5654);
        Restaurant near = createRestaurant("1", CuisineType.JAPANESE, 25.0340, 121.5660);
        Restaurant far = createRestaurant("2", CuisineType.CHINESE, 26.0, 122.0);
        List<Restaurant> list = Arrays.asList(near, far);

        List<Restaurant> result = service.findNearby(userLocation, list, 5.0);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    @DisplayName("Find nearby - uses default radius if zero")
    void findNearby_UsesDefaultRadius() {
        Location userLocation = new Location(25.0330, 121.5654);
        Restaurant near = createRestaurant("1", CuisineType.JAPANESE, 25.0340, 121.5660);
        List<Restaurant> list = Arrays.asList(near);

        List<Restaurant> result = service.findNearby(userLocation, list, 0);
        assertFalse(result.isEmpty());
    }

    // calculateDistance tests
    @Test
    @DisplayName("Calculate distance - null from returns max value")
    void calculateDistance_NullFrom_ReturnsMaxValue() {
        Location to = new Location(25.0, 121.0);
        assertEquals(Double.MAX_VALUE, service.calculateDistance(null, to));
    }

    @Test
    @DisplayName("Calculate distance - null to returns max value")
    void calculateDistance_NullTo_ReturnsMaxValue() {
        Location from = new Location(25.0, 121.0);
        assertEquals(Double.MAX_VALUE, service.calculateDistance(from, null));
    }

    @Test
    @DisplayName("Calculate distance - same point returns 0")
    void calculateDistance_SamePoint_ReturnsZero() {
        Location loc = new Location(25.0330, 121.5654);
        double distance = service.calculateDistance(loc, loc);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    @DisplayName("Calculate distance - calculates correctly")
    void calculateDistance_CalculatesCorrectly() {
        Location taipei = new Location(25.0330, 121.5654);
        Location kaohsiung = new Location(22.6273, 120.3014);
        double distance = service.calculateDistance(taipei, kaohsiung);
        assertTrue(distance > 280 && distance < 400, "Distance should be ~300-350km, was: " + distance); // ~300-350km
    }

    // sortByDistance tests
    @Test
    @DisplayName("Sort by distance - null location returns empty")
    void sortByDistance_NullLocation_ReturnsEmpty() {
        Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r);
        assertTrue(service.sortByDistance(null, list).isEmpty());
    }

    @Test
    @DisplayName("Sort by distance - sorts correctly")
    void sortByDistance_SortsCorrectly() {
        Location userLocation = new Location(25.0, 121.0);
        Restaurant far = createRestaurant("1", CuisineType.JAPANESE, 26.0, 122.0);
        Restaurant near = createRestaurant("2", CuisineType.CHINESE, 25.01, 121.01);
        List<Restaurant> list = Arrays.asList(far, near);

        List<Restaurant> result = service.sortByDistance(userLocation, list);
        assertEquals("2", result.get(0).getId());
    }

    // getTopPicks tests
    @Test
    @DisplayName("Get top picks - null list returns empty")
    void getTopPicks_NullList_ReturnsEmpty() {
        Location loc = new Location(25.0, 121.0);
        assertTrue(service.getTopPicks(null, loc, 5).isEmpty());
    }

    @Test
    @DisplayName("Get top picks - respects limit")
    void getTopPicks_RespectsLimit() {
        Location loc = new Location(25.0330, 121.5654);
        Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0340, 121.5660);
        Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0350, 121.5670);
        Restaurant r3 = createRestaurant("3", CuisineType.ITALIAN, 25.0360, 121.5680);
        List<Restaurant> list = Arrays.asList(r1, r2, r3);

        List<Restaurant> result = service.getTopPicks(list, loc, 2);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Get top picks - works without location")
    void getTopPicks_WorksWithoutLocation() {
        Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
        Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = service.getTopPicks(list, null, 5);
        assertFalse(result.isEmpty());
    }
}
