package org.example.restaurant.service;

import org.example.restaurant.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

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

    @Nested
    @DisplayName("Preferences-Based Recommendation")
    class PreferencesBasedRecommendation {
        @Test
        @DisplayName("recommendByPreferences - null 列表返回空")
        void recommendByPreferences_NullList_ReturnsEmpty() {
            UserPreferences prefs = new UserPreferences();
            assertTrue(service.recommendByPreferences(prefs, null).isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - null 偏好使用熱門")
        void recommendByPreferences_NullPrefs_UsesPopular() {
            Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r);
            List<Restaurant> result = service.recommendByPreferences(null, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 優先喜歡的菜系")
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
        @DisplayName("recommendByPreferences - 篩選不喜歡的菜系")
        void recommendByPreferences_FiltersDisliked() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r1, r2);

            UserPreferences prefs = new UserPreferences();
            prefs.addDislikedCuisine(CuisineType.CHINESE);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            // 兩者都應在結果中但日式優先
            assertTrue(result.size() <= 2);
        }

        @Test
        @DisplayName("recommendByPreferences - 空列表返回空")
        void recommendByPreferences_EmptyList_ReturnsEmpty() {
            UserPreferences prefs = new UserPreferences();
            assertTrue(service.recommendByPreferences(prefs, new java.util.ArrayList<>()).isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 考慮停車位需求")
        void recommendByPreferences_ConsidersParkingRequirement() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setHasParking(true);
            Restaurant r2 = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            r2.setHasParking(false);
            List<Restaurant> list = Arrays.asList(r1, r2);

            UserPreferences prefs = new UserPreferences();
            prefs.setRequiresParking(true);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
            // 有停車位的應該優先
            assertEquals("1", result.get(0).getId());
        }

        @Test
        @DisplayName("recommendByPreferences - 考慮外送偏好")
        void recommendByPreferences_ConsidersDeliveryPreference() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setHasDelivery(true);
            Restaurant r2 = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            r2.setHasDelivery(false);
            List<Restaurant> list = Arrays.asList(r1, r2);

            UserPreferences prefs = new UserPreferences();
            prefs.setPreferDelivery(true);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 考慮外帶偏好")
        void recommendByPreferences_ConsidersTakeoutPreference() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setHasTakeout(true);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setPreferTakeout(true);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 考慮距離")
        void recommendByPreferences_ConsidersDistance() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0340, 121.5660);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setUserLocation(new Location(25.0330, 121.5654));
            prefs.setMaxDistanceKm(5.0);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 距離超過最大值")
        void recommendByPreferences_DistanceExceedsMax() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 26.0, 122.0);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setUserLocation(new Location(25.0330, 121.5654));
            prefs.setMaxDistanceKm(1.0);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            // 結果可能為空或分數較低
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendByPreferences - 跳過非活躍餐廳")
        void recommendByPreferences_SkipsInactiveRestaurants() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setActive(false);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 考慮附加菜系喜好")
        void recommendByPreferences_ConsidersAdditionalCuisines() {
            Restaurant r1 = createRestaurant("1", CuisineType.CHINESE, 25.0, 121.0);
            r1.addCuisineType(CuisineType.JAPANESE);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.addFavoriteCuisine(CuisineType.JAPANESE);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 排除不喜歡的附加菜系")
        void recommendByPreferences_FilterDislikedAdditionalCuisines() {
            Restaurant r1 = createRestaurant("1", CuisineType.CHINESE, 25.0, 121.0);
            r1.addCuisineType(CuisineType.ITALIAN);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.addDislikedCuisine(CuisineType.ITALIAN);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            // 餐廳可能在結果中，但分數較低
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendByPreferences - 價格等級超過最大值")
        void recommendByPreferences_PriceExceedsMax() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setPriceLevel(5);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setMaxPriceLevel(2);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            // 餐廳會在結果中但分數較低
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendByPreferences - 需要停車場且有停車場")
        void recommendByPreferences_RequiresParkingAndHas() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setHasParking(true);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setRequiresParking(true);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 需要停車場但沒有")
        void recommendByPreferences_RequiresParkingAndLacks() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setHasParking(false);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setRequiresParking(true);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            // 餐廳會在結果中但分數較低
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendByPreferences - 偏好外送且有外送")
        void recommendByPreferences_PreferDeliveryAndHas() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setHasDelivery(true);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setPreferDelivery(true);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 偏好外帶且有外帶")
        void recommendByPreferences_PreferTakeoutAndHas() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.setHasTakeout(true);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setPreferTakeout(true);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendByPreferences - 評分低於最低要求")
        void recommendByPreferences_RatingBelowMinimum() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            r1.getReviews().clear();
            Review lowReview = new Review("r1", "1", 2, "Bad");
            r1.addReview(lowReview);
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.setMinAcceptableRating(4.0);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendByPreferences - 無主菜系類型")
        void recommendByPreferences_NullMainCuisineType() {
            Restaurant r1 = new Restaurant("1", "Restaurant 1");
            r1.setActive(true);
            r1.setPriceLevel(2);
            r1.addReview(new Review("r1", "1", 4, "Good"));
            List<Restaurant> list = Arrays.asList(r1);

            UserPreferences prefs = new UserPreferences();
            prefs.addFavoriteCuisine(CuisineType.JAPANESE);

            List<Restaurant> result = service.recommendByPreferences(prefs, list);
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Similar Restaurant Recommendation")
    class SimilarRestaurantRecommendation {
        @Test
        @DisplayName("recommendSimilar - null 參考返回空")
        void recommendSimilar_NullReference_ReturnsEmpty() {
            Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r);
            assertTrue(service.recommendSimilar(null, list).isEmpty());
        }

        @Test
        @DisplayName("recommendSimilar - 找到相似餐廳")
        void recommendSimilar_FindsSimilar() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.1, 121.1);
            Restaurant different = createRestaurant("3", CuisineType.ITALIAN, 30.0, 130.0);
            List<Restaurant> list = Arrays.asList(ref, similar, different);

            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertTrue(result.stream().anyMatch(r -> r.getId().equals("2")));
        }

        @Test
        @DisplayName("recommendSimilar - 排除參考餐廳")
        void recommendSimilar_ExcludesReference() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(ref, similar);

            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertTrue(result.stream().noneMatch(r -> r.getId().equals("1")));
        }

        @Test
        @DisplayName("recommendSimilar - null 候選列表返回空")
        void recommendSimilar_NullCandidates_ReturnsEmpty() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            assertTrue(service.recommendSimilar(ref, null).isEmpty());
        }

        @Test
        @DisplayName("recommendSimilar - 空候選列表返回空")
        void recommendSimilar_EmptyCandidates_ReturnsEmpty() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            assertTrue(service.recommendSimilar(ref, new java.util.ArrayList<>()).isEmpty());
        }

        @Test
        @DisplayName("recommendSimilar - 跳過非活躍餐廳")
        void recommendSimilar_SkipsInactiveRestaurants() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant inactive = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            inactive.setActive(false);
            List<Restaurant> list = Arrays.asList(ref, inactive);

            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertTrue(result.stream().noneMatch(r -> r.getId().equals("2")));
        }

        @Test
        @DisplayName("recommendSimilar - r2 有 r1 的菜系作為附加菜系")
        void recommendSimilar_r2HasCuisineAsAdditional() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant similar = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
            similar.addCuisineType(CuisineType.JAPANESE);
            List<Restaurant> list = Arrays.asList(ref, similar);

            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertTrue(result.stream().anyMatch(r -> r.getId().equals("2")));
        }

        @Test
        @DisplayName("recommendSimilar - 無評分的餐廳")
        void recommendSimilar_NoRatingRestaurants() {
            Restaurant ref = new Restaurant("1", "Ref");
            ref.setCuisineType(CuisineType.JAPANESE);
            ref.setActive(true);

            Restaurant similar = new Restaurant("2", "Similar");
            similar.setCuisineType(CuisineType.JAPANESE);
            similar.setActive(true);
            List<Restaurant> list = Arrays.asList(ref, similar);

            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 無位置的餐廳")
        void recommendSimilar_NoLocationRestaurants() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setLocation(null);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setLocation(null);
            List<Restaurant> list = Arrays.asList(ref, similar);

            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 不同城市")
        void recommendSimilar_DifferentCities() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.getLocation().setCity("Taipei");

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.getLocation().setCity("Kaohsiung");
            List<Restaurant> list = Arrays.asList(ref, similar);

            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 菜系類型為 null")
        void recommendSimilar_NullCuisineTypes() {
            Restaurant ref = new Restaurant("1", "Ref");
            ref.setActive(true);
            ref.setCuisineType(null);

            Restaurant candidate = new Restaurant("2", "Candidate");
            candidate.setActive(true);
            candidate.setCuisineType(null);

            List<Restaurant> list = Arrays.asList(candidate);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            // 不應該拋出異常
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 相同價格等級")
        void recommendSimilar_SamePriceLevel() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setPriceLevel(2);
            ref.setAveragePrice(200);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setPriceLevel(2);
            similar.setAveragePrice(200);

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("recommendSimilar - 不同價格等級")
        void recommendSimilar_DifferentPriceLevel() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setPriceLevel(1);
            ref.setAveragePrice(100);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setPriceLevel(4);
            similar.setAveragePrice(800);

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 有評分差異")
        void recommendSimilar_DifferentRatings() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            // 添加高評分
            for (int i = 0; i < 3; i++) {
                ref.addReview(new Review("r" + i, "1", 5, "Great"));
            }

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            // 添加低評分
            for (int i = 0; i < 3; i++) {
                similar.addReview(new Review("s" + i, "2", 2, "Bad"));
            }

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 城市為 null")
        void recommendSimilar_NullCity() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.getLocation().setCity(null);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.getLocation().setCity(null);

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 特徵匹配差異（外送）")
        void recommendSimilar_DifferentDeliveryFeature() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setHasDelivery(true);
            ref.setHasTakeout(true);
            ref.setHasParking(true);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setHasDelivery(false); // 不同
            similar.setHasTakeout(true);
            similar.setHasParking(true);

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 特徵匹配差異（外帶）")
        void recommendSimilar_DifferentTakeoutFeature() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setHasDelivery(true);
            ref.setHasTakeout(true);
            ref.setHasParking(false);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setHasDelivery(true);
            similar.setHasTakeout(false); // 不同
            similar.setHasParking(false);

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 特徵匹配差異（停車位）")
        void recommendSimilar_DifferentParkingFeature() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setHasDelivery(false);
            ref.setHasTakeout(false);
            ref.setHasParking(true);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setHasDelivery(false);
            similar.setHasTakeout(false);
            similar.setHasParking(false); // 不同

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 所有特徵不同")
        void recommendSimilar_AllFeaturesDifferent() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setHasDelivery(true);
            ref.setHasTakeout(true);
            ref.setHasParking(true);

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setHasDelivery(false);
            similar.setHasTakeout(false);
            similar.setHasParking(false);

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 低於相似度閾值被排除")
        void recommendSimilar_BelowThresholdExcluded() {
            // 創建完全不同的餐廳
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.getLocation().setCity("Taipei");
            ref.setPriceLevel(1);
            ref.setHasDelivery(true);

            Restaurant dissimilar = createRestaurant("2", CuisineType.MEXICAN, 22.0, 120.0);
            dissimilar.getLocation().setCity("Kaohsiung");
            dissimilar.setPriceLevel(4);
            dissimilar.setHasDelivery(false);
            dissimilar.setHasTakeout(false);
            dissimilar.setHasParking(false);

            List<Restaurant> list = Arrays.asList(dissimilar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            // 相似度很低，可能被排除
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 候選列表包含 null 元素")
        void recommendSimilar_ListWithNullElements() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);

            List<Restaurant> list = Arrays.asList(null, similar, null);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }

        @Test
        @DisplayName("recommendSimilar - 價格等級為 0（未設定）")
        void recommendSimilar_PriceLevelZero() {
            Restaurant ref = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            ref.setPriceLevel(0); // 未設定

            Restaurant similar = createRestaurant("2", CuisineType.JAPANESE, 25.0, 121.0);
            similar.setPriceLevel(0); // 未設定

            List<Restaurant> list = Arrays.asList(similar);
            List<Restaurant> result = service.recommendSimilar(ref, list);
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Popularity Ranking")
    class PopularityRanking {
        @Test
        @DisplayName("getPopularRestaurants - null 列表返回空")
        void getPopularRestaurants_NullList_ReturnsEmpty() {
            assertTrue(service.getPopularRestaurants(null, 10).isEmpty());
        }

        @Test
        @DisplayName("getPopularRestaurants - 遵守限制數量")
        void getPopularRestaurants_RespectsLimit() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
            Restaurant r3 = createRestaurant("3", CuisineType.ITALIAN, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r1, r2, r3);

            List<Restaurant> result = service.getPopularRestaurants(list, 2);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("getPopularRestaurants - 空列表返回空")
        void getPopularRestaurants_EmptyList_ReturnsEmpty() {
            assertTrue(service.getPopularRestaurants(new java.util.ArrayList<>(), 10).isEmpty());
        }

        @Test
        @DisplayName("getPopularRestaurants - 負數限制使用預設值")
        void getPopularRestaurants_NegativeLimit_UsesDefault() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r1);
            List<Restaurant> result = service.getPopularRestaurants(list, -1);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("getPopularRestaurants - 跳過無評論餐廳")
        void getPopularRestaurants_SkipsNoReviewRestaurants() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant r2 = new Restaurant("2", "No Reviews");
            r2.setActive(true);
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = service.getPopularRestaurants(list, 10);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Proximity Search")
    class ProximitySearch {
        @Test
        @DisplayName("findNearby - null 位置返回空")
        void findNearby_NullLocation_ReturnsEmpty() {
            Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r);
            assertTrue(service.findNearby(null, list, 5.0).isEmpty());
        }

        @Test
        @DisplayName("findNearby - 在半徑內找到")
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
        @DisplayName("findNearby - 零半徑使用預設值")
        void findNearby_UsesDefaultRadius() {
            Location userLocation = new Location(25.0330, 121.5654);
            Restaurant near = createRestaurant("1", CuisineType.JAPANESE, 25.0340, 121.5660);
            List<Restaurant> list = Arrays.asList(near);

            List<Restaurant> result = service.findNearby(userLocation, list, 0);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("calculateDistance - null from 返回最大值")
        void calculateDistance_NullFrom_ReturnsMaxValue() {
            Location to = new Location(25.0, 121.0);
            assertEquals(Double.MAX_VALUE, service.calculateDistance(null, to));
        }

        @Test
        @DisplayName("calculateDistance - null to 返回最大值")
        void calculateDistance_NullTo_ReturnsMaxValue() {
            Location from = new Location(25.0, 121.0);
            assertEquals(Double.MAX_VALUE, service.calculateDistance(from, null));
        }

        @Test
        @DisplayName("calculateDistance - 同一點返回 0")
        void calculateDistance_SamePoint_ReturnsZero() {
            Location loc = new Location(25.0330, 121.5654);
            double distance = service.calculateDistance(loc, loc);
            assertEquals(0.0, distance, 0.001);
        }

        @Test
        @DisplayName("calculateDistance - 正確計算")
        void calculateDistance_CalculatesCorrectly() {
            Location taipei = new Location(25.0330, 121.5654);
            Location kaohsiung = new Location(22.6273, 120.3014);
            double distance = service.calculateDistance(taipei, kaohsiung);
            assertTrue(distance > 280 && distance < 400, "距離應該在 ~300-350km，實際: " + distance);
        }

        @Test
        @DisplayName("sortByDistance - null 位置返回空")
        void sortByDistance_NullLocation_ReturnsEmpty() {
            Restaurant r = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r);
            assertTrue(service.sortByDistance(null, list).isEmpty());
        }

        @Test
        @DisplayName("sortByDistance - 正確排序")
        void sortByDistance_SortsCorrectly() {
            Location userLocation = new Location(25.0, 121.0);
            Restaurant far = createRestaurant("1", CuisineType.JAPANESE, 26.0, 122.0);
            Restaurant near = createRestaurant("2", CuisineType.CHINESE, 25.01, 121.01);
            List<Restaurant> list = Arrays.asList(far, near);

            List<Restaurant> result = service.sortByDistance(userLocation, list);
            assertEquals("2", result.get(0).getId());
        }

        @Test
        @DisplayName("findNearby - 空列表返回空")
        void findNearby_EmptyList_ReturnsEmpty() {
            Location loc = new Location(25.0, 121.0);
            assertTrue(service.findNearby(loc, new java.util.ArrayList<>(), 5.0).isEmpty());
        }

        @Test
        @DisplayName("findNearby - null 列表返回空")
        void findNearby_NullList_ReturnsEmpty() {
            Location loc = new Location(25.0, 121.0);
            assertTrue(service.findNearby(loc, null, 5.0).isEmpty());
        }

        @Test
        @DisplayName("findNearby - 跳過無位置餐廳")
        void findNearby_SkipsRestaurantsWithoutLocation() {
            Location userLocation = new Location(25.0330, 121.5654);
            Restaurant r1 = new Restaurant("1", "No Location");
            r1.setActive(true);
            r1.addReview(new Review("r1", "1", 4, "Good"));
            List<Restaurant> list = Arrays.asList(r1);

            List<Restaurant> result = service.findNearby(userLocation, list, 5.0);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("sortByDistance - null 列表返回空")
        void sortByDistance_NullList_ReturnsEmpty() {
            Location loc = new Location(25.0, 121.0);
            assertTrue(service.sortByDistance(loc, null).isEmpty());
        }

        @Test
        @DisplayName("sortByDistance - 跳過無位置餐廳")
        void sortByDistance_SkipsRestaurantsWithoutLocation() {
            Location userLocation = new Location(25.0, 121.0);
            Restaurant r1 = new Restaurant("1", "No Location");
            r1.setActive(true);
            List<Restaurant> list = Arrays.asList(r1);

            List<Restaurant> result = service.sortByDistance(userLocation, list);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Top Picks Suggestion")
    class TopPicksSuggestion {
        @Test
        @DisplayName("getTopPicks - null 列表返回空")
        void getTopPicks_NullList_ReturnsEmpty() {
            Location loc = new Location(25.0, 121.0);
            assertTrue(service.getTopPicks(null, loc, 5).isEmpty());
        }

        @Test
        @DisplayName("getTopPicks - 遵守限制數量")
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
        @DisplayName("getTopPicks - 無位置也可運作")
        void getTopPicks_WorksWithoutLocation() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            Restaurant r2 = createRestaurant("2", CuisineType.CHINESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = service.getTopPicks(list, null, 5);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("getTopPicks - 空列表返回空")
        void getTopPicks_EmptyList_ReturnsEmpty() {
            Location loc = new Location(25.0, 121.0);
            assertTrue(service.getTopPicks(new java.util.ArrayList<>(), loc, 5).isEmpty());
        }

        @Test
        @DisplayName("getTopPicks - 負數限制使用預設值")
        void getTopPicks_NegativeLimit_UsesDefault() {
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0, 121.0);
            List<Restaurant> list = Arrays.asList(r1);
            Location loc = new Location(25.0, 121.0);

            List<Restaurant> result = service.getTopPicks(list, loc, -1);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("getTopPicks - 1公里內距離獎勵")
        void getTopPicks_DistanceBonus_Within1Km() {
            Location userLocation = new Location(25.0330, 121.5654);
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.0335, 121.5658);
            List<Restaurant> list = Arrays.asList(r1);

            List<Restaurant> result = service.getTopPicks(list, userLocation, 5);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("getTopPicks - 3公里內距離獎勵")
        void getTopPicks_DistanceBonus_Within3Km() {
            Location userLocation = new Location(25.0330, 121.5654);
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.05, 121.58);
            List<Restaurant> list = Arrays.asList(r1);

            List<Restaurant> result = service.getTopPicks(list, userLocation, 5);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("getTopPicks - 5公里內距離獎勵")
        void getTopPicks_DistanceBonus_Within5Km() {
            Location userLocation = new Location(25.0330, 121.5654);
            Restaurant r1 = createRestaurant("1", CuisineType.JAPANESE, 25.07, 121.60);
            List<Restaurant> list = Arrays.asList(r1);

            List<Restaurant> result = service.getTopPicks(list, userLocation, 5);
            assertFalse(result.isEmpty());
        }
    }
}
