package org.example.restaurant.data;

import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SampleDataLoader 測試類別
 */
class SampleDataLoaderTest {

    private RestaurantRepository repository;
    private SampleDataLoader loader;

    @BeforeEach
    void setUp() {
        repository = new RestaurantRepository();
        loader = new SampleDataLoader(repository);
    }

    @Nested
    @DisplayName("Data Loading")
    class DataLoading {
        @Test
        @DisplayName("loadSampleData - 載入 10 家餐廳")
        void loadSampleData_LoadsTenRestaurants() {
            loader.loadSampleData();

            List<Restaurant> all = repository.findAll();
            assertEquals(10, all.size());
        }

        @Test
        @DisplayName("loadSampleData - 可以多次載入不會出錯")
        void loadSampleData_CanLoadMultipleTimes() {
            loader.loadSampleData();
            loader.loadSampleData(); // 再次載入

            // 應該不會拋出異常
            List<Restaurant> all = repository.findAll();
            assertTrue(all.size() >= 10);
        }
    }

    @Nested
    @DisplayName("Data Integrity")
    class DataIntegrity {
        @BeforeEach
        void loadData() {
            loader.loadSampleData();
        }

        @Test
        @DisplayName("loadSampleData - 所有餐廳都是活躍狀態")
        void loadSampleData_AllRestaurantsActive() {
            List<Restaurant> all = repository.findAll();
            for (Restaurant r : all) {
                assertTrue(r.isActive(), "餐廳 " + r.getName() + " 應該是活躍狀態");
            }
        }

        @Test
        @DisplayName("loadSampleData - 所有餐廳都有營業時間")
        void loadSampleData_AllRestaurantsHaveBusinessHours() {
            List<Restaurant> all = repository.findAll();
            for (Restaurant r : all) {
                assertNotNull(r.getBusinessHours(), "餐廳 " + r.getName() + " 應該有營業時間");
            }
        }

        @Test
        @DisplayName("loadSampleData - 所有餐廳都有評論")
        void loadSampleData_AllRestaurantsHaveReviews() {
            List<Restaurant> all = repository.findAll();
            for (Restaurant r : all) {
                assertFalse(r.getReviews().isEmpty(), "餐廳 " + r.getName() + " 應該有評論");
            }
        }

        @Test
        @DisplayName("loadSampleData - 所有餐廳都有位置資訊")
        void loadSampleData_AllRestaurantsHaveLocation() {
            List<Restaurant> all = repository.findAll();
            for (Restaurant r : all) {
                assertNotNull(r.getLocation(), "餐廳 " + r.getName() + " 應該有位置資訊");
                assertEquals("台中市", r.getLocation().getCity());
            }
        }

        @Test
        @DisplayName("loadSampleData - 所有餐廳都有價格等級")
        void loadSampleData_AllRestaurantsHavePriceLevel() {
            List<Restaurant> all = repository.findAll();
            for (Restaurant r : all) {
                assertTrue(r.getPriceLevel() >= 1 && r.getPriceLevel() <= 4,
                        "餐廳 " + r.getName() + " 的價格等級應該在 1-4 之間");
            }
        }

        @Test
        @DisplayName("loadSampleData - 評論日期正確設定")
        void loadSampleData_ReviewDatesSet() {
            List<Restaurant> all = repository.findAll();
            for (Restaurant r : all) {
                for (Review review : r.getReviews()) {
                    assertNotNull(review.getCreatedAt(),
                            "餐廳 " + r.getName() + " 的評論應該有建立日期");
                }
            }
        }
    }

    @Nested
    @DisplayName("Specific Restaurant Data")
    class SpecificRestaurantData {
        @BeforeEach
        void loadData() {
            loader.loadSampleData();
        }

        @Test
        @DisplayName("loadSampleData - 春水堂正確載入")
        void loadSampleData_ChunShuiTangLoaded() {
            Restaurant chunShui = repository.findById("1").orElse(null);
            assertNotNull(chunShui);
            assertEquals("春水堂創始店", chunShui.getName());
            assertEquals(CuisineType.TAIWANESE, chunShui.getCuisineType());
            assertEquals("西區", chunShui.getLocation().getDistrict());
        }

        @Test
        @DisplayName("loadSampleData - 宮原眼科正確載入")
        void loadSampleData_MiyaharaLoaded() {
            Restaurant miyahara = repository.findById("2").orElse(null);
            assertNotNull(miyahara);
            assertEquals("宮原眼科", miyahara.getName());
            assertEquals(CuisineType.DESSERT, miyahara.getCuisineType());
        }

        @Test
        @DisplayName("loadSampleData - 屋馬燒肉正確載入")
        void loadSampleData_UmaiLoaded() {
            Restaurant umai = repository.findById("5").orElse(null);
            assertNotNull(umai);
            assertEquals("屋馬燒肉國安店", umai.getName());
            assertEquals(CuisineType.JAPANESE, umai.getCuisineType());
            assertEquals(4, umai.getPriceLevel()); // 頂級餐廳
        }

        @Test
        @DisplayName("loadSampleData - 輕井澤鍋物正確載入")
        void loadSampleData_KaruizawaLoaded() {
            Restaurant karuizawa = repository.findById("8").orElse(null);
            assertNotNull(karuizawa);
            assertEquals("輕井澤鍋物公益店", karuizawa.getName());
            assertEquals(CuisineType.HOT_POT, karuizawa.getCuisineType());
        }
    }

    @Nested
    @DisplayName("Data Diversity")
    class DataDiversity {
        @BeforeEach
        void loadData() {
            loader.loadSampleData();
        }

        @Test
        @DisplayName("loadSampleData - 各價格等級都有餐廳")
        void loadSampleData_AllPriceLevelsRepresented() {
            List<Restaurant> all = repository.findAll();
            int[] priceLevelCount = new int[5]; // 0-4

            for (Restaurant r : all) {
                priceLevelCount[r.getPriceLevel()]++;
            }

            assertTrue(priceLevelCount[1] > 0, "應該有價格等級 1 的餐廳");
            assertTrue(priceLevelCount[2] > 0, "應該有價格等級 2 的餐廳");
            assertTrue(priceLevelCount[3] > 0, "應該有價格等級 3 的餐廳");
            assertTrue(priceLevelCount[4] > 0, "應該有價格等級 4 的餐廳");
        }

        @Test
        @DisplayName("loadSampleData - 各菜系都有餐廳")
        void loadSampleData_MultipleCuisineTypes() {
            List<Restaurant> all = repository.findAll();

            boolean hasTaiwanese = all.stream().anyMatch(r -> r.getCuisineType() == CuisineType.TAIWANESE);
            boolean hasDessert = all.stream().anyMatch(r -> r.getCuisineType() == CuisineType.DESSERT);
            boolean hasJapanese = all.stream().anyMatch(r -> r.getCuisineType() == CuisineType.JAPANESE);
            boolean hasHotPot = all.stream().anyMatch(r -> r.getCuisineType() == CuisineType.HOT_POT);
            boolean hasThai = all.stream().anyMatch(r -> r.getCuisineType() == CuisineType.THAI);

            assertTrue(hasTaiwanese, "應該有台式料理餐廳");
            assertTrue(hasDessert, "應該有甜點店");
            assertTrue(hasJapanese, "應該有日式料理餐廳");
            assertTrue(hasHotPot, "應該有火鍋店");
            assertTrue(hasThai, "應該有泰式料理餐廳");
        }
    }
}
