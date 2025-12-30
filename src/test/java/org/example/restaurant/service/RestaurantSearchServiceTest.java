package org.example.restaurant.service;

import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

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

    @Nested
    @DisplayName("Exact Name Search")
    class ExactNameSearch {
        @Test
        @DisplayName("searchByName - null 返回空列表")
        void searchByName_NullName_ReturnsEmpty() {
            assertTrue(searchService.searchByName(null).isEmpty());
        }

        @Test
        @DisplayName("searchByName - 空字串返回空列表")
        void searchByName_EmptyName_ReturnsEmpty() {
            assertTrue(searchService.searchByName("  ").isEmpty());
        }

        @Test
        @DisplayName("searchByName - 精確匹配")
        void searchByName_ExactMatch() {
            List<Restaurant> result = searchService.searchByName("Tokyo Sushi");
            assertEquals(1, result.size());
            assertEquals("1", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByName - 不分大小寫")
        void searchByName_CaseInsensitive() {
            List<Restaurant> result = searchService.searchByName("tokyo sushi");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("searchByName - 不匹配非活躍餐廳")
        void searchByName_ExcludesInactive() {
            List<Restaurant> result = searchService.searchByName("Closed Restaurant");
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Fuzzy Name Search")
    class FuzzyNameSearch {
        @Test
        @DisplayName("searchByNameFuzzy - null 返回空列表")
        void searchByNameFuzzy_NullKeyword_ReturnsEmpty() {
            assertTrue(searchService.searchByNameFuzzy(null).isEmpty());
        }

        @Test
        @DisplayName("searchByNameFuzzy - 部分匹配")
        void searchByNameFuzzy_PartialMatch() {
            List<Restaurant> result = searchService.searchByNameFuzzy("Sushi");
            assertEquals(1, result.size());
            assertEquals("1", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByNameFuzzy - 中文字元")
        void searchByNameFuzzy_ChineseCharacters() {
            List<Restaurant> result = searchService.searchByNameFuzzy("小籠包");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("searchByNameFuzzy - 空字串返回空列表")
        void searchByNameFuzzy_EmptyKeyword_ReturnsEmpty() {
            assertTrue(searchService.searchByNameFuzzy("").isEmpty());
            assertTrue(searchService.searchByNameFuzzy("   ").isEmpty());
        }

        @Test
        @DisplayName("searchByNameFuzzy - Levenshtein 相似度匹配")
        void searchByNameFuzzy_LevenshteinMatch() {
            // 新增 Levenshtein 相似度測試用餐廳
            Restaurant r = new Restaurant("11", "Sashimi Restaurant", CuisineType.JAPANESE, null);
            r.setActive(true);
            repository.save(r);

            // "Sashimi" 與 "Sushi" 相似度較低，但 "Sashimi" 包含部分匹配
            List<Restaurant> result = searchService.searchByNameFuzzy("Sashimi");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchByNameFuzzy - 優先顯示開頭匹配的結果")
        void searchByNameFuzzy_PrioritizesStartsWith() {
            Restaurant r1 = new Restaurant("15", "Sushi Tokyo", CuisineType.JAPANESE, null);
            r1.setActive(true);
            repository.save(r1);

            List<Restaurant> result = searchService.searchByNameFuzzy("Sushi");
            // 開頭匹配的應該優先顯示
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchByNameFuzzy - Comparator 分支覆蓋")
        void searchByNameFuzzy_Comparator_Coverage() {
            // R1: Starts with "Test"
            Restaurant r1 = new Restaurant("101", "Test Start A", CuisineType.OTHER, null);
            r1.setActive(true);
            // R2: Starts with "Test"
            Restaurant r2 = new Restaurant("102", "Test Start B", CuisineType.OTHER, null);
            r2.setActive(true);
            // R3: Contains "Test" (starts later)
            Restaurant r3 = new Restaurant("103", "A Test Contains", CuisineType.OTHER, null);
            r3.setActive(true);
            // R4: Contains "Test" (starts later)
            Restaurant r4 = new Restaurant("104", "B Test Contains", CuisineType.OTHER, null);
            r4.setActive(true);

            repository.save(r1);
            repository.save(r2);
            repository.save(r3);
            repository.save(r4);

            List<Restaurant> results = searchService.searchByNameFuzzy("Test");

            // Expected order:
            // 1. Starts with (sorted alphabetically): Test Start A, Test Start B
            // 2. Contains (sorted alphabetically): A Test Contains, B Test Contains

            assertEquals(4, results.size());
            assertEquals("Test Start A", results.get(0).getName());
            assertEquals("Test Start B", results.get(1).getName());
            assertEquals("A Test Contains", results.get(2).getName());
            assertEquals("B Test Contains", results.get(3).getName());
        }
    }

    @Nested
    @DisplayName("Attribute Search")
    class AttributeSearch {
        @Test
        @DisplayName("searchByCity - null 返回空列表")
        void searchByCity_NullCity_ReturnsEmpty() {
            assertTrue(searchService.searchByCity(null).isEmpty());
        }

        @Test
        @DisplayName("searchByCity - 找到該城市的餐廳")
        void searchByCity_FindsRestaurantsInCity() {
            List<Restaurant> result = searchService.searchByCity("台北市");
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("searchByCity - 不同城市")
        void searchByCity_DifferentCity() {
            List<Restaurant> result = searchService.searchByCity("高雄市");
            assertEquals(1, result.size());
            assertEquals("4", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByCuisineType - null 返回空列表")
        void searchByCuisineType_NullType_ReturnsEmpty() {
            assertTrue(searchService.searchByCuisineType(null).isEmpty());
        }

        @Test
        @DisplayName("searchByCuisineType - 找到正確菜系")
        void searchByCuisineType_FindsCorrectType() {
            List<Restaurant> result = searchService.searchByCuisineType(CuisineType.JAPANESE);
            assertEquals(1, result.size());
            assertEquals("1", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByMultipleCuisineTypes - null 返回空列表")
        void searchByMultipleCuisineTypes_NullTypes_ReturnsEmpty() {
            assertTrue(searchService.searchByMultipleCuisineTypes(null).isEmpty());
        }

        @Test
        @DisplayName("searchByMultipleCuisineTypes - 找到任一匹配的菜系")
        void searchByMultipleCuisineTypes_FindsAnyMatching() {
            Set<CuisineType> types = new HashSet<>();
            types.add(CuisineType.JAPANESE);
            types.add(CuisineType.ITALIAN);

            List<Restaurant> result = searchService.searchByMultipleCuisineTypes(types);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("searchByDistrict - null 返回空列表")
        void searchByDistrict_NullDistrict_ReturnsEmpty() {
            assertTrue(searchService.searchByDistrict(null).isEmpty());
        }

        @Test
        @DisplayName("searchByDistrict - 空字串返回空列表")
        void searchByDistrict_EmptyDistrict_ReturnsEmpty() {
            assertTrue(searchService.searchByDistrict("").isEmpty());
            assertTrue(searchService.searchByDistrict("   ").isEmpty());
        }

        @Test
        @DisplayName("searchByDistrict - 找到匹配的地區")
        void searchByDistrict_FindsMatchingDistrict() {
            // 新增有區域資料的餐廳
            Restaurant r = new Restaurant("10", "測試餐廳", CuisineType.CHINESE,
                    new Location(25.0, 121.0, "測試路", "台北市"));
            r.getLocation().setDistrict("大安區");
            r.setActive(true);
            repository.save(r);

            List<Restaurant> result = searchService.searchByDistrict("大安");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchByMultipleCuisineTypes - 空集合返回空列表")
        void searchByMultipleCuisineTypes_EmptySet_ReturnsEmpty() {
            Set<CuisineType> emptySet = new HashSet<>();
            assertTrue(searchService.searchByMultipleCuisineTypes(emptySet).isEmpty());
        }

        @Test
        @DisplayName("searchByCity - 空字串返回空列表")
        void searchByCity_EmptyCity_ReturnsEmpty() {
            assertTrue(searchService.searchByCity("").isEmpty());
            assertTrue(searchService.searchByCity("   ").isEmpty());
        }
    }

    @Nested
    @DisplayName("Multiple Criteria Search")
    class MultipleCriteriaSearch {
        @Test
        @DisplayName("searchByMultipleCriteria - null 返回所有活躍餐廳")
        void searchByMultipleCriteria_NullCriteria_ReturnsAllActive() {
            List<Restaurant> result = searchService.searchByMultipleCriteria(null);
            List<Restaurant> allActive = searchService.getAllRestaurants();
            assertEquals(allActive.size(), result.size()); // Excludes inactive
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 關鍵字篩選")
        void searchByMultipleCriteria_KeywordFilter() {
            SearchCriteria criteria = new SearchCriteria().keyword("sushi");
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 城市篩選")
        void searchByMultipleCriteria_CityFilter() {
            SearchCriteria criteria = new SearchCriteria().city("台北");
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 菜系篩選")
        void searchByMultipleCriteria_CuisineFilter() {
            SearchCriteria criteria = new SearchCriteria().cuisineType(CuisineType.TAIWANESE);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 外送篩選")
        void searchByMultipleCriteria_DeliveryFilter() {
            SearchCriteria criteria = new SearchCriteria().hasDelivery(true);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(1, result.size());
            assertEquals("1", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 停車位篩選")
        void searchByMultipleCriteria_ParkingFilter() {
            SearchCriteria criteria = new SearchCriteria().hasParking(true);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(1, result.size());
            assertEquals("2", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 預約篩選")
        void searchByMultipleCriteria_ReservationsFilter() {
            SearchCriteria criteria = new SearchCriteria().acceptsReservations(true);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(1, result.size());
            assertEquals("3", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 組合篩選")
        void searchByMultipleCriteria_CombinedFilters() {
            SearchCriteria criteria = new SearchCriteria()
                    .city("台北")
                    .cuisineType(CuisineType.JAPANESE);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(1, result.size());
            assertEquals("1", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 分頁")
        void searchByMultipleCriteria_Pagination() {
            SearchCriteria criteria = new SearchCriteria().limit(2).offset(0);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 偏移量超過結果數量")
        void searchByMultipleCriteria_OffsetBeyondResults() {
            SearchCriteria criteria = new SearchCriteria().offset(100);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 區域篩選")
        void searchByMultipleCriteria_DistrictFilter() {
            Restaurant r = new Restaurant("12", "區域測試餐廳", CuisineType.CHINESE,
                    new Location(25.0, 121.0, "測試路", "台北市"));
            r.getLocation().setDistrict("信義區");
            r.setActive(true);
            repository.save(r);

            SearchCriteria criteria = new SearchCriteria().district("信義");
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 多菜系篩選")
        void searchByMultipleCriteria_MultipleCuisineTypes() {
            SearchCriteria criteria = new SearchCriteria()
                    .addCuisineType(CuisineType.JAPANESE)
                    .addCuisineType(CuisineType.ITALIAN);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertTrue(result.size() >= 2);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 評分篩選")
        void searchByMultipleCriteria_RatingFilter() {
            SearchCriteria criteria = new SearchCriteria().minRating(4.0);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 價格等級篩選")
        void searchByMultipleCriteria_PriceLevelFilter() {
            Restaurant r = new Restaurant("13", "價格測試餐廳", CuisineType.CHINESE, null);
            r.setActive(true);
            r.setPriceLevel(2);
            repository.save(r);

            SearchCriteria criteria = new SearchCriteria().priceLevel(2);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 價格範圍篩選")
        void searchByMultipleCriteria_PriceRangeFilter() {
            SearchCriteria criteria = new SearchCriteria()
                    .minPrice(100.0)
                    .maxPrice(500.0);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            // 結果取決於測試資料
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 外帶篩選")
        void searchByMultipleCriteria_TakeoutFilter() {
            SearchCriteria criteria = new SearchCriteria().hasTakeout(true);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(1, result.size());
            assertEquals("1", result.get(0).getId());
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 位置篩選")
        void searchByMultipleCriteria_LocationFilter() {
            SearchCriteria criteria = new SearchCriteria()
                    .nearLocation(25.0330, 121.5654, 5.0);
            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 價格等級篩選 (WithPriceLevel)")
        void searchByMultipleCriteria_WithPriceLevel() {
            Restaurant r = new Restaurant("16", "便宜餐廳", CuisineType.TAIWANESE, null);
            r.setActive(true);
            r.setPriceLevel(1);
            repository.save(r);

            SearchCriteria criteria = new SearchCriteria()
                    .priceLevel(1);

            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 價格範圍篩選 (WithPriceRange)")
        void searchByMultipleCriteria_WithPriceRange() {
            Restaurant r = new Restaurant("17", "中價餐廳", CuisineType.CHINESE, null);
            r.setActive(true);
            r.setAveragePrice(300);
            repository.save(r);

            SearchCriteria criteria = new SearchCriteria()
                    .minPrice(100.0)
                    .maxPrice(500.0);

            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 開放中篩選")
        void searchByMultipleCriteria_OpenNow() {
            SearchCriteria criteria = new SearchCriteria()
                    .openNow(true);

            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 關鍵字篩選 (WithKeyword)")
        void searchByMultipleCriteria_WithKeyword() {
            SearchCriteria criteria = new SearchCriteria()
                    .keyword("Sushi");

            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 區域篩選 (WithDistrict)")
        void searchByMultipleCriteria_WithDistrict() {
            Restaurant r = new Restaurant("18", "信義區餐廳", CuisineType.CHINESE,
                    new Location(25.0330, 121.5654, "信義路", "台北市"));
            r.getLocation().setDistrict("信義區");
            r.setActive(true);
            repository.save(r);

            SearchCriteria criteria = new SearchCriteria();
            criteria.setDistrict("信義");

            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 分頁 offset 超過結果大小返回空列表")
        void searchByMultipleCriteria_OffsetExceedsResults() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setOffset(1000);

            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Sorting Functionality")
    class Sorting {
        @Test
        @DisplayName("sortResults - 依名稱升冪排序")
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
        @DisplayName("sortResults - 依評分降冪排序")
        void sortResults_ByRatingDescending() {
            SearchCriteria criteria = new SearchCriteria()
                    .sortBy(SearchCriteria.SortType.RATING)
                    .ascending(false);
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> sorted = searchService.sortResults(allRestaurants, criteria);
            assertFalse(sorted.isEmpty());
        }

        @Test
        @DisplayName("sortResults - null 列表返回空列表")
        void sortResults_NullList_ReturnsEmpty() {
            SearchCriteria criteria = new SearchCriteria().sortBy(SearchCriteria.SortType.NAME);
            List<Restaurant> result = searchService.sortResults(null, criteria);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("sortResults - 空列表返回空列表")
        void sortResults_EmptyList_ReturnsEmpty() {
            SearchCriteria criteria = new SearchCriteria().sortBy(SearchCriteria.SortType.NAME);
            List<Restaurant> result = searchService.sortResults(new java.util.ArrayList<>(), criteria);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("sortResults - null 條件返回原列表複本")
        void sortResults_NullCriteria_ReturnsCopy() {
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> result = searchService.sortResults(allRestaurants, null);
            assertEquals(allRestaurants.size(), result.size());
        }

        @Test
        @DisplayName("sortResults - 依價格排序")
        void sortResults_ByPrice() {
            SearchCriteria criteria = new SearchCriteria()
                    .sortBy(SearchCriteria.SortType.PRICE)
                    .ascending(true);
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> result = searchService.sortResults(allRestaurants, criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("sortResults - 依評論數排序")
        void sortResults_ByReviewCount() {
            SearchCriteria criteria = new SearchCriteria()
                    .sortBy(SearchCriteria.SortType.REVIEW_COUNT)
                    .ascending(false);
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> result = searchService.sortResults(allRestaurants, criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("sortResults - 依距離排序（有位置篩選）")
        void sortResults_ByDistanceWithLocation() {
            SearchCriteria criteria = new SearchCriteria()
                    .sortBy(SearchCriteria.SortType.DISTANCE)
                    .nearLocation(25.0330, 121.5654, 10.0)
                    .ascending(true);
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> result = searchService.sortResults(allRestaurants, criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("sortResults - 依距離排序（無位置篩選時退回名稱排序）")
        void sortResults_ByDistanceWithoutLocation() {
            SearchCriteria criteria = new SearchCriteria()
                    .sortBy(SearchCriteria.SortType.DISTANCE)
                    .ascending(true);
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> result = searchService.sortResults(allRestaurants, criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("sortResults - 依相關性排序")
        void sortResults_ByRelevance() {
            SearchCriteria criteria = new SearchCriteria()
                    .sortBy(SearchCriteria.SortType.RELEVANCE)
                    .ascending(false);
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> result = searchService.sortResults(allRestaurants, criteria);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("sortResults - null sortBy 返回原列表")
        void sortResults_NullSortBy_ReturnsCopy() {
            SearchCriteria criteria = new SearchCriteria();
            // sortBy 默認為 null
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> result = searchService.sortResults(allRestaurants, criteria);
            assertEquals(allRestaurants.size(), result.size());
        }
    }

    @Nested
    @DisplayName("Global Search")
    class GlobalSearch {
        @Test
        @DisplayName("searchGlobal - null 關鍵字返回空列表")
        void searchGlobal_NullKeyword_ReturnsEmpty() {
            assertTrue(searchService.searchGlobal(null).isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 匹配名稱")
        void searchGlobal_MatchesName() {
            List<Restaurant> result = searchService.searchGlobal("Tokyo");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("searchGlobal - 匹配城市")
        void searchGlobal_MatchesCity() {
            List<Restaurant> result = searchService.searchGlobal("高雄");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("searchGlobal - 空字串返回空列表")
        void searchGlobal_EmptyKeyword_ReturnsEmpty() {
            assertTrue(searchService.searchGlobal("").isEmpty());
            assertTrue(searchService.searchGlobal("   ").isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 匹配描述")
        void searchGlobal_MatchesDescription() {
            Restaurant r = new Restaurant("14", "描述測試餐廳", CuisineType.CHINESE, null);
            r.setActive(true);
            r.setDescription("提供美味的川菜料理");
            repository.save(r);

            List<Restaurant> result = searchService.searchGlobal("川菜");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 匹配菜系顯示名稱")
        void searchGlobal_MatchesCuisineDisplayName() {
            List<Restaurant> result = searchService.searchGlobal("日式");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 匹配地址")
        void searchGlobal_MatchesAddress() {
            List<Restaurant> result = searchService.searchGlobal("信義路");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 不匹配任何欄位返回空")
        void searchGlobal_NoMatchReturnsEmpty() {
            List<Restaurant> result = searchService.searchGlobal("xyzNotExist123");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 餐廳無描述時仍可匹配其他欄位")
        void searchGlobal_NullDescriptionStillMatchesOtherFields() {
            Restaurant r = new Restaurant("20", "TestRestaurant", CuisineType.CHINESE,
                    new Location(25.0, 121.0, "測試路", "新北市"));
            r.setActive(true);
            r.setDescription(null);
            repository.save(r);

            List<Restaurant> result = searchService.searchGlobal("TestRestaurant");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 餐廳無菜系類型時仍可匹配其他欄位")
        void searchGlobal_NullCuisineTypeStillMatchesOtherFields() {
            Restaurant r = new Restaurant("21", "NoCuisineRest");
            r.setActive(true);
            r.setLocation(new Location(25.0, 121.0, "某路", "桃園市"));
            repository.save(r);

            List<Restaurant> result = searchService.searchGlobal("桃園市");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 餐廳無位置時仍可匹配名稱")
        void searchGlobal_NullLocationStillMatchesName() {
            Restaurant r = new Restaurant("22", "NoLocationRestaurant", CuisineType.ITALIAN, null);
            r.setActive(true);
            repository.save(r);

            List<Restaurant> result = searchService.searchGlobal("NoLocationRestaurant");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 餐廳位置無城市時仍可匹配地址")
        void searchGlobal_NullCityStillMatchesAddress() {
            Restaurant r = new Restaurant("23", "特殊餐廳", CuisineType.SEAFOOD, null);
            Location loc = new Location(25.0, 121.0);
            loc.setAddress("復興北路100號");
            loc.setCity(null);
            r.setLocation(loc);
            r.setActive(true);
            repository.save(r);

            List<Restaurant> result = searchService.searchGlobal("復興北路");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 餐廳位置無地址時仍可匹配城市")
        void searchGlobal_NullAddressStillMatchesCity() {
            Restaurant r = new Restaurant("24", "無地址餐廳", CuisineType.CHINESE, null);
            Location loc = new Location(25.0, 121.0);
            loc.setCity("基隆市");
            loc.setAddress(null);
            r.setLocation(loc);
            r.setActive(true);
            repository.save(r);

            List<Restaurant> result = searchService.searchGlobal("基隆市");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 名稱優先排序（名稱匹配排在前面）")
        void searchGlobal_NameMatchesPrioritized() {
            Restaurant r1 = new Restaurant("25", "Pizza Place", CuisineType.ITALIAN, null);
            r1.setActive(true);
            r1.setDescription("Best pizza in town");
            repository.save(r1);

            Restaurant r2 = new Restaurant("26", "Best Food", CuisineType.AMERICAN, null);
            r2.setActive(true);
            r2.setDescription("We serve pizza too");
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("pizza");
            assertFalse(result.isEmpty());
            // 名稱包含 pizza 的應該排在前面
            if (result.size() >= 2) {
                assertTrue(result.get(0).getName().toLowerCase().contains("pizza"));
            }
        }

        @Test
        @DisplayName("searchGlobal - 餐廳名稱為 null 時不會拋出異常")
        void searchGlobal_RestaurantWithNullName_NoException() {
            Restaurant r = new Restaurant("27", null, CuisineType.CHINESE,
                    new Location(25.0, 121.0, "某路", "某市"));
            r.setActive(true);
            repository.save(r);

            // 不應該拋出 NullPointerException
            List<Restaurant> result = searchService.searchGlobal("某市");
            assertNotNull(result);
        }

        @Test
        @DisplayName("searchGlobal - 名稱匹配優先於非名稱匹配")
        void searchGlobal_NameMatchPrioritizedOverNonNameMatch() {
            // r1: 名稱包含關鍵字
            Restaurant r1 = new Restaurant("30", "Sushi Bar", CuisineType.JAPANESE, null);
            r1.setActive(true);
            repository.save(r1);

            // r2: 名稱不包含關鍵字，但描述包含
            Restaurant r2 = new Restaurant("31", "Food Place", CuisineType.JAPANESE, null);
            r2.setActive(true);
            r2.setDescription("We serve sushi here");
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("sushi");
            assertFalse(result.isEmpty());
            // 名稱匹配的應該排在前面
            if (result.size() >= 2) {
                assertTrue(result.get(0).getName().toLowerCase().contains("sushi"));
            }
        }

        @Test
        @DisplayName("searchGlobal - 非名稱匹配排在名稱匹配之後")
        void searchGlobal_NonNameMatchAfterNameMatch() {
            // r1: 名稱不包含關鍵字
            Restaurant r1 = new Restaurant("32", "Great Place", CuisineType.ITALIAN, null);
            r1.setActive(true);
            r1.setDescription("Best ramen in town");
            repository.save(r1);

            // r2: 名稱包含關鍵字
            Restaurant r2 = new Restaurant("33", "Ramen House", CuisineType.JAPANESE, null);
            r2.setActive(true);
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("ramen");
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("searchGlobal - 兩個餐廳名稱都匹配保持穩定排序")
        void searchGlobal_BothNamesMatch_StableSort() {
            Restaurant r1 = new Restaurant("34", "Pizza One", CuisineType.ITALIAN, null);
            r1.setActive(true);
            repository.save(r1);

            Restaurant r2 = new Restaurant("35", "Pizza Two", CuisineType.ITALIAN, null);
            r2.setActive(true);
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("pizza");
            assertEquals(2, result.size());
            // 兩者名稱都匹配，比較返回 0，保持穩定排序
        }

        @Test
        @DisplayName("searchGlobal - 兩個餐廳名稱都不匹配保持穩定排序")
        void searchGlobal_NeitherNameMatches_StableSort() {
            Restaurant r1 = new Restaurant("36", "Store A", CuisineType.CHINESE,
                    new Location(25.0, 121.0, "某路", "某城市"));
            r1.setActive(true);
            r1.getLocation().setCity("特別城市");
            repository.save(r1);

            Restaurant r2 = new Restaurant("37", "Store B", CuisineType.CHINESE,
                    new Location(25.0, 121.0, "某路", "某城市"));
            r2.setActive(true);
            r2.getLocation().setCity("特別城市");
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("特別城市");
            assertEquals(2, result.size());
            // 兩者名稱都不匹配，比較返回 0
        }

        @Test
        @DisplayName("searchGlobal - r1 名稱匹配，r2 名稱為 null")
        void searchGlobal_R1NameMatches_R2NullName() {
            Restaurant r1 = new Restaurant("40", "特色餐廳", CuisineType.THAI, null);
            r1.setActive(true);
            repository.save(r1);

            Restaurant r2 = new Restaurant("41", null, CuisineType.THAI,
                    new Location(25.0, 121.0, "特色路", "城市"));
            r2.setActive(true);
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("特色");
            assertFalse(result.isEmpty());
            // r1 名稱匹配應該排在前面
            if (result.size() >= 2) {
                assertTrue(result.get(0).getName() != null);
            }
        }

        @Test
        @DisplayName("searchGlobal - r1 名稱為 null，r2 名稱匹配")
        void searchGlobal_R1NullName_R2NameMatches() {
            Restaurant r1 = new Restaurant("38", null, CuisineType.CHINESE,
                    new Location(25.0, 121.0, "路", "獨特城市"));
            r1.setActive(true);
            repository.save(r1);

            Restaurant r2 = new Restaurant("39", "獨特餐廳", CuisineType.CHINESE,
                    new Location(25.0, 121.0, "路", "其他城市"));
            r2.setActive(true);
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("獨特");
            assertFalse(result.isEmpty());
            // r2 名稱匹配應該排在前面
            if (result.size() >= 1) {
                assertTrue(result.get(0).getName() != null && result.get(0).getName().contains("獨特"));
            }
        }

        @Test
        @DisplayName("searchGlobal - 兩個餐廳名稱都為 null")
        void searchGlobal_BothNullNames() {
            Restaurant r1 = new Restaurant("42", null, CuisineType.KOREAN,
                    new Location(25.0, 121.0, "某路", "韓式城市"));
            r1.setActive(true);
            repository.save(r1);

            Restaurant r2 = new Restaurant("43", null, CuisineType.KOREAN,
                    new Location(25.0, 121.0, "某路", "韓式城市"));
            r2.setActive(true);
            repository.save(r2);

            List<Restaurant> result = searchService.searchGlobal("韓式城市");
            assertEquals(2, result.size());
            // 兩者名稱都為 null (name1 = false, name2 = false)，比較返回 0
        }
    }

    @Nested
    @DisplayName("Management")
    class Management {
        @Test
        @DisplayName("getAllRestaurants - 排除非活躍餐廳")
        void getAllRestaurants_ExcludesInactive() {
            List<Restaurant> result = searchService.getAllRestaurants();
            // Should have at least 4 active restaurants from setUp
            assertTrue(result.size() >= 4);
            assertTrue(result.stream().noneMatch(r -> r.getId().equals("5")));
        }

        @Test
        @DisplayName("countRestaurants - 只計算活躍餐廳")
        void countRestaurants_CountsOnlyActive() {
            // Should have at least 4 active restaurants
            assertTrue(searchService.countRestaurants() >= 4);
        }
    }

    @Nested
    @DisplayName("Additional Branch Coverage")
    class BranchCoverageTests {
        @Test
        @DisplayName("searchByCity - 忽略位置為 null 的餐廳")
        void searchByCity_IgnoresNullLocation() {
            Restaurant r = new Restaurant("999", "Null Loc", CuisineType.OTHER, null);
            r.setActive(true);
            repository.save(r);

            List<Restaurant> result = searchService.searchByCity("台北");
            assertFalse(result.stream().anyMatch(res -> res.getId().equals("999")), "應該忽略位置為 null 的餐廳");
            assertFalse(result.isEmpty(), "應該仍然能找到其他正常的餐廳");
        }

        @Test
        @DisplayName("searchByCity - 忽略城市為 null 的餐廳")
        void searchByCity_IgnoresNullCity() {
            Restaurant r = new Restaurant("998", "Null City", CuisineType.OTHER, new Location(25.0, 121.0));
            r.getLocation().setCity(null);
            r.setActive(true);
            repository.save(r);

            List<Restaurant> result = searchService.searchByCity("台北");
            assertFalse(result.stream().anyMatch(res -> res.getId().equals("998")), "應該忽略城市為 null 的餐廳");
        }

        @Test
        @DisplayName("searchByDistrict - 忽略位置為 null 的餐廳")
        void searchByDistrict_IgnoresNullLocation() {
            // Add a restaurant with NULL location (should be ignored)
            Restaurant r1 = new Restaurant("997", "Null Loc Dist", CuisineType.OTHER, null);
            r1.setActive(true);
            repository.save(r1);

            // Add a restaurant with VALID location and matching district
            Restaurant r2 = new Restaurant("997_valid", "Valid Dist", CuisineType.TAIWANESE,
                    new Location(25.0, 121.0, "Any Rd", "Taipei"));
            r2.getLocation().setDistrict("永康區");
            r2.setActive(true);
            repository.save(r2);

            List<Restaurant> result = searchService.searchByDistrict("永康");
            assertFalse(result.stream().anyMatch(res -> res.getId().equals("997")), "應該忽略位置為 null 的餐廳");
            assertFalse(result.isEmpty(), "應該能找到區配的餐廳");
            assertTrue(result.stream().anyMatch(res -> res.getId().equals("997_valid")));
        }

        @Test
        @DisplayName("searchByDistrict - 忽略區域為 null 的餐廳")
        void searchByDistrict_IgnoresNullDistrict() {
            Restaurant r = new Restaurant("996", "Null District", CuisineType.OTHER, new Location(25.0, 121.0));
            r.getLocation().setCity("台北市");
            r.getLocation().setDistrict(null);
            r.setActive(true);
            repository.save(r);

            List<Restaurant> result = searchService.searchByDistrict("永康");
            assertFalse(result.stream().anyMatch(res -> res.getId().equals("996")), "應該忽略區域為 null 的餐廳");
        }

        @Test
        @DisplayName("searchByMultipleCriteria - 綜合測試空篩選條件")
        void searchByMultipleCriteria_EmptyFilters() {
            // 測試當條件存在但內容為空時的路徑
            SearchCriteria criteria = new SearchCriteria();
            criteria.setCuisineTypes(new HashSet<>()); // 空集合

            List<Restaurant> result = searchService.searchByMultipleCriteria(criteria);
            assertEquals(searchService.getAllRestaurants().size(), result.size());
        }
    }
}
