package org.example.restaurant.service;

import org.example.restaurant.model.MenuItem;
import org.example.restaurant.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

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

    @Nested
    @DisplayName("Range Filtering")
    class RangeFiltering {
        @Test
        @DisplayName("filterByPriceRange - null 列表返回空")
        void filterByPriceRange_NullList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.filterByPriceRange(null, 0.0, 100.0).isEmpty());
        }

        @Test
        @DisplayName("filterByPriceRange - 最小大於最大返回空")
        void filterByPriceRange_MinGreaterThanMax_ReturnsEmpty() {
            Restaurant r = createRestaurantWithMenu(100, 200, 300);
            List<Restaurant> list = Arrays.asList(r);
            assertTrue(priceAnalyzer.filterByPriceRange(list, 500.0, 100.0).isEmpty());
        }

        @Test
        @DisplayName("filterByPriceRange - 正確篩選")
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

        @Test
        @DisplayName("filterByPriceRange - 空列表返回空")
        void filterByPriceRange_EmptyList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.filterByPriceRange(new java.util.ArrayList<>(), 0.0, 100.0).isEmpty());
        }

        @Test
        @DisplayName("filterByPriceRange - 跳過 null 餐廳")
        void filterByPriceRange_SkipsNullRestaurants() {
            Restaurant r1 = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r1, null);
            List<Restaurant> result = priceAnalyzer.filterByPriceRange(list, 0.0, 200.0);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("filterByPriceRange - 使用餐廳平均價格")
        void filterByPriceRange_UsesRestaurantAveragePrice() {
            Restaurant r = new Restaurant("1", "Test");
            r.setAveragePrice(150);
            List<Restaurant> list = Arrays.asList(r);
            List<Restaurant> result = priceAnalyzer.filterByPriceRange(list, 100.0, 200.0);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("filterByPriceRange - null 最小值包含所有")
        void filterByPriceRange_NullMin_IncludesAll() {
            Restaurant r = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r);
            List<Restaurant> result = priceAnalyzer.filterByPriceRange(list, null, 200.0);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("filterByPriceRange - null 最大值包含所有")
        void filterByPriceRange_NullMax_IncludesAll() {
            Restaurant r = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r);
            List<Restaurant> result = priceAnalyzer.filterByPriceRange(list, 50.0, null);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Level Categorization")
    class LevelCategorization {
        @Test
        @DisplayName("categorizePriceLevel - null 返回 0")
        void categorizePriceLevel_NullRestaurant_ReturnsZero() {
            assertEquals(0, priceAnalyzer.categorizePriceLevel(null));
        }

        @Test
        @DisplayName("categorizePriceLevel - 使用現有等級")
        void categorizePriceLevel_UsesExistingLevel() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setPriceLevel(3);
            assertEquals(3, priceAnalyzer.categorizePriceLevel(restaurant));
        }

        @Test
        @DisplayName("categorizePriceLevel - 平價")
        void categorizePriceLevel_Cheap() {
            Restaurant restaurant = createRestaurantWithMenu(100, 150);
            assertEquals(PriceAnalyzer.PRICE_LEVEL_CHEAP, priceAnalyzer.categorizePriceLevel(restaurant));
        }

        @Test
        @DisplayName("categorizePriceLevel - 中等")
        void categorizePriceLevel_Moderate() {
            Restaurant restaurant = createRestaurantWithMenu(300, 400);
            assertEquals(PriceAnalyzer.PRICE_LEVEL_MODERATE, priceAnalyzer.categorizePriceLevel(restaurant));
        }

        @Test
        @DisplayName("categorizePriceLevel - 高級")
        void categorizePriceLevel_Expensive() {
            Restaurant restaurant = createRestaurantWithMenu(600, 800);
            assertEquals(PriceAnalyzer.PRICE_LEVEL_EXPENSIVE, priceAnalyzer.categorizePriceLevel(restaurant));
        }

        @Test
        @DisplayName("categorizePriceLevel - 奢華")
        void categorizePriceLevel_Luxury() {
            Restaurant restaurant = createRestaurantWithMenu(1500, 2000);
            assertEquals(PriceAnalyzer.PRICE_LEVEL_LUXURY, priceAnalyzer.categorizePriceLevel(restaurant));
        }

        @Test
        @DisplayName("categorizePriceLevel - 無價格資料返回 0")
        void categorizePriceLevel_NoPriceData_ReturnsZero() {
            Restaurant restaurant = new Restaurant("1", "Test");
            assertEquals(0, priceAnalyzer.categorizePriceLevel(restaurant));
        }
    }

    @Nested
    @DisplayName("Level Filtering")
    class LevelFiltering {
        @Test
        @DisplayName("filterByPriceLevel - 正確篩選")
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

        @Test
        @DisplayName("filterByPriceLevel - null 列表返回空")
        void filterByPriceLevel_NullList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.filterByPriceLevel(null, PriceAnalyzer.PRICE_LEVEL_CHEAP).isEmpty());
        }

        @Test
        @DisplayName("filterByPriceLevel - 空列表返回空")
        void filterByPriceLevel_EmptyList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.filterByPriceLevel(new java.util.ArrayList<>(), PriceAnalyzer.PRICE_LEVEL_CHEAP)
                    .isEmpty());
        }

        @Test
        @DisplayName("filterByPriceLevel - 無效等級返回所有")
        void filterByPriceLevel_InvalidLevel_ReturnsAll() {
            Restaurant r1 = createRestaurantWithMenu(100);
            Restaurant r2 = createRestaurantWithMenu(500);
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = priceAnalyzer.filterByPriceLevel(list, 0);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("filterByPriceLevel - 等級 5 無效返回所有")
        void filterByPriceLevel_Level5Invalid_ReturnsAll() {
            Restaurant r1 = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r1);

            List<Restaurant> result = priceAnalyzer.filterByPriceLevel(list, 5);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Calculation")
    class Calculation {
        @Test
        @DisplayName("calculateAveragePrice - null 餐廳返回 0")
        void calculateAveragePrice_NullRestaurant_ReturnsZero() {
            assertEquals(0.0, priceAnalyzer.calculateAveragePrice(null));
        }

        @Test
        @DisplayName("calculateAveragePrice - 空菜單使用餐廳平均價")
        void calculateAveragePrice_EmptyMenu_UsesRestaurantAverage() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setAveragePrice(250);
            assertEquals(250.0, priceAnalyzer.calculateAveragePrice(restaurant));
        }

        @Test
        @DisplayName("calculateAveragePrice - 從菜單計算")
        void calculateAveragePrice_CalculatesFromMenu() {
            Restaurant restaurant = createRestaurantWithMenu(100, 200, 300);
            assertEquals(200.0, priceAnalyzer.calculateAveragePrice(restaurant));
        }

        @Test
        @DisplayName("calculateAveragePrice - 忽略不可用項目")
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

        @Test
        @DisplayName("calculateAveragePrice - 跳過 null 菜單項目")
        void calculateAveragePrice_SkipsNullMenuItems() {
            Restaurant restaurant = new Restaurant("1", "Test");
            MenuItem item = new MenuItem("1", "Item", 200);
            item.setAvailable(true);
            restaurant.addMenuItem(item);
            restaurant.getMenu().add(null);
            assertEquals(200.0, priceAnalyzer.calculateAveragePrice(restaurant));
        }

        @Test
        @DisplayName("calculateAveragePrice - 跳過零價格項目")
        void calculateAveragePrice_SkipsZeroPriceItems() {
            Restaurant restaurant = new Restaurant("1", "Test");
            MenuItem item1 = new MenuItem("1", "Item 1", 200);
            item1.setAvailable(true);
            MenuItem item2 = new MenuItem("2", "Item 2", 0);
            item2.setAvailable(true);
            restaurant.addMenuItem(item1);
            restaurant.addMenuItem(item2);
            assertEquals(200.0, priceAnalyzer.calculateAveragePrice(restaurant));
        }

        @Test
        @DisplayName("calculateAveragePrice - 所有項目不可用使用餐廳平均價")
        void calculateAveragePrice_AllUnavailable_UsesRestaurantAverage() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setAveragePrice(300);
            MenuItem item = new MenuItem("1", "Item", 200);
            item.setAvailable(false);
            restaurant.addMenuItem(item);
            assertEquals(300.0, priceAnalyzer.calculateAveragePrice(restaurant));
        }

        @Test
        @DisplayName("calculatePriceStatistics - null 列表返回空統計")
        void calculatePriceStatistics_NullList_ReturnsEmptyStats() {
            PriceAnalyzer.PriceStatistics stats = priceAnalyzer.calculatePriceStatistics(null);
            assertEquals(0, stats.count);
        }

        @Test
        @DisplayName("calculatePriceStatistics - 正確計算")
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

        @Test
        @DisplayName("calculatePriceStatistics - 空列表返回空統計")
        void calculatePriceStatistics_EmptyList_ReturnsEmptyStats() {
            PriceAnalyzer.PriceStatistics stats = priceAnalyzer.calculatePriceStatistics(new java.util.ArrayList<>());
            assertEquals(0, stats.count);
        }

        @Test
        @DisplayName("calculatePriceStatistics - 跳過 null 餐廳")
        void calculatePriceStatistics_SkipsNullRestaurants() {
            Restaurant r = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r, null);
            PriceAnalyzer.PriceStatistics stats = priceAnalyzer.calculatePriceStatistics(list);
            assertEquals(1, stats.count);
        }

        @Test
        @DisplayName("calculatePriceStatistics - 跳過無價格餐廳")
        void calculatePriceStatistics_SkipsNoPriceRestaurants() {
            Restaurant r1 = createRestaurantWithMenu(100);
            Restaurant r2 = new Restaurant("2", "Test");
            List<Restaurant> list = Arrays.asList(r1, r2);
            PriceAnalyzer.PriceStatistics stats = priceAnalyzer.calculatePriceStatistics(list);
            assertEquals(1, stats.count);
        }

        @Test
        @DisplayName("PriceStatistics - toString 格式正確")
        void priceStatistics_ToString_FormatsCorrectly() {
            PriceAnalyzer.PriceStatistics stats = new PriceAnalyzer.PriceStatistics();
            stats.count = 3;
            stats.min = 100;
            stats.max = 300;
            stats.average = 200;
            stats.median = 200;
            String result = stats.toString();
            assertTrue(result.contains("count=3"));
            assertTrue(result.contains("min=100"));
            assertTrue(result.contains("PriceStats"));
        }
    }

    @Nested
    @DisplayName("Sorting")
    class Sorting {
        @Test
        @DisplayName("sortByPrice - 升冪排序")
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
        @DisplayName("sortByPrice - 降冪排序")
        void sortByPrice_Descending_SortsCorrectly() {
            Restaurant r1 = createRestaurantWithMenu(200);
            r1.setId("1");
            Restaurant r2 = createRestaurantWithMenu(500);
            r2.setId("2");
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = priceAnalyzer.sortByPrice(list, false);
            assertEquals("2", result.get(0).getId());
        }

        @Test
        @DisplayName("sortByPrice - null 列表返回空")
        void sortByPrice_NullList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.sortByPrice(null, true).isEmpty());
        }

        @Test
        @DisplayName("sortByPrice - 空列表返回空")
        void sortByPrice_EmptyList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.sortByPrice(new java.util.ArrayList<>(), true).isEmpty());
        }

        @Test
        @DisplayName("sortByPrice - 跳過 null 餐廳")
        void sortByPrice_SkipsNullRestaurants() {
            Restaurant r1 = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r1, null);
            List<Restaurant> result = priceAnalyzer.sortByPrice(list, true);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Budget Recommendation")
    class BudgetRecommendation {
        @Test
        @DisplayName("recommendByBudget - null 列表返回空")
        void recommendByBudget_NullList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.recommendByBudget(null, 500).isEmpty());
        }

        @Test
        @DisplayName("recommendByBudget - 零預算返回空")
        void recommendByBudget_ZeroBudget_ReturnsEmpty() {
            Restaurant r = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r);
            assertTrue(priceAnalyzer.recommendByBudget(list, 0).isEmpty());
        }

        @Test
        @DisplayName("recommendByBudget - 包含預算內")
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

        @Test
        @DisplayName("recommendByBudget - 空列表返回空")
        void recommendByBudget_EmptyList_ReturnsEmpty() {
            assertTrue(priceAnalyzer.recommendByBudget(new java.util.ArrayList<>(), 500).isEmpty());
        }

        @Test
        @DisplayName("recommendByBudget - 負預算返回空")
        void recommendByBudget_NegativeBudget_ReturnsEmpty() {
            Restaurant r = createRestaurantWithMenu(100);
            List<Restaurant> list = Arrays.asList(r);
            assertTrue(priceAnalyzer.recommendByBudget(list, -100).isEmpty());
        }

        @Test
        @DisplayName("recommendByBudget - 包含略超預算")
        void recommendByBudget_IncludesSlightlyOverBudget() {
            Restaurant r = createRestaurantWithMenu(105);
            r.setId("1");
            List<Restaurant> list = Arrays.asList(r);
            // 105 <= 100 * 1.1 = 110, 應該包含
            List<Restaurant> result = priceAnalyzer.recommendByBudget(list, 100);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("recommendByBudget - 跳過無價格資料")
        void recommendByBudget_SkipsNoPriceData() {
            Restaurant r = new Restaurant("1", "Test");
            List<Restaurant> list = Arrays.asList(r);
            assertTrue(priceAnalyzer.recommendByBudget(list, 500).isEmpty());
        }

        @Test
        @DisplayName("isAffordable - null 餐廳返回 false")
        void isAffordable_NullRestaurant_ReturnsFalse() {
            assertFalse(priceAnalyzer.isAffordable(null, null));
        }

        @Test
        @DisplayName("isAffordable - 在平均內返回 true")
        void isAffordable_WithinAverage_ReturnsTrue() {
            Restaurant r1 = createRestaurantWithMenu(200);
            Restaurant r2 = createRestaurantWithMenu(200);
            Restaurant r3 = createRestaurantWithMenu(200);
            List<Restaurant> referenceSet = Arrays.asList(r1, r2, r3);

            Restaurant test = createRestaurantWithMenu(200);
            assertTrue(priceAnalyzer.isAffordable(test, referenceSet));
        }

        @Test
        @DisplayName("isAffordable - 空參考集返回 true")
        void isAffordable_EmptyReferenceSet_ReturnsTrue() {
            Restaurant test = createRestaurantWithMenu(100);
            assertTrue(priceAnalyzer.isAffordable(test, new java.util.ArrayList<>()));
        }

        @Test
        @DisplayName("isAffordable - 無價格資料返回 true")
        void isAffordable_NoPriceData_ReturnsTrue() {
            Restaurant test = new Restaurant("1", "Test");
            Restaurant ref = createRestaurantWithMenu(100);
            List<Restaurant> referenceSet = Arrays.asList(ref);
            assertTrue(priceAnalyzer.isAffordable(test, referenceSet));
        }

        @Test
        @DisplayName("isAffordable - 超過平均返回 false")
        void isAffordable_ExceedsAverage_ReturnsFalse() {
            Restaurant r1 = createRestaurantWithMenu(100);
            Restaurant r2 = createRestaurantWithMenu(100);
            List<Restaurant> referenceSet = Arrays.asList(r1, r2);

            Restaurant test = createRestaurantWithMenu(200);
            assertFalse(priceAnalyzer.isAffordable(test, referenceSet));
        }
    }

    @Nested
    @DisplayName("Description")
    class Description {
        @Test
        @DisplayName("getPriceRangeDescription - null 返回未知")
        void getPriceRangeDescription_NullRestaurant_ReturnsUnknown() {
            assertEquals("未知", priceAnalyzer.getPriceRangeDescription(null));
        }

        @Test
        @DisplayName("getPriceRangeDescription - 平價")
        void getPriceRangeDescription_Cheap() {
            Restaurant restaurant = createRestaurantWithMenu(100);
            String desc = priceAnalyzer.getPriceRangeDescription(restaurant);
            assertTrue(desc.contains("平價"));
        }

        @Test
        @DisplayName("getPriceRangeDescription - 中等")
        void getPriceRangeDescription_Moderate() {
            Restaurant restaurant = createRestaurantWithMenu(350);
            String desc = priceAnalyzer.getPriceRangeDescription(restaurant);
            assertTrue(desc.contains("中等"));
        }

        @Test
        @DisplayName("getPriceRangeDescription - 高級")
        void getPriceRangeDescription_Expensive() {
            Restaurant restaurant = createRestaurantWithMenu(700);
            String desc = priceAnalyzer.getPriceRangeDescription(restaurant);
            assertTrue(desc.contains("高級"));
        }

        @Test
        @DisplayName("getPriceRangeDescription - 奢華")
        void getPriceRangeDescription_Luxury() {
            Restaurant restaurant = createRestaurantWithMenu(1500);
            String desc = priceAnalyzer.getPriceRangeDescription(restaurant);
            assertTrue(desc.contains("奢華"));
        }

        @Test
        @DisplayName("getPriceRangeDescription - 無價格資料返回未知")
        void getPriceRangeDescription_NoPriceData_ReturnsUnknown() {
            Restaurant restaurant = new Restaurant("1", "Test");
            String desc = priceAnalyzer.getPriceRangeDescription(restaurant);
            assertEquals("未知", desc);
        }
    }
}
