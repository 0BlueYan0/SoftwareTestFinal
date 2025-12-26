package org.example.restaurant.service;

import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantSearchServiceTest {

    private RestaurantSearchService searchService;
    private RestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RestaurantRepository();
        searchService = new RestaurantSearchService(repository);

        // Add test data
        Restaurant r1 = new Restaurant("1", "Tokyo Sushi", CuisineType.JAPANESE,
                new Location(25.0330, 121.5654, "信義路", "台北市"));
        r1.setActive(true);
        r1.setHasDelivery(true);
        r1.setHasTakeout(true);
        r1.addReview(new Review("rev1", "1", 5, "Great!"));
        BusinessHours hours1 = new BusinessHours();
        for (DayOfWeek day : DayOfWeek.values()) {
            hours1.setHours(day, LocalTime.of(11, 0), LocalTime.of(22, 0));
        }
        r1.setBusinessHours(hours1);
        repository.save(r1);

        Restaurant r2 = new Restaurant("2", "義大利餐廳", CuisineType.ITALIAN,
                new Location(25.0420, 121.5320, "中山北路", "台北市"));
        r2.setActive(true);
        r2.setHasParking(true);
        r2.addReview(new Review("rev2", "2", 4, "Good pasta"));
        MenuItem item = new MenuItem("m1", "Pizza", 350);
        item.setAvailable(true);
        r2.addMenuItem(item);
        repository.save(r2);

        Restaurant r3 = new Restaurant("3", "小籠包專賣店", CuisineType.TAIWANESE,
                new Location(25.0280, 121.5430, "永康街", "台北市"));
        r3.setActive(true);
        r3.setAcceptsReservations(true);
        r3.addReview(new Review("rev3", "3", 5, "Best xiaolongbao!"));
        repository.save(r3);

        Restaurant r4 = new Restaurant("4", "高雄海鮮", CuisineType.SEAFOOD,
                new Location(22.6273, 120.3014, "海港路", "高雄市"));
        r4.setActive(true);
        repository.save(r4);

        Restaurant inactive = new Restaurant("5", "Closed Restaurant");
        inactive.setActive(false);
        repository.save(inactive);
    }

    // searchByName tests
    @Test
    @DisplayName("Search by name - null returns empty")
    void searchByName_NullName_ReturnsEmpty() {
        assertTrue(searchService.searchByName(null).isEmpty());
    }

    @Test
    @DisplayName("Search by name - empty returns empty")
    void searchByName_EmptyName_ReturnsEmpty() {
        assertTrue(searchService.searchByName("  ").isEmpty());
    }

    @Test
    @DisplayName("Search by name - exact match")
    void searchByName_ExactMatch() {
        List<Restaurant> result = searchService.searchByName("Tokyo Sushi");
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    @DisplayName("Search by name - case insensitive")
    void searchByName_CaseInsensitive() {
        List<Restaurant> result = searchService.searchByName("tokyo sushi");
        assertEquals(1, result.size());
    }

    // searchByNameFuzzy tests
    @Test
    @DisplayName("Search by name fuzzy - null returns empty")
    void searchByNameFuzzy_NullKeyword_ReturnsEmpty() {
        assertTrue(searchService.searchByNameFuzzy(null).isEmpty());
    }

    @Test
    @DisplayName("Search by name fuzzy - partial match")
    void searchByNameFuzzy_PartialMatch() {
        List<Restaurant> result = searchService.searchByNameFuzzy("Sushi");
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    @DisplayName("Search by name fuzzy - Chinese characters")
    void searchByNameFuzzy_ChineseCharacters() {
        List<Restaurant> result = searchService.searchByNameFuzzy("小籠包");
        assertEquals(1, result.size());
    }

    // searchByCity tests
    @Test
    @DisplayName("Search by city - null returns empty")
    void searchByCity_NullCity_ReturnsEmpty() {
        assertTrue(searchService.searchByCity(null).isEmpty());
    }

    @Test
    @DisplayName("Search by city - finds restaurants in city")
    void searchByCity_FindsRestaurantsInCity() {
        List<Restaurant> result = searchService.searchByCity("台北市");
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Search by city - different city")
    void searchByCity_DifferentCity() {
        List<Restaurant> result = searchService.searchByCity("高雄市");
        assertEquals(1, result.size());
        assertEquals("4", result.get(0).getId());
    }

    // searchByCuisineType tests
    @Test
    @DisplayName("Search by cuisine type - null returns empty")
    void searchByCuisineType_NullType_ReturnsEmpty() {
        assertTrue(searchService.searchByCuisineType(null).isEmpty());
    }

    @Test
    @DisplayName("Search by cuisine type - finds correct type")
    void searchByCuisineType_FindsCorrectType() {
        List<Restaurant> result = searchService.searchByCuisineType(CuisineType.JAPANESE);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    // searchByMultipleCuisineTypes tests
    @Test
    @DisplayName("Search by multiple cuisine types - null returns empty")
    void searchByMultipleCuisineTypes_NullTypes_ReturnsEmpty() {
        assertTrue(searchService.searchByMultipleCuisineTypes(null).isEmpty());
    }

    @Test
    @DisplayName("Search by multiple cuisine types - finds any matching")
    void searchByMultipleCuisineTypes_FindsAnyMatching() {
        Set<CuisineType> types = new HashSet<>();
        types.add(CuisineType.JAPANESE);
        types.add(CuisineType.ITALIAN);

        List<Restaurant> result = searchService.searchByMultipleCuisineTypes(types);
        assertEquals(2, result.size());
    }

    // searchByMultipleCriteria tests
    @Test
    @DisplayName("Search by multiple criteria - null returns all active")
    void searchByMultipleCriteria_NullCriteria_ReturnsAllActive() {
        List<Restaurant> result = searchService.searchByMultipleCriteria(null);
        List<Restaurant> allActive = searchService.getAllRestaurants();
        assertEquals(allActive.size(), result.size()); // Excludes inactive
    }

    @Test
    @DisplayName("Search by multiple criteria - keyword filter")
    void searchByMultipleCriteria_KeywordFilter() {
        SearchCriteria criteria = new SearchCriteria().keyword("sushi");
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Search by multiple criteria - city filter")
    void searchByMultipleCriteria_CityFilter() {
        SearchCriteria criteria = new SearchCriteria().city("台北");
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Search by multiple criteria - cuisine filter")
    void searchByMultipleCriteria_CuisineFilter() {
        SearchCriteria criteria = new SearchCriteria().cuisineType(CuisineType.TAIWANESE);
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Search by multiple criteria - delivery filter")
    void searchByMultipleCriteria_DeliveryFilter() {
        SearchCriteria criteria = new SearchCriteria().hasDelivery(true);
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    @DisplayName("Search by multiple criteria - parking filter")
    void searchByMultipleCriteria_ParkingFilter() {
        SearchCriteria criteria = new SearchCriteria().hasParking(true);
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).getId());
    }

    @Test
    @DisplayName("Search by multiple criteria - reservations filter")
    void searchByMultipleCriteria_ReservationsFilter() {
        SearchCriteria criteria = new SearchCriteria().acceptsReservations(true);
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(1, result.size());
        assertEquals("3", result.get(0).getId());
    }

    @Test
    @DisplayName("Search by multiple criteria - combined filters")
    void searchByMultipleCriteria_CombinedFilters() {
        SearchCriteria criteria = new SearchCriteria()
                .city("台北")
                .cuisineType(CuisineType.JAPANESE);
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    @DisplayName("Search by multiple criteria - pagination")
    void searchByMultipleCriteria_Pagination() {
        SearchCriteria criteria = new SearchCriteria().limit(2).offset(0);
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Search by multiple criteria - offset beyond results")
    void searchByMultipleCriteria_OffsetBeyondResults() {
        SearchCriteria criteria = new SearchCriteria().offset(100);
        List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
        assertTrue(result.isEmpty());
    }

    // sortResults tests
    @Test
    @DisplayName("Sort results - by name ascending")
    void sortResults_ByNameAscending() {
        SearchCriteria criteria = new SearchCriteria()
                .sortBy(SearchCriteria.SortType.NAME)
                .ascending(true);
        List<Restaurant> allRestaurants = searchService.getAllRestaurants();
        List<Restaurant> sorted = searchService.sortResults(allRestaurants, criteria);

        // Check alphabetical order
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).getName().compareToIgnoreCase(sorted.get(i + 1).getName()) <= 0);
        }
    }

    @Test
    @DisplayName("Sort results - by rating descending")
    void sortResults_ByRatingDescending() {
        SearchCriteria criteria = new SearchCriteria()
                .sortBy(SearchCriteria.SortType.RATING)
                .ascending(false);
        List<Restaurant> allRestaurants = searchService.getAllRestaurants();
        List<Restaurant> sorted = searchService.sortResults(allRestaurants, criteria);
        assertFalse(sorted.isEmpty());
    }

    // searchGlobal tests
    @Test
    @DisplayName("Search global - null keyword returns empty")
    void searchGlobal_NullKeyword_ReturnsEmpty() {
        assertTrue(searchService.searchGlobal(null).isEmpty());
    }

    @Test
    @DisplayName("Search global - matches name")
    void searchGlobal_MatchesName() {
        List<Restaurant> result = searchService.searchGlobal("Tokyo");
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Search global - matches city")
    void searchGlobal_MatchesCity() {
        List<Restaurant> result = searchService.searchGlobal("高雄");
        assertEquals(1, result.size());
    }

    // getAllRestaurants tests
    @Test
    @DisplayName("Get all restaurants - excludes inactive")
    void getAllRestaurants_ExcludesInactive() {
        List<Restaurant> result = searchService.getAllRestaurants();
        // Should have at least 4 active restaurants from setUp
        assertTrue(result.size() >= 4);
        assertTrue(result.stream().noneMatch(r -> r.getId().equals("5")));
    }

    // countRestaurants tests
    @Test
    @DisplayName("Count restaurants - counts only active")
    void countRestaurants_CountsOnlyActive() {
        // Should have at least 4 active restaurants
        assertTrue(searchService.countRestaurants() >= 4);
    }

    // searchByDistrict tests
    @Test
    @DisplayName("Search by district - null returns empty")
    void searchByDistrict_NullDistrict_ReturnsEmpty() {
        assertTrue(searchService.searchByDistrict(null).isEmpty());
    }
}
