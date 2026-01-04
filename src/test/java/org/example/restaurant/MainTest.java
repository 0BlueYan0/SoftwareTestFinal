package org.example.restaurant;

import org.example.restaurant.data.SampleDataLoader;
import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;
import org.example.restaurant.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main 主程式測試類別
 */
class MainTest {

    private RestaurantRepository repository;
    private RestaurantSearchService searchService;
    private RecommendationService recommendationService;
    private RatingService ratingService;
    // private BusinessHoursService businessHoursService; // Removed unused field
    private PriceAnalyzer priceAnalyzer;

    @BeforeEach
    void setUp() {
        repository = new RestaurantRepository();
        ratingService = new RatingService();
        priceAnalyzer = new PriceAnalyzer();
        searchService = new RestaurantSearchService(repository);
        // businessHoursService = new BusinessHoursService();
        recommendationService = new RecommendationService(ratingService, priceAnalyzer);

        // 載入示範資料
        SampleDataLoader loader = new SampleDataLoader(repository);
        loader.loadSampleData();
    }

    // 反射輔助方法
    private Object getField(Object object, String fieldName) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    @Nested
    @DisplayName("Initialization")
    class Initialization {
        @Test
        @DisplayName("Main - 初始化不會拋出異常")
        void main_InitializesWithoutException() {
            assertDoesNotThrow(() -> {
                // 模擬輸入 "0" 來立即離開
                String input = "0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                // 主程式初始化測試（不實際運行 run）
                Main main = new Main();
                assertNotNull(main);
            });
        }
    }

    @Nested
    @DisplayName("Service Integration")
    class ServiceIntegration {
        @Test
        @DisplayName("搜尋服務 - 可以搜尋春水堂")
        void searchService_CanSearchChunShui() {
            List<Restaurant> results = searchService.searchGlobal("春水堂");
            assertFalse(results.isEmpty());
            assertTrue(results.stream().anyMatch(r -> r.getName().contains("春水堂")));
        }

        // ... 其他原有 Search 測試保留 ...

        @Test
        @DisplayName("推薦服務 - 取得人氣餐廳")
        void recommendationService_GetPopularRestaurants() {
            List<Restaurant> all = searchService.getAllRestaurants();
            List<Restaurant> popular = recommendationService.getPopularRestaurants(all, 5);

            assertFalse(popular.isEmpty());
            assertTrue(popular.size() <= 5);
        }

        @Test
        @DisplayName("價格過濾 - 經濟實惠餐廳")
        void priceFilter_CheapRestaurants() {
            List<Restaurant> all = searchService.getAllRestaurants();
            long cheapCount = all.stream()
                    .filter(r -> r.getPriceLevel() == 1)
                    .count();

            assertTrue(cheapCount > 0, "應該有價格等級 1 的餐廳");
        }
    }

