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
    private BusinessHoursService businessHoursService;
    private PriceAnalyzer priceAnalyzer;

    @BeforeEach
    void setUp() {
        repository = new RestaurantRepository();
        ratingService = new RatingService();
        priceAnalyzer = new PriceAnalyzer();
        searchService = new RestaurantSearchService(repository);
        businessHoursService = new BusinessHoursService();
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

        @Test
        @DisplayName("搜尋服務 - 可以搜尋逢甲")
        void searchService_CanSearchFengJia() {
            List<Restaurant> results = searchService.searchGlobal("逢甲");
            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("搜尋服務 - 可以搜尋台式料理")
        void searchService_CanSearchTaiwanese() {
            List<Restaurant> results = searchService.searchGlobal("台式");
            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("搜尋服務 - 可以搜尋西區")
        void searchService_CanSearchXiQu() {
            List<Restaurant> results = searchService.searchGlobal("西區");
            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("推薦服務 - 取得人氣餐廳")
        void recommendationService_GetPopularRestaurants() {
            List<Restaurant> all = searchService.getAllRestaurants();
            List<Restaurant> popular = recommendationService.getPopularRestaurants(all, 5);

            assertFalse(popular.isEmpty());
            assertTrue(popular.size() <= 5);
        }

        @Test
        @DisplayName("推薦服務 - 取得相似餐廳")
        void recommendationService_GetSimilarRestaurants() {
            List<Restaurant> all = searchService.getAllRestaurants();
            Restaurant reference = all.get(0);

            List<Restaurant> similar = recommendationService.recommendSimilar(reference, all);
            assertNotNull(similar);
        }

        @Test
        @DisplayName("評分服務 - 計算平均評分")
        void ratingService_CalculateAverageRating() {
            List<Restaurant> all = searchService.getAllRestaurants();

            for (Restaurant r : all) {
                double rating = ratingService.calculateAverageRating(r);
                assertTrue(rating >= 0 && rating <= 5,
                        "餐廳 " + r.getName() + " 的評分應該在 0-5 之間");
            }
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

        @Test
        @DisplayName("價格過濾 - 頂級餐廳")
        void priceFilter_LuxuryRestaurants() {
            List<Restaurant> all = searchService.getAllRestaurants();
            long luxuryCount = all.stream()
                    .filter(r -> r.getPriceLevel() == 4)
                    .count();

            assertTrue(luxuryCount > 0, "應該有價格等級 4 的餐廳");
        }

        @Test
        @DisplayName("營業時間服務 - 可以查詢營業中餐廳")
        void businessHoursService_FindOpenNow() {
            List<Restaurant> all = searchService.getAllRestaurants();
            List<Restaurant> openNow = businessHoursService.findOpenNow(all);

            // 不確定當前時間是否有營業中的餐廳，但不應該拋出異常
            assertNotNull(openNow);
        }

        @Test
        @DisplayName("getAllRestaurants - 返回所有餐廳")
        void getAllRestaurants_ReturnsAll() {
            List<Restaurant> all = searchService.getAllRestaurants();
            assertEquals(10, all.size());
        }

        @Test
        @DisplayName("搜尋服務 - 空關鍵字返回空列表")
        void searchService_EmptyKeywordReturnsEmpty() {
            List<Restaurant> results = searchService.searchGlobal("");
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("搜尋服務 - null 關鍵字返回空列表")
        void searchService_NullKeywordReturnsEmpty() {
            List<Restaurant> results = searchService.searchGlobal(null);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("搜尋服務 - 找不到的關鍵字返回空列表")
        void searchService_NotFoundKeywordReturnsEmpty() {
            List<Restaurant> results = searchService.searchGlobal("不存在的餐廳名稱xyz");
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("System Output & Menu")
    class SystemOutputAndMenu {
        @Test
        @DisplayName("系統輸出 - 主選單包含所有選項")
        void systemOutput_MenuContainsAllOptions() {
            // 測試輸出流
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
                assertTrue(output.contains("台中美食"), "輸出應包含系統名稱");
                assertTrue(output.contains("關鍵字搜尋") || output.contains("搜尋"), "輸出應包含搜尋選項");
                assertTrue(output.contains("再見") || output.contains("離開"), "輸出應包含離開訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("系統輸出 - 顯示所有餐廳")
        void systemOutput_ShowAllRestaurants() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "5" 顯示所有餐廳，然後 "0" 離開
                String input = "5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("春水堂") || output.contains("10"),
                        "輸出應包含餐廳名稱或數量");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("系統輸出 - 無效選項處理")
        void systemOutput_InvalidOptionHandled() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入無效選項，然後 "0" 離開
                String input = "99\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效") || output.contains("錯誤"),
                        "輸出應包含錯誤訊息");
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Keyword Search Feature")
    class KeywordSearchFeature {
        @Test
        @DisplayName("searchByKeyword - 關鍵字為空")
        void searchByKeyword_EmptyInput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "1" 搜尋，然後輸入空白，然後 "0" 離開
                String input = "1\n   \n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("關鍵字不能為空"), "輸出應包含錯誤訊息");
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
                // 輸入 "1" 搜尋，然後輸入不存在的名稱，然後 "0" 離開
                String input = "1\nNonExistentRestaurantXYZ\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("找不到符"), "輸出應包含找不到結果的訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByKeyword - 找到結果")
        void searchByKeyword_FoundResults() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "1" 搜尋，然後輸入 "春水堂"，然後 "0" 離開
                String input = "1\n春水堂\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("找到") && output.contains("春水堂"),
                        "輸出應包含找到結果的訊息");
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Recommendation Feature")
    class RecommendationFeature {
        @Test
        @DisplayName("showRecommendations - 選擇有效餐廳編號")
        void showRecommendations_ValidIndex() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" 選擇推薦功能，然後輸入 "1" 選擇第一家餐廳，然後 "0" 離開
                String input = "3\n1\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("相似") || output.contains("推薦") || output.contains("找不到"),
                        "輸出應包含推薦相關訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showRecommendations - 選擇超出範圍的編號（太大）")
        void showRecommendations_InvalidIndexTooHigh() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" 選擇推薦功能，然後輸入 "999" 無效編號，然後 "0" 離開
                String input = "3\n999\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效") || output.contains("編號"),
                        "輸出應包含無效編號訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showRecommendations - 選擇負數編號")
        void showRecommendations_InvalidIndexNegative() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" 選擇推薦功能，然後輸入 "0" 或負數，然後 "0" 離開
                // 注意：輸入 "0" 會讓 index = -1 (0-1)，應該觸發無效編號
                String input = "3\n-5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效") || output.contains("編號"),
                        "輸出應包含無效編號訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showRecommendations - 輸入非數字")
        void showRecommendations_NonNumericInput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" 選擇推薦功能，然後輸入非數字，然後 "0" 離開
                String input = "3\nabc\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("數字") || output.contains("有效"),
                        "輸出應包含輸入有效數字的提示");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showRecommendations - 選擇最後一家餐廳")
        void showRecommendations_LastRestaurant() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" 選擇推薦功能，然後輸入 "10" 選擇最後一家餐廳，然後 "0" 離開
                String input = "3\n10\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("相似") || output.contains("推薦") || output.contains("找不到"),
                        "輸出應包含推薦相關訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showRecommendations - 邊界值測試（編號剛好超出）")
        void showRecommendations_BoundaryIndexExactlyOver() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" 選擇推薦功能，然後輸入 "11" (超出10家餐廳)，然後 "0" 離開
                String input = "3\n11\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("無效") || output.contains("編號"),
                        "輸出應包含無效編號訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("showRecommendations - 找不到相似餐廳")
        void showRecommendations_NoSimilarFound() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "3" 推薦，選擇第 11 個餐廳 (我們剛加的孤兒)，然後 "0" 離開
                String input = "3\n11\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // 添加一個與其他餐廳完全不同的餐廳
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant orphan = new Restaurant("999", "孤兒餐廳", CuisineType.OTHER,
                        new Location(0, 0, "No Address", "Nowhere"));
                orphan.setPriceLevel(1);
                repo.save(orphan);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("找不到類似") || output.contains("孤兒餐廳"),
                        "應顯示找不到類似餐廳或至少列出該餐廳");
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Price Search Feature")
    class PriceSearchFeature {
        @Test
        @DisplayName("searchByPriceLevel - 有效價格等級且有結果")
        void searchByPriceLevel_ValidLevelWithResults() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "4" 價格搜尋，然後輸入 "2" (有結果)，然後 "0" 離開
                String input = "4\n2\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("$$") && output.contains("家"),
                        "輸出應包含結果訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByPriceLevel - 價格等級過低")
        void searchByPriceLevel_LevelTooLow() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "4" 價格搜尋，然後輸入 "0"，然後 "0" 離開
                String input = "4\n0\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("請輸入 1-4"), "輸出應包含範圍錯誤訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByPriceLevel - 價格等級過高")
        void searchByPriceLevel_LevelTooHigh() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "4" 價格搜尋，然後輸入 "5"，然後 "0" 離開
                String input = "4\n5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("請輸入 1-4"), "輸出應包含範圍錯誤訊息");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("searchByPriceLevel - 非數字輸入")
        void searchByPriceLevel_NonNumericInput() {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                // 輸入 "4" 價格搜尋，然後輸入 "abc"，然後 "0" 離開
                String input = "4\nabc\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("有效的數字"), "輸出應包含格式錯誤訊息");
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
                // 輸入 "6" 顯示營業中，然後 "0" 離開
                String input = "6\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();
                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("目前營業中") || output.contains("沒有營業中"),
                        "輸出應包含營業狀態訊息");
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
                // 輸入 "6" 顯示營業中，然後 "0" 離開
                String input = "6\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // 使用反射修改所有餐廳使其關閉
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                for (Restaurant r : repo.findAll()) {
                    // 移除所有營業時間
                    r.setBusinessHours(new BusinessHours());
                }

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("沒有營業中"), "輸出應包含沒有營業中訊息");
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Display Utilities")
    class DisplayUtilities {
        @Test
        @DisplayName("printRestaurantList - 長描述截斷")
        void printRestaurantList_TruncatesLongDescription() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "5\n0\n"; // 顯示所有列表
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // 修改一個餐廳擁有超長描述
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant r = repo.findAll().get(0);
                String longDesc = "這是一個非常非常長的描述，絕對超過了四十個字元限制，用來測試系統是否能夠正確地將其截斷並顯示...";
                r.setDescription(longDesc);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("..."), "長描述應該被截斷並顯示省略號");
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        @DisplayName("printRestaurantList - 處理 Null 欄位")
        void printRestaurantList_HandlesNullFields() throws Exception {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));

            try {
                String input = "5\n0\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));

                Main main = new Main();

                // 創建一個擁有 Null 欄位的餐廳
                RestaurantRepository repo = (RestaurantRepository) getField(main, "repository");
                Restaurant nullRest = new Restaurant("999", "Null餐廳", null, null);
                nullRest.setDescription(null);
                nullRest.setPriceLevel(0);
                nullRest.setAveragePrice(0);
                repo.save(nullRest);

                main.run();

                String output = outContent.toString();
                assertTrue(output.contains("Null餐廳"), "應顯示餐廳名稱");
                assertTrue(output.contains("未分類") || output.contains("null"), "應處理 Null 菜系");
            } finally {
                System.setOut(originalOut);
            }
        }
    }
}
