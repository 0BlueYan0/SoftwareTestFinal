package org.example.restaurant.service;

import org.example.restaurant.model.MenuItem;
import org.example.restaurant.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceAnalyzerTest {

    private PriceAnalyzer priceAnalyzer;

    @BeforeEach
    void setUp() {
        priceAnalyzer = new PriceAnalyzer();
    }

    private Restaurant createRestaurantWithMenu(double... prices) {
        Restaurant restaurant = new Restaurant("1", "Test");
        for (int i = 0; i < prices.length; i++) {
            MenuItem item = new MenuItem("item" + i, "Item " + i, prices[i]);
            item.setAvailable(true);
            restaurant.addMenuItem(item);
        }
        return restaurant;
    }

    // filterByPriceRange tests
    @Test
    @DisplayName("Filter by price range - null list returns empty")
    void filterByPriceRange_NullList_ReturnsEmpty() {
        assertTrue(priceAnalyzer.filterByPriceRange(null, 0.0, 100.0).isEmpty());
    }

    @Test
    @DisplayName("Filter by price range - min greater than max returns empty")
    void filterByPriceRange_MinGreaterThanMax_ReturnsEmpty() {
        Restaurant r = createRestaurantWithMenu(100, 200, 300);
        List<Restaurant> list = Arrays.asList(r);
        assertTrue(priceAnalyzer.filterByPriceRange(list, 500.0, 100.0).isEmpty());
    }

    @Test
    @DisplayName("Filter by price range - filters correctly")
    void filterByPriceRange_FiltersCorrectly() {
        Restaurant r1 = createRestaurantWithMenu(100, 150, 200);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithMenu(500, 600, 700);
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = priceAnalyzer.filterByPriceRange(list, 100.0, 300.0);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    // calculateAveragePrice tests
    @Test
    @DisplayName("Calculate average price - null restaurant returns 0")
    void calculateAveragePrice_NullRestaurant_ReturnsZero() {
        assertEquals(0.0, priceAnalyzer.calculateAveragePrice(null));
    }

    @Test
    @DisplayName("Calculate average price - empty menu uses restaurant average")
    void calculateAveragePrice_EmptyMenu_UsesRestaurantAverage() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setAveragePrice(250);
        assertEquals(250.0, priceAnalyzer.calculateAveragePrice(restaurant));
    }

    @Test
    @DisplayName("Calculate average price - calculates from menu")
    void calculateAveragePrice_CalculatesFromMenu() {
        Restaurant restaurant = createRestaurantWithMenu(100, 200, 300);
        assertEquals(200.0, priceAnalyzer.calculateAveragePrice(restaurant));
    }

    @Test
    @DisplayName("Calculate average price - ignores unavailable items")
    void calculateAveragePrice_IgnoresUnavailableItems() {
        Restaurant restaurant = new Restaurant("1", "Test");
        MenuItem available = new MenuItem("1", "Item 1", 200);
        available.setAvailable(true);
        MenuItem unavailable = new MenuItem("2", "Item 2", 1000);
        unavailable.setAvailable(false);
        restaurant.addMenuItem(available);
        restaurant.addMenuItem(unavailable);
        assertEquals(200.0, priceAnalyzer.calculateAveragePrice(restaurant));
    }

    // sortByPrice tests
    @Test
    @DisplayName("Sort by price - ascending")
    void sortByPrice_Ascending_SortsCorrectly() {
        Restaurant r1 = createRestaurantWithMenu(500);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithMenu(200);
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = priceAnalyzer.sortByPrice(list, true);
        assertEquals("2", result.get(0).getId());
    }

    @Test
    @DisplayName("Sort by price - descending")
    void sortByPrice_Descending_SortsCorrectly() {
        Restaurant r1 = createRestaurantWithMenu(200);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithMenu(500);
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = priceAnalyzer.sortByPrice(list, false);
        assertEquals("2", result.get(0).getId());
    }

    // categorizePriceLevel tests
    @Test
    @DisplayName("Categorize price level - null returns 0")
    void categorizePriceLevel_NullRestaurant_ReturnsZero() {
        assertEquals(0, priceAnalyzer.categorizePriceLevel(null));
    }

    @Test
    @DisplayName("Categorize price level - uses existing level")
    void categorizePriceLevel_UsesExistingLevel() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setPriceLevel(3);
        assertEquals(3, priceAnalyzer.categorizePriceLevel(restaurant));
    }

    @Test
    @DisplayName("Categorize price level - cheap")
    void categorizePriceLevel_Cheap() {
        Restaurant restaurant = createRestaurantWithMenu(100, 150);
        assertEquals(PriceAnalyzer.PRICE_LEVEL_CHEAP, priceAnalyzer.categorizePriceLevel(restaurant));
    }

    @Test
    @DisplayName("Categorize price level - moderate")
    void categorizePriceLevel_Moderate() {
        Restaurant restaurant = createRestaurantWithMenu(300, 400);
        assertEquals(PriceAnalyzer.PRICE_LEVEL_MODERATE, priceAnalyzer.categorizePriceLevel(restaurant));
    }

    @Test
    @DisplayName("Categorize price level - expensive")
    void categorizePriceLevel_Expensive() {
        Restaurant restaurant = createRestaurantWithMenu(600, 800);
        assertEquals(PriceAnalyzer.PRICE_LEVEL_EXPENSIVE, priceAnalyzer.categorizePriceLevel(restaurant));
    }

    @Test
    @DisplayName("Categorize price level - luxury")
    void categorizePriceLevel_Luxury() {
        Restaurant restaurant = createRestaurantWithMenu(1500, 2000);
        assertEquals(PriceAnalyzer.PRICE_LEVEL_LUXURY, priceAnalyzer.categorizePriceLevel(restaurant));
    }

    // recommendByBudget tests
    @Test
    @DisplayName("Recommend by budget - null list returns empty")
    void recommendByBudget_NullList_ReturnsEmpty() {
        assertTrue(priceAnalyzer.recommendByBudget(null, 500).isEmpty());
    }

    @Test
    @DisplayName("Recommend by budget - zero budget returns empty")
    void recommendByBudget_ZeroBudget_ReturnsEmpty() {
        Restaurant r = createRestaurantWithMenu(100);
        List<Restaurant> list = Arrays.asList(r);
        assertTrue(priceAnalyzer.recommendByBudget(list, 0).isEmpty());
    }

    @Test
    @DisplayName("Recommend by budget - includes within budget")
    void recommendByBudget_IncludesWithinBudget() {
        Restaurant r1 = createRestaurantWithMenu(300);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithMenu(800);
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = priceAnalyzer.recommendByBudget(list, 500);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    // getPriceRangeDescription tests
    @Test
    @DisplayName("Get price range description - null returns unknown")
    void getPriceRangeDescription_NullRestaurant_ReturnsUnknown() {
        assertEquals("未知", priceAnalyzer.getPriceRangeDescription(null));
    }

    @Test
    @DisplayName("Get price range description - cheap")
    void getPriceRangeDescription_Cheap() {
        Restaurant restaurant = createRestaurantWithMenu(100);
        String desc = priceAnalyzer.getPriceRangeDescription(restaurant);
        assertTrue(desc.contains("平價"));
    }

    // calculatePriceStatistics tests
    @Test
    @DisplayName("Calculate price statistics - null list returns empty stats")
    void calculatePriceStatistics_NullList_ReturnsEmptyStats() {
        PriceAnalyzer.PriceStatistics stats = priceAnalyzer.calculatePriceStatistics(null);
        assertEquals(0, stats.count);
    }

    @Test
    @DisplayName("Calculate price statistics - calculates correctly")
    void calculatePriceStatistics_CalculatesCorrectly() {
        Restaurant r1 = createRestaurantWithMenu(100);
        Restaurant r2 = createRestaurantWithMenu(200);
        Restaurant r3 = createRestaurantWithMenu(300);
        List<Restaurant> list = Arrays.asList(r1, r2, r3);

        PriceAnalyzer.PriceStatistics stats = priceAnalyzer.calculatePriceStatistics(list);
        assertEquals(3, stats.count);
        assertEquals(100.0, stats.min);
        assertEquals(300.0, stats.max);
        assertEquals(200.0, stats.average);
    }

    // isAffordable tests
    @Test
    @DisplayName("Is affordable - null restaurant returns false")
    void isAffordable_NullRestaurant_ReturnsFalse() {
        assertFalse(priceAnalyzer.isAffordable(null, null));
    }

    @Test
    @DisplayName("Is affordable - within average returns true")
    void isAffordable_WithinAverage_ReturnsTrue() {
        Restaurant r1 = createRestaurantWithMenu(200);
        Restaurant r2 = createRestaurantWithMenu(200);
        Restaurant r3 = createRestaurantWithMenu(200);
        List<Restaurant> referenceSet = Arrays.asList(r1, r2, r3);

        Restaurant test = createRestaurantWithMenu(200);
        assertTrue(priceAnalyzer.isAffordable(test, referenceSet));
    }

    // filterByPriceLevel tests
    @Test
    @DisplayName("Filter by price level - filters correctly")
    void filterByPriceLevel_FiltersCorrectly() {
        Restaurant r1 = createRestaurantWithMenu(100);
        r1.setId("1");
        Restaurant r2 = createRestaurantWithMenu(500);
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        List<Restaurant> result = priceAnalyzer.filterByPriceLevel(list, PriceAnalyzer.PRICE_LEVEL_CHEAP);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }
}