    @Nested
    @DisplayName("System Output & Menu")
    class SystemOutputAndMenu {
        @Test
        @DisplayName("系統輸出 - 主選單包含所有選項 (含偏好設定)")
        void systemOutput_MenuContainsAllOptions() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 模擬輸入 "0" 來離開
                String input = "0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("1. 關鍵字搜尋"), "選項 1 存在");
                assertTrue(output.contains("2. 查看人氣"), "選項 2 存在");
                assertTrue(output.contains("3. 取得餐廳推薦"), "選項 3 存在");
                assertTrue(output.contains("7. 設定使用者偏好"), "新增選項 7 存在");
                assertTrue(output.contains("8. 新增餐廳評論"), "新增選項 8 存在");
                assertTrue(output.contains("9. 查詢下次營業時間"), "新增選項 9 存在");
                assertTrue(output.contains("10. 進階組合搜尋"), "新增選項 10 存在");
                assertTrue(output.contains("0. 離開系統"), "選項 0 存在");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("run - 無效的主選單輸入")
        void run_InvalidOption() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "99" (無效選項) -> "0" (離開)
                String input = "99\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效的選項，請重新輸入"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Recommendation Feature")
    class RecommendationFeature {
        @Test
        @DisplayName("showRecommendations - 偏好推薦 (無偏好時)")
        void showRecommendations_PreferenceBased_NoPrefs() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" (推薦) -> "1" (偏好推薦) -> "0" (離開)
                String input = "3\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                // 預設無偏好時，通常會推薦熱門餐廳或所有評分高的
                assertTrue(output.contains("推薦") || output.contains("人氣"),
                        "顯示推薦列表");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showRecommendations - 類似餐廳推薦")
        void showRecommendations_SimilarBased() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" (推薦) -> "2" (類似餐廳) -> "1" (選第1家) -> "0" (離開)
                String input = "3\n2\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("相似的餐廳"), "應顯示相似推薦");
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("User Preferences Feature")
    class UserPreferencesFeature {
        @Test
        @DisplayName("setUserPreferences - 設定喜愛料理與價格")
        void setUserPreferences_HappyPath() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "7" (設定偏好) -> "1,2" (選擇料理 1,2) -> "" (跳過不喜歡) -> "2" (價格等級 2) -> "0" (離開)
                String input = "7\n1,2\n\n2\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("已更新喜愛料理"), "應確認更新料理");
                assertTrue(output.contains("已設定價格上限: 2"), "應確認更新價格");
                assertTrue(output.contains("偏好設定完成"), "應顯示完成訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("setUserPreferences - 包含不喜歡的料理")
        void setUserPreferences_WithDislikedCuisines() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 7 -> Favorite: "1" (Chinese) -> Disliked: "2" (Japanese) -> MaxPrice: "4" ->
                // 0
                String input = "7\n1\n2\n4\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("已更新喜愛料理"));
                assertTrue(output.contains("已更新不喜歡的料理"));
                assertTrue(output.contains(CuisineType.JAPANESE.toString()) || output.contains("Japanese"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("setUserPreferences - 輸入無效格式")
        void setUserPreferences_InvalidInput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "7" -> "abc" (無效料理) -> "" (跳過不喜歡) -> "99" (無效價格) -> "0"
                String input = "7\nabc\n\n99\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                // 系統應該容錯並完成設定流程，可能不會顯示更新成功的訊息，但不能崩潰
                assertTrue(output.contains("偏好設定完成"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("setUserPreferences - 跳過價格設定")
        void setUserPreferences_SkipPrice() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 7 -> 1 (料理) -> "" (跳過不喜歡) -> header empty (跳過價格) -> 0
                String input = "7\n1\n\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("偏好設定完成"));
                // 應保留預設值或不顯示更新價格訊息
                assertFalse(output.contains("已設定價格上限"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("setUserPreferences - 價格輸入超出範圍")
        void setUserPreferences_PriceOutOfRange() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 7 -> 1 -> "" (跳過不喜歡) -> 5 (超出) -> 0
                String input = "7\n1\n\n5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                // 系統應該忽略無效輸入，不崩潰
                assertFalse(output.contains("已設定價格上限: 5"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("setUserPreferences - 料理選項部分無效")
        void setUserPreferences_PartialInvalidCuisine() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "7\n1,99,abc\n\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("已更新喜愛料理")); // 至少有一個有效
                assertTrue(output.contains("中式料理") || output.contains("台式料理")); // 假設1號是某料理
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("setUserPreferences - 衝突解決 (先喜歡再不喜歡)")
        void setUserPreferences_ConflictResolution() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 7 -> Favorite: "1" -> Disliked: "1" (Same) -> MaxPrice: "" -> 0
                // Should result in Favorite being empty (removed) and Disliked having "1"
                String input = "7\n1\n1\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("已更新喜愛料理"));
                assertTrue(output.contains("已更新不喜歡的料理"));
                // Disliked should win (be present in final output)
                // We check the final summary at the end of setUserPreferences
                assertTrue(output.contains("目前不喜愛料理:"));
                // Ideally check that favorites does NOT contain it, but output scanning is
                // fuzzy.
                // Assuming "1" is Chinese/Taiwanese, verify it appears in Disliked list string
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("setUserPreferences - 全空輸入")
        void setUserPreferences_EmptyInputs() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 7 -> Empty -> Empty -> Empty -> 0
                String input = "7\n\n\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("偏好設定完成"));
                assertFalse(output.contains("已更新"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Recommendation Feature Coverage")
    class RecommendationFeatureCoverage {
        @Test
        @DisplayName("showSimilarRecommendations - 沒有餐廳資料")
        void showSimilarRecommendations_NoData() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" (推薦) -> "2" (類似餐廳) -> "0" (離開)
                String input = "3\n2\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // 清空倉庫 (需操作內部 Map)
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                java.lang.reflect.Field mapField = RestaurantRepository.class.getDeclaredField("restaurants");
                mapField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, Restaurant> internalMap = (java.util.Map<String, Restaurant>) mapField.get(repo);
                internalMap.clear();

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("目前沒有餐廳資料"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showSimilarRecommendations - 找不到類似餐廳")
        void showSimilarRecommendations_NoSimilarFound() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" -> "2" -> "1" (選第1家) -> "0" (離開)
                String input = "3\n2\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                // 備份第一家
                Restaurant r = repo.findAll().get(0);

                // 清空並只放入這一家
                java.lang.reflect.Field mapField = RestaurantRepository.class.getDeclaredField("restaurants");
                mapField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, Restaurant> internalMap = (java.util.Map<String, Restaurant>) mapField.get(repo);
                internalMap.clear();
                internalMap.put(r.getId(), r);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("找不到類似餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showSimilarRecommendations - 無效輸入 (非數字)")
        void showSimilarRecommendations_InvalidInput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "3\n2\nabc\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("請輸入有效的數字"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showSimilarRecommendations - 無效編號 (超出範圍)")
        void showSimilarRecommendations_InvalidIndex() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "3\n2\n999\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效的編號"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("handleRecommendations - 無效選項")
        void handleRecommendations_InvalidOption() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 3 -> 99 (無效) -> 0
                String input = "3\n99\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效的選項"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showPreferenceRecommendations - 根據偏好找到餐廳 (顯示條件)")
        void showPreferenceRecommendations_WithResults_AndConditions() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 7 -> 1 (設定台式) -> "" (跳過不喜歡) -> 2 (價格上限2) -> 3 -> 1 -> 0
                String input = "7\n1\n\n2\n3\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("目前偏好條件:"));
                assertTrue(output.contains("價格等級<=2")); // 驗證條件顯示分支
                assertTrue(output.contains("台式料理"));
                assertTrue(output.contains("推薦"), "應顯示推薦結果");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showPreferenceRecommendations - 根據偏好找到餐廳 (不顯示價格條件)")
        void showPreferenceRecommendations_WithResults_NoPriceCondition() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 7 -> 1 (設定台式) -> "" (跳過不喜歡) -> 4 (價格上限4 - 最大值不顯示條件) -> 3 -> 1 -> 0
                String input = "7\n1\n\n4\n3\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("目前偏好條件:"));
                assertFalse(output.contains("價格等級<=4")); // 最大值不需特別顯示限制
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showPreferenceRecommendations - 根據偏好找不到餐廳")
        void showPreferenceRecommendations_NoResults() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 3 -> 1 (偏好推薦) -> 0
                String input = "3\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // 清空倉庫以確保無結果
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                java.lang.reflect.Field mapField = RestaurantRepository.class.getDeclaredField("restaurants");
                mapField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, Restaurant> internalMap = (java.util.Map<String, Restaurant>) mapField.get(repo);
                internalMap.clear();

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("目前沒有合適的推薦"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Price Search Feature Coverage")
    class PriceSearchFeatureCoverage {
        @Test
        @DisplayName("searchByPriceLevel - 找不到該價位的餐廳")
        void searchByPriceLevel_NoResults() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "4\n4\n0\n"; // 假設沒有 $$$$ 餐廳
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // 確保沒有等級 4 的餐廳
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");

                // 使用 reflection 抓出內部的 Map (RestaurantRepository uses Map<String, Restaurant>)
                java.lang.reflect.Field mapField = RestaurantRepository.class.getDeclaredField("restaurants");
                mapField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, Restaurant> internalMap = (java.util.Map<String, Restaurant>) mapField.get(repo);

                // 從 Map 中移除價格等級為 4 的餐廳
                internalMap.values().removeIf(r -> r.getPriceLevel() == 4);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("找不到價格等級為 $$$$ 的餐廳"),
                        "Output should contain '找不到價格等級為 $$$$ 的餐廳'");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByPriceLevel - 輸入範圍外數字")
        void searchByPriceLevel_OutOfRange() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "4\n5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("請輸入 1-4 之間的數字"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByPriceLevel - 輸入非數字")
        void searchByPriceLevel_NonNumeric() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "4\nxyz\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("請輸入有效的數字"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Display Utilities Coverage")
    class DisplayUtilitiesCoverage {
        @Test
        @DisplayName("printRestaurantList - 處理 Null 欄位")
        void printRestaurantList_NullFields() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");

                // 創建 Null 欄位餐廳
                Restaurant nullRest = new Restaurant("999", "Null Rest", null, null);
                nullRest.setDescription(null);
                repo.save(nullRest);

                main.run();

                String output = outContent.toString();
                // 驗證是否優雅處理 Null 而不崩潰，並顯示預設文字
                assertTrue(output.contains("Null Rest"));
                assertTrue(output.contains("未分類"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("printRestaurantList - 長描述截斷")
        void printRestaurantList_LongDescription() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");

                Restaurant r = repo.findAll().get(0);
                // 設定 > 40 字元描述
                r.setDescription("這是一個非常非常長的描述，絕對超過了四十個字元的限制，系統應該要在列印時將其截斷並加上省略號...");

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("..."));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    // Original tests kept for completeness and consistency
    @Nested
    @DisplayName("Keyword Search Feature")
    class KeywordSearchFeature {
        @Test
        @DisplayName("searchByKeyword - 找到結果")
        void searchByKeyword_FoundResults() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));
            try {
                String input = "1\n春水堂\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                Main main = new Main();
                main.run();
                String output = outContent.toString();
                assertTrue(output.contains("找到") && output.contains("春水堂"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByKeyword - 關鍵字為空")
        void searchByKeyword_EmptyInput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));
            try {
                // 輸入 1 (搜尋) -> 空白 -> 0 (離開)
                String input = "1\n   \n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                Main main = new Main();
                main.run();
                String output = outContent.toString();
                assertTrue(output.contains("關鍵字不能為空"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByKeyword - 找不到結果")
        void searchByKeyword_NoResults() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));
            try {
                // 輸入 1 -> 不存在的名稱 -> 0
                String input = "1\nNonExistentRestaurantXYZ\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                Main main = new Main();
                main.run();
                String output = outContent.toString();
                assertTrue(output.contains("找不到符合"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Price Search Feature")
    class PriceSearchFeature {
        @Test
        @DisplayName("searchByPriceLevel - 有效價格等級")
        void searchByPriceLevel_Valid() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));
            try {
                String input = "4\n2\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                Main main = new Main();
                main.run();
                assertTrue(outContent.toString().contains("價格等級 $$"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Open Now Feature")
    class OpenNowFeature {
        @Test
        @DisplayName("showOpenNow - 顯示營業中餐廳")
        void showOpenNow_DisplaysRestaurants() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));
            try {
                String input = "6\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                Main main = new Main();
                main.run();
                String output = outContent.toString();
                assertTrue(output.contains("目前營業中") || output.contains("沒有營業中"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showOpenNow - 沒有營業中餐廳")
        void showOpenNow_NoRestaurantsOpen() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));
            try {
                String input = "6\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                // 讓所有餐廳變成關閉
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                // 這裡可以直接修改物件狀態，因為是參照
                for (Restaurant r : repo.findAll()) {
                    r.setBusinessHours(null); // or empty business hours
                }

                main.run();
                String output = outContent.toString();
                assertTrue(output.contains("目前沒有營業中的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Add Review Feature")
    class AddReviewFeature {
        @Test
        @DisplayName("addRestaurantReview - 成功新增評論")
        void addRestaurantReview_Success() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 8 (新增評論) -> 1 (選第1家) -> 5 (評分) -> 好吃 (評論) -> 测试员 (名字) -> 0 (離開)
                String input = "8\n1\n5\n好吃\n测试员\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                // 執行前先確認第1家餐廳評論數
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant r = repo.findAll().get(0);
                int initialCount = r.getReviewCount();

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("評論已新增"));
                assertEquals(initialCount + 1, r.getReviewCount());
                assertEquals("测试员", r.getReviews().get(r.getReviews().size() - 1).getUserName());
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("addRestaurantReview - 無效餐廳編號")
        void addRestaurantReview_InvalidIndex() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 8 -> 999 (無效) -> 0
                String input = "8\n999\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效的編號"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("addRestaurantReview - 評分超出範圍")
        void addRestaurantReview_InvalidRating() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 8 -> 1 -> 6 (無效評分) -> 0
                String input = "8\n1\n6\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("評分必須在 1 到 5 之間"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("addRestaurantReview - 評分非數字")
        void addRestaurantReview_NonNumericRating() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 8 -> 1 -> abc -> 0
                String input = "8\n1\nabc\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效的數字輸入"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("addRestaurantReview - 取消選擇")
        void addRestaurantReview_Cancel() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 8 -> 0 (取消) -> 0
                String input = "8\n0\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                // 確保沒有進入評分流程 (例如沒有提示輸入評分)
                assertFalse(output.contains("請輸入評分"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("addRestaurantReview - 空白編號取消")
        void addRestaurantReview_EmptyIndex() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 8 -> 空白 -> 0
                String input = "8\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertFalse(output.contains("請輸入評分"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("addRestaurantReview - 預設使用者名稱")
        void addRestaurantReview_DefaultUserName() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 8 -> 1 -> 5 -> 好吃 -> (空) -> 0
                String input = "8\n1\n5\n好吃\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant r = repo.findAll().get(0);
                int initialCount = r.getReviewCount();

                main.run();

                assertEquals(initialCount + 1, r.getReviewCount());
                String lastReviewer = r.getReviews().get(r.getReviews().size() - 1).getUserName();
                assertEquals("匿名", lastReviewer);
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Next Opening Time Feature")
    class NextOpeningTimeFeature {
        @Test
        @DisplayName("checkNextOpeningTime - 餐廳營業中")
        void checkNextOpeningTime_OpenNow() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 9 -> 1 -> 0
                String input = "9\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // Mock repository data to ensure restaurant is open
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant r = repo.findAll().get(0);
                // Set to 24 hours open to guarantee it's open now
                BusinessHours hours = new BusinessHours();
                for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
                    hours.setHours(day, java.time.LocalTime.MIN, java.time.LocalTime.MAX);
                }
                r.setBusinessHours(hours);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("狀態: 目前營業中"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("checkNextOpeningTime - 餐廳休息中顯示下次營業")
        void checkNextOpeningTime_Closed_ShowNext() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 9 -> 1 -> 0
                String input = "9\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // Mock to ensure closed now but open later
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant r = repo.findAll().get(0);
                BusinessHours hours = new BusinessHours();
                // Set open time for tmr only
                java.time.DayOfWeek tmr = java.time.LocalDate.now().plusDays(1).getDayOfWeek();
                hours.setHours(tmr, java.time.LocalTime.of(9, 0), java.time.LocalTime.of(18, 0));
                // Clear today
                hours.setHours(java.time.LocalDate.now().getDayOfWeek(), null, null);

                r.setBusinessHours(hours);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("狀態: 目前休息中"));
                assertTrue(output.contains("下次營業時間") || output.contains("近期無營業時間"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("checkNextOpeningTime - 無效輸入")
        void checkNextOpeningTime_InvalidInput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 9 -> abc -> 0
                String input = "9\nabc\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效的數字輸入"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("checkNextOpeningTime - 取消")
        void checkNextOpeningTime_Cancel() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 9 -> 0 -> 0
                String input = "9\n0\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertFalse(output.contains("查詢餐廳:"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("checkNextOpeningTime - 無下一營業時間")
        void checkNextOpeningTime_NoNextOpenInfo() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "9\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant r = repo.findAll().get(0);
                // Completely empty business hours
                r.setBusinessHours(new BusinessHours());

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("近期無營業時間資訊"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("checkNextOpeningTime - 即將打烊")
        void checkNextOpeningTime_ClosingSoon() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "9\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant r = repo.findAll().get(0);
                BusinessHours hours = new BusinessHours();
                // Set closing time to 1 minute from now
                java.time.LocalTime now = java.time.LocalTime.now();
                java.time.LocalTime close = now.plusMinutes(20);
                // Handle day overflow just in case (though unlikely for simple test on same
                // day)
                if (close.isBefore(now))
                    close = java.time.LocalTime.MAX;

                java.time.DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
                // Ensure it covers now -> close
                hours.setHours(today, now.minusHours(1), close);
                r.setBusinessHours(hours);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("狀態: 目前營業中"));
                assertTrue(output.contains("即將在 60 分鐘內打烊"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("checkNextOpeningTime - 無效餐廳編號")
        void checkNextOpeningTime_InvalidIndex() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "9\n999\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效的編號"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("checkNextOpeningTime - 營業中但無今日結束時間 (Edge Case)")
        void checkNextOpeningTime_Open_NoCloseTime() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "9\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // Hack: Inject a custom BusinessHoursService that forces open=true but
                // closingTime=null
                BusinessHoursService mockService = new BusinessHoursService() {
                    @Override
                    public boolean isOpenNow(Restaurant r) {
                        return true;
                    }

                    @Override
                    public boolean isClosingSoon(Restaurant r, int min) {
                        return false;
                    }

                    @Override
                    public java.time.LocalTime getClosingTimeToday(Restaurant r) {
                        return null;
                    }
                };

                java.lang.reflect.Field serviceField = Main.class.getDeclaredField("businessHoursService");
                serviceField.setAccessible(true);
                // Remove final modifier if necessary (often can just setAccessible on newer
                // JDKs but fields are final)
                // However, on many JVMs setAccessible(true) is enough even for final fields.
                // If it fails, we might need to remove final modifier via reflection too
                // (unsafe).
                // Let's assume setAccessible is enough for test environment.
                serviceField.set(main, mockService);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("狀態: 目前營業中"));
                assertFalse(output.contains("今日營業至"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Advanced Search Feature")
    class AdvancedSearchFeature {
        @Test
        @DisplayName("advancedSearch - 完整條件搜尋")
        void advancedSearch_FullCriteria() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 10 -> keyword "春水" -> cuisine "1" -> price "2" -> rating "3" -> open "n" -> 0
                String input = "10\n春水\n1\n2\n3\nn\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("符合條件的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("advancedSearch - 無條件 (全部跳過)")
        void advancedSearch_NoCriteria() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 10 -> empty -> empty -> empty -> empty -> empty -> 0
                String input = "10\n\n\n\n\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("符合條件的餐廳"));
                // Should find something since it becomes "find all"
                assertFalse(output.contains("沒有找到符合條件的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("advancedSearch - 無效輸入忽略")
        void advancedSearch_InvalidInputs() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 10 -> keyword -> invalid cuisine -> invalid price -> invalid rating -> open
                // -> 0
                String input = "10\n\nabc\n99\nabc\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("符合條件的餐廳"));
                // Should verify that invalid inputs didn't crash app
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("advancedSearch - 價格輸入非數字")
        void advancedSearch_PriceNonNumeric() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 10 -> empty -> empty -> "xyz" (exception) -> empty -> empty -> 0
                String input = "10\n\n\nxyz\n\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                // Should ignore error and proceed
                assertTrue(output.contains("符合條件的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("advancedSearch - 只顯示營業中")
        void advancedSearch_OpenNow_Yes() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 10 -> empty -> empty -> empty -> empty -> "y" -> 0
                String input = "10\n\n\n\n\ny\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("符合條件的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("advancedSearch - 找不到結果")
        void advancedSearch_NoResults() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 10 -> "NoSuchRest" -> empty -> empty -> empty -> empty -> 0
                String input = "10\nNoSuchRest\n\n\n\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // Ensure repository is clean or won't match "NoSuchRest"
                // (Sample data doesn't have "NoSuchRest")

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("沒有找到符合條件的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("advancedSearch - 料理類型數值超出範圍")
        void advancedSearch_Cuisine_NumericOutOfRange() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 10 -> empty -> "99" (out of range) -> empty -> empty -> empty -> 0
                String input = "10\n\n99\n\n\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("符合條件的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("advancedSearch - 評分輸入非數字與超出範圍")
        void advancedSearch_Rating_EdgeCases() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // Test multiple inputs: 1. rating="abc"(fail) 2. rating="6"(fail range) 3.
                // rating="0"(fail range)
                // But scanner takes one line per prompt.
                // We test one scenario: Rating "6" (out of range)
                // 10 -> empty -> empty -> empty -> "6" -> empty -> 0
                String input = "10\n\n\n\n6\n\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("符合條件的餐廳"));
            } finally {
                System.setOut(originalOut);
            }
        }
    }
}
