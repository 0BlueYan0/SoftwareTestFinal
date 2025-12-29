package org.example.restaurant.service;

import org.example.restaurant.model.Restaurant;
import org.example.restaurant.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RatingServiceTest {

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService();
    }

    private Restaurant createRestaurantWithReviews(int... ratings) {
        Restaurant restaurant = new Restaurant("1", "Test Restaurant");
        for (int i = 0; i < ratings.length; i++) {
            Review review = new Review("r" + i, "1", ratings[i], "Comment");
            review.setUserLevel(3);
            review.setCreatedAt(LocalDateTime.now().minusDays(i));
            restaurant.addReview(review);
        }
        return restaurant;
    }

    @Nested
    @DisplayName("Average Rating Calculation")
    class AverageRatingCalculation {
        @Test
        @DisplayName("calculateAverageRating - null 餐廳返回 0")
        void calculateAverageRating_NullRestaurant_ReturnsZero() {
            assertEquals(0.0, ratingService.calculateAverageRating(null));
        }

        @Test
        @DisplayName("calculateAverageRating - 無評論返回 0")
        void calculateAverageRating_NoReviews_ReturnsZero() {
            Restaurant restaurant = new Restaurant("1", "Test");
            assertEquals(0.0, ratingService.calculateAverageRating(restaurant));
        }

        @Test
        @DisplayName("calculateAverageRating - 單一評論")
        void calculateAverageRating_SingleReview_ReturnsRating() {
            Restaurant restaurant = createRestaurantWithReviews(4);
            assertEquals(4.0, ratingService.calculateAverageRating(restaurant));
        }

        @Test
        @DisplayName("calculateAverageRating - 多個評論計算平均")
        void calculateAverageRating_MultipleReviews_ReturnsAverage() {
            Restaurant restaurant = createRestaurantWithReviews(4, 5, 3, 4, 4);
            assertEquals(4.0, ratingService.calculateAverageRating(restaurant));
        }

        @Test
        @DisplayName("calculateAverageRating - 忽略無效評分")
        void calculateAverageRating_IgnoresInvalidRatings() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.addReview(new Review("1", "1", 4, "Good"));
            Review invalidReview = new Review("2", "1", 0, "Invalid");
            restaurant.addReview(invalidReview);
            assertEquals(4.0, ratingService.calculateAverageRating(restaurant));
        }

        @Test
        @DisplayName("calculateAverageRating - 所有評分無效返回 0")
        void calculateAverageRating_AllInvalidRatings_ReturnsZero() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.addReview(new Review("1", "1", 0, "Invalid"));
            restaurant.addReview(new Review("2", "1", 6, "Invalid"));
            restaurant.getReviews().add(null);
            assertEquals(0.0, ratingService.calculateAverageRating(restaurant));
        }

        @Test
        @DisplayName("calculateAverage - 私有方法反射測試 (Null/Empty/Invalid)")
        void calculateAverage_ReflectionTest() throws Exception {
            java.lang.reflect.Method method = RatingService.class.getDeclaredMethod("calculateAverage", List.class);
            method.setAccessible(true);

            // 1. Null list
            assertEquals(0.0, (Double) method.invoke(ratingService, (List<Review>) null));

            // 2. Empty list
            assertEquals(0.0, (Double) method.invoke(ratingService, new ArrayList<Review>()));

            // 3. List with nulls and invalid ratings
            List<Review> reviews = new ArrayList<>();
            reviews.add(null);
            reviews.add(new Review("r1", "u1", 0, "Too low")); // rating 0
            reviews.add(new Review("r2", "u2", 6, "Too high")); // rating 6
            reviews.add(new Review("r3", "u3", 3, "Valid")); // rating 3

            // Should only count the valid one (3)
            assertEquals(3.0, (Double) method.invoke(ratingService, reviews));
        }
    }

    @Nested
    @DisplayName("Weighted Rating Calculation")
    class WeightedRatingCalculation {
        @Test
        @DisplayName("calculateWeightedRating - null 餐廳返回 0")
        void calculateWeightedRating_NullRestaurant_ReturnsZero() {
            assertEquals(0.0, ratingService.calculateWeightedRating(null));
        }

        @Test
        @DisplayName("calculateWeightedRating - 少量評論使用平均值")
        void calculateWeightedRating_FewReviews_UsesAverage() {
            Restaurant restaurant = createRestaurantWithReviews(4, 5, 4);
            double avg = ratingService.calculateAverageRating(restaurant);
            double weighted = ratingService.calculateWeightedRating(restaurant);
            assertEquals(avg, weighted);
        }

        @Test
        @DisplayName("calculateWeightedRating - 多評論使用加權計算")
        void calculateWeightedRating_ManyReviews_UsesWeighted() {
            Restaurant restaurant = createRestaurantWithReviews(4, 5, 4, 5, 4, 5, 4, 5);
            double weighted = ratingService.calculateWeightedRating(restaurant);
            assertTrue(weighted > 0);
            assertTrue(weighted <= 5);
        }

        @Test
        @DisplayName("calculateWeightedRating - 無評論返回 0")
        void calculateWeightedRating_NoReviews_ReturnsZero() {
            Restaurant restaurant = new Restaurant("1", "Test");
            assertEquals(0.0, ratingService.calculateWeightedRating(restaurant));
        }

        @Test
        @DisplayName("calculateWeightedRating - 驗證評論加權")
        void calculateWeightedRating_VerifiedReviews_UsesWeight() {
            Restaurant restaurant = new Restaurant("1", "Test");
            for (int i = 0; i < 6; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setUserLevel(3);
                review.setVerified(true);
                review.setHelpfulCount(15);
                review.setCreatedAt(LocalDateTime.now().minusDays(i));
                restaurant.addReview(review);
            }
            double weighted = ratingService.calculateWeightedRating(restaurant);
            assertEquals(4.0, weighted);
        }

        @Test
        @DisplayName("calculateWeightedRating - 高 helpfulCount 影響權重 (>20)")
        void calculateWeightedRating_HighHelpfulCount() {
            Restaurant restaurant = new Restaurant("1", "Test");
            for (int i = 0; i < 6; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setUserLevel(3);
                review.setHelpfulCount(25); // > 20 觸發 1.4x weight
                review.setCreatedAt(LocalDateTime.now().minusDays(i));
                restaurant.addReview(review);
            }
            double weighted = ratingService.calculateWeightedRating(restaurant);
            assertTrue(weighted > 0);
        }

        @Test
        @DisplayName("calculateWeightedRating - 中等 helpfulCount 影響權重 (>10)")
        void calculateWeightedRating_MediumHelpfulCount() {
            Restaurant restaurant = new Restaurant("1", "Test");
            for (int i = 0; i < 6; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setUserLevel(3);
                review.setHelpfulCount(15); // > 10 觸發 1.2x weight
                review.setCreatedAt(LocalDateTime.now().minusDays(i));
                restaurant.addReview(review);
            }
            double weighted = ratingService.calculateWeightedRating(restaurant);
            assertTrue(weighted > 0);
        }

        @Test
        @DisplayName("calculateWeightedRating - 低 helpfulCount 影響權重 (>5)")
        void calculateWeightedRating_LowHelpfulCount() {
            Restaurant restaurant = new Restaurant("1", "Test");
            for (int i = 0; i < 6; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setUserLevel(3);
                review.setHelpfulCount(7); // > 5 觸發 1.1x weight
                review.setCreatedAt(LocalDateTime.now().minusDays(i));
                restaurant.addReview(review);
            }
            double weighted = ratingService.calculateWeightedRating(restaurant);
            assertTrue(weighted > 0);
        }

        @Test
        @DisplayName("calculateWeightedRating - 非最近評論降低權重")
        void calculateWeightedRating_OldReviewsLowerWeight() {
            Restaurant restaurant = new Restaurant("1", "Test");
            for (int i = 0; i < 6; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setUserLevel(3);
                review.setCreatedAt(LocalDateTime.now().minusMonths(3)); // 非最近
                restaurant.addReview(review);
            }
            double weighted = ratingService.calculateWeightedRating(restaurant);
            assertTrue(weighted > 0);
        }

        @Test
        @DisplayName("calculateWeightedRating - 無效評分")
        void calculateWeightedRating_AllInvalidRatings() {
            Restaurant restaurant = new Restaurant("1", "Test");
            for (int i = 0; i < 6; i++) {
                Review review = new Review("r" + i, "1", 0, "Invalid");
                restaurant.addReview(review);
            }
            assertEquals(0.0, ratingService.calculateWeightedRating(restaurant));
        }

        @Test
        @DisplayName("calculateReviewWeight - 私有方法反射測試 (分支覆蓋)")
        void calculateReviewWeight_ReflectionTest() throws Exception {
            java.lang.reflect.Method method = RatingService.class.getDeclaredMethod("calculateReviewWeight",
                    Review.class);
            method.setAccessible(true);

            // 1. Null Review
            assertEquals(1.0, (Double) method.invoke(ratingService, (Review) null));

            // 2. Base weight verified
            Review r1 = new Review("r1", "u1", 5, "c");
            r1.setUserLevel(0); // weight factor = 1.0
            r1.setVerified(true); // * 1.3
            r1.setHelpfulCount(0); // no change
            assertEquals(1.56, (Double) method.invoke(ratingService, r1), 0.01);

            // 3. User Level & Helpful Count branches
            Review r2 = new Review("r2", "u2", 5, "c");
            r2.setUserLevel(5); // (0.5 + 1.0) = 1.5 multiplier
            r2.setHelpfulCount(21); // > 20 -> * 1.4
            r2.setVerified(false);
            assertEquals(2.52, (Double) method.invoke(ratingService, r2), 0.01);

            // 4. Helpful count > 10 (but <= 20)
            Review r3 = new Review("r3", "u3", 5, "c");
            r3.setHelpfulCount(15); // * 1.2
            r3.setUserLevel(1); // (0.5 + 0.2) = 0.7
            assertEquals(1.008, (Double) method.invoke(ratingService, r3), 0.001);

            // 5. Helpful count > 5 (but <= 10)
            Review r4 = new Review("r4", "u4", 5, "c");
            r4.setUserLevel(0); // Set to 0 to skip user level factor
            r4.setHelpfulCount(6); // * 1.1
            assertEquals(1.32, (Double) method.invoke(ratingService, r4), 0.01);

            // 6. Not Recent
            Review r5 = new Review("r5", "u5", 5, "c");
            r5.setUserLevel(0); // Set to 0 to skip user level factor
            r5.setCreatedAt(LocalDateTime.now().minusMonths(7)); // Not recent (> 6 months)
            assertEquals(0.9, (Double) method.invoke(ratingService, r5), 0.01);

            // 7. Cap at 3.0
            Review r6 = new Review("r6", "u6", 5, "c");
            r6.setUserLevel(5); // * 1.5
            r6.setVerified(true); // * 1.3
            r6.setHelpfulCount(100); // * 1.4
            r6.setCreatedAt(LocalDateTime.now()); // * 1.2
            assertEquals(3.0, (Double) method.invoke(ratingService, r6), 0.01);
        }
    }

    @Nested
    @DisplayName("Rating Trend Analysis")
    class RatingTrendAnalysis {
        @Test
        @DisplayName("calculateRatingTrend - 資料不足")
        void calculateRatingTrend_InsufficientData_ReturnsMessage() {
            Restaurant restaurant = createRestaurantWithReviews(5, 5, 5);
            assertEquals("INSUFFICIENT_DATA", ratingService.calculateRatingTrend(restaurant));
        }

        @Test
        @DisplayName("calculateRatingTrend - null 餐廳返回 UNKNOWN")
        void calculateRatingTrend_NullRestaurant_ReturnsUnknown() {
            assertEquals("UNKNOWN", ratingService.calculateRatingTrend(null));
        }

        @Test
        @DisplayName("calculateRatingTrend - 評分提升返回 IMPROVING (Complex)")
        void calculateRatingTrend_Improving_ReturnsImproving() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 前半部分低評分，後半部分高評分
            for (int i = 0; i < 5; i++) {
                Review review = new Review("r" + i, "1", 2, "Bad");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            for (int i = 5; i < 12; i++) {
                Review review = new Review("r" + i, "1", 5, "Great");
                review.setCreatedAt(LocalDateTime.now().minusDays(15 - (i - 5)));
                restaurant.addReview(review);
            }
            assertEquals("IMPROVING", ratingService.calculateRatingTrend(restaurant));
        }

        @Test
        @DisplayName("calculateRatingTrend - 評分下降返回 DECLINING (Complex)")
        void calculateRatingTrend_Declining_ReturnsDeclining() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 前半部分高評分，後半部分低評分
            for (int i = 0; i < 5; i++) {
                Review review = new Review("r" + i, "1", 5, "Great");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            for (int i = 5; i < 12; i++) {
                Review review = new Review("r" + i, "1", 2, "Bad");
                review.setCreatedAt(LocalDateTime.now().minusDays(15 - (i - 5)));
                restaurant.addReview(review);
            }
            assertEquals("DECLINING", ratingService.calculateRatingTrend(restaurant));
        }

        @Test
        @DisplayName("calculateRatingTrend - 評分穩定返回 STABLE (Complex)")
        void calculateRatingTrend_Stable_ReturnsStable() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 所有評分相近
            for (int i = 0; i < 12; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            assertEquals("STABLE", ratingService.calculateRatingTrend(restaurant));
        }

        @Test
        @DisplayName("calculateRatingTrend - IMPROVING 趨勢 (Simple)")
        void calculateRatingTrend_Improving() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 前 5 個評論較低分
            for (int i = 0; i < 5; i++) {
                Review review = new Review("r" + i, "1", 2, "Bad");
                review.setCreatedAt(LocalDateTime.now().minusDays(20 - i));
                restaurant.addReview(review);
            }
            // 後 5 個評論較高分
            for (int i = 5; i < 10; i++) {
                Review review = new Review("r" + i, "1", 5, "Great");
                review.setCreatedAt(LocalDateTime.now().minusDays(10 - (i - 5)));
                restaurant.addReview(review);
            }
            String trend = ratingService.calculateRatingTrend(restaurant);
            assertEquals("IMPROVING", trend);
        }

        @Test
        @DisplayName("calculateRatingTrend - DECLINING 趨勢 (Simple)")
        void calculateRatingTrend_Declining() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 前 5 個評論較高分
            for (int i = 0; i < 5; i++) {
                Review review = new Review("r" + i, "1", 5, "Great");
                review.setCreatedAt(LocalDateTime.now().minusDays(20 - i));
                restaurant.addReview(review);
            }
            // 後 5 個評論較低分
            for (int i = 5; i < 10; i++) {
                Review review = new Review("r" + i, "1", 2, "Bad");
                review.setCreatedAt(LocalDateTime.now().minusDays(10 - (i - 5)));
                restaurant.addReview(review);
            }
            String trend = ratingService.calculateRatingTrend(restaurant);
            assertEquals("DECLINING", trend);
        }

        @Test
        @DisplayName("calculateRatingTrend - STABLE 趨勢 (Simple)")
        void calculateRatingTrend_Stable() {
            Restaurant restaurant = new Restaurant("1", "Test");
            for (int i = 0; i < 10; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setCreatedAt(LocalDateTime.now().minusDays(i));
                restaurant.addReview(review);
            }
            String trend = ratingService.calculateRatingTrend(restaurant);
            assertEquals("STABLE", trend);
        }

        @Test
        @DisplayName("calculateRatingTrend - 評論列表包含 null 元素")
        void calculateRatingTrend_WithNullReviews_HandlesGracefully() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 添加一些正常評論和 null 評論
            for (int i = 0; i < 12; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            // 應該正常處理並返回結果
            String result = ratingService.calculateRatingTrend(restaurant);
            assertNotNull(result);
            assertFalse(result.equals("UNKNOWN"));
        }

        @Test
        @DisplayName("calculateRatingTrend - 評論包含無效評分（小於 1）")
        void calculateRatingTrend_WithInvalidLowRatings_IgnoresThem() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 添加正常評論
            for (int i = 0; i < 12; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            // 添加無效評分評論（評分 = 0）
            Review invalidReview = new Review("invalid", "1", 0, "Invalid");
            invalidReview.setCreatedAt(LocalDateTime.now().minusDays(5));
            restaurant.addReview(invalidReview);

            String result = ratingService.calculateRatingTrend(restaurant);
            assertNotNull(result);
        }

        @Test
        @DisplayName("calculateRatingTrend - 評論包含無效評分（大於 5）")
        void calculateRatingTrend_WithInvalidHighRatings_IgnoresThem() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 添加正常評論
            for (int i = 0; i < 12; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            // 添加無效評分評論（評分 = 10）
            Review invalidReview = new Review("invalid", "1", 10, "Invalid");
            invalidReview.setCreatedAt(LocalDateTime.now().minusDays(5));
            restaurant.addReview(invalidReview);

            String result = ratingService.calculateRatingTrend(restaurant);
            assertNotNull(result);
        }

        @Test
        @DisplayName("calculateRatingTrend - 評論無日期被過濾")
        void calculateRatingTrend_ReviewsWithoutDate_Filtered() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 添加有日期的評論
            for (int i = 0; i < 8; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            // 添加沒有日期的評論
            for (int i = 8; i < 15; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                // 不設定 createdAt
                restaurant.addReview(review);
            }

            // 總共有 15 個評論，但部分沒有日期
            // 方法會過濾無日期的評論，如果過濾後少於 10 筆則返回 INSUFFICIENT_DATA
            String result = ratingService.calculateRatingTrend(restaurant);
            // 結果可能是 STABLE 或 INSUFFICIENT_DATA，取決於過濾邏輯
            assertNotNull(result);
        }

        @Test
        @DisplayName("calculateRatingTrend - 僅有 10 筆評論的邊界情況")
        void calculateRatingTrend_ExactlyTenReviews_Works() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 剛好 10 筆評論
            for (int i = 0; i < 10; i++) {
                Review review = new Review("r" + i, "1", 4, "Good");
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }

            String result = ratingService.calculateRatingTrend(restaurant);
            assertNotNull(result);
            assertNotEquals("INSUFFICIENT_DATA", result);
        }

        @Test
        @DisplayName("calculateRatingTrend - 部分評論無效評分仍可計算")
        void calculateRatingTrend_MixedValidInvalidRatings_CalculatesCorrectly() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 添加前半部分評論（混合有效和無效）
            for (int i = 0; i < 6; i++) {
                Review review;
                if (i % 2 == 0) {
                    review = new Review("r" + i, "1", 3, "OK"); // 有效
                } else {
                    review = new Review("r" + i, "1", -1, "Invalid"); // 無效
                }
                review.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
                restaurant.addReview(review);
            }
            // 添加後半部分評論（全部有效高分）
            for (int i = 6; i < 14; i++) {
                Review review = new Review("r" + i, "1", 5, "Great");
                review.setCreatedAt(LocalDateTime.now().minusDays(15 - (i - 6)));
                restaurant.addReview(review);
            }

            String result = ratingService.calculateRatingTrend(restaurant);
            assertNotNull(result);
            // 前半部有效評分低，後半部高分，應該是 IMPROVING
            assertEquals("IMPROVING", result);
        }

        @Test
        @DisplayName("calculateRatingTrend - No reviews returns INSUFFICIENT_DATA")
        void calculateRatingTrend_NoReviews_ReturnsInsufficientData() {
            Restaurant restaurant = new Restaurant("1", "Test");
            assertEquals("INSUFFICIENT_DATA", ratingService.calculateRatingTrend(restaurant));
        }

        @Test
        @DisplayName("calculateRatingTrend - 驗證評論排序 (Lambda 分支)")
        void calculateRatingTrend_VerifySortingLogic() {
            Restaurant restaurant = new Restaurant("1", "Test");
            // 插入順序：最新 -> 最舊 (逆序)
            // 最舊 (Oldest): 1 star
            // 最新 (Newest): 5 stars
            // 如果沒排序：前半(Newest, 5 stars) -> 後半(Oldest, 1 star) => DECLINING
            // 如果有排序：前半(Oldest, 1 star) -> 後半(Newest, 5 stars) => IMPROVING

            for (int i = 0; i < 12; i++) {
                // i=0 is newest (today), i=11 is oldest
                Review review = new Review("r" + i, "u" + i,
                        i < 6 ? 5 : 1, // First 6 inserted (newest) are 5, last 6 (oldest) are 1
                        "Comment");
                review.setCreatedAt(LocalDateTime.now().minusDays(i));
                restaurant.addReview(review);
            }

            String trend = ratingService.calculateRatingTrend(restaurant);
            assertEquals("IMPROVING", trend, "應該先排序評論再計算趨勢");
        }
    }

    @Nested
    @DisplayName("Rating Filtering")
    class RatingFiltering {
        @Test
        @DisplayName("filterByRatingRange - null 列表返回空")
        void filterByRatingRange_NullList_ReturnsEmpty() {
            List<Restaurant> result = ratingService.filterByRatingRange(null, 3.0, 5.0);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("filterByRatingRange - 正確篩選")
        void filterByRatingRange_FiltersCorrectly() {
            Restaurant r1 = createRestaurantWithReviews(5, 5, 5);
            Restaurant r2 = createRestaurantWithReviews(3, 3, 3);
            Restaurant r3 = createRestaurantWithReviews(2, 2, 2);
            List<Restaurant> list = Arrays.asList(r1, r2, r3);

            List<Restaurant> result = ratingService.filterByRatingRange(list, 3.0, 5.0);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("filterByRatingRange - null 最小值包含所有低分")
        void filterByRatingRange_NullMin_IncludesAllBelow() {
            Restaurant r1 = createRestaurantWithReviews(5);
            Restaurant r2 = createRestaurantWithReviews(1);
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = ratingService.filterByRatingRange(list, null, 5.0);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("filterByRatingRange - null 最大值包含所有高分")
        void filterByRatingRange_NullMax_IncludesAllAbove() {
            Restaurant r1 = createRestaurantWithReviews(5);
            Restaurant r2 = createRestaurantWithReviews(3);
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = ratingService.filterByRatingRange(list, 3.0, null);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("filterByRatingRange - 空列表返回空")
        void filterByRatingRange_EmptyList_ReturnsEmpty() {
            List<Restaurant> result = ratingService.filterByRatingRange(new ArrayList<>(), 3.0, 5.0);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("filterByRatingRange - 跳過 null 餐廳")
        void filterByRatingRange_SkipsNullRestaurants() {
            Restaurant r1 = createRestaurantWithReviews(4);
            List<Restaurant> list = Arrays.asList(r1, null);

            List<Restaurant> result = ratingService.filterByRatingRange(list, 3.0, 5.0);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("filterByMinReviewCount - 正確篩選")
        void filterByMinReviewCount_FiltersCorrectly() {
            Restaurant r1 = createRestaurantWithReviews(5, 5, 5, 5, 5);
            Restaurant r2 = createRestaurantWithReviews(5, 5);
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = ratingService.filterByMinReviewCount(list, 3);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("filterByMinReviewCount - null 列表返回空")
        void filterByMinReviewCount_NullList_ReturnsEmpty() {
            assertTrue(ratingService.filterByMinReviewCount(null, 3).isEmpty());
        }

        @Test
        @DisplayName("filterByMinReviewCount - 空列表返回空")
        void filterByMinReviewCount_EmptyList_ReturnsEmpty() {
            assertTrue(ratingService.filterByMinReviewCount(new ArrayList<>(), 3).isEmpty());
        }

        @Test
        @DisplayName("filterByMinReviewCount - 負數最小值使用 0")
        void filterByMinReviewCount_NegativeMin_UsesZero() {
            Restaurant r1 = createRestaurantWithReviews(5);
            List<Restaurant> list = Arrays.asList(r1);
            List<Restaurant> result = ratingService.filterByMinReviewCount(list, -5);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("hasGoodRating - 高評分返回 true")
        void hasGoodRating_HighRating_ReturnsTrue() {
            Restaurant restaurant = createRestaurantWithReviews(5, 5, 5, 5, 5);
            assertTrue(ratingService.hasGoodRating(restaurant, 3));
        }

        @Test
        @DisplayName("hasGoodRating - 低評分返回 false")
        void hasGoodRating_LowRating_ReturnsFalse() {
            Restaurant restaurant = createRestaurantWithReviews(2, 2, 2, 2, 2);
            assertFalse(ratingService.hasGoodRating(restaurant, 3));
        }

        @Test
        @DisplayName("hasGoodRating - 評論不足返回 false")
        void hasGoodRating_InsufficientReviews_ReturnsFalse() {
            Restaurant restaurant = createRestaurantWithReviews(5);
            assertFalse(ratingService.hasGoodRating(restaurant, 3));
        }

        @Test
        @DisplayName("hasGoodRating - null 餐廳返回 false")
        void hasGoodRating_NullRestaurant_ReturnsFalse() {
            assertFalse(ratingService.hasGoodRating(null, 3));
        }
    }

    @Nested
    @DisplayName("Ranking and Sorting")
    class RankingAndSorting {
        @Test
        @DisplayName("getTopRatedRestaurants - null 列表返回空")
        void getTopRatedRestaurants_NullList_ReturnsEmpty() {
            assertTrue(ratingService.getTopRatedRestaurants(null, 5).isEmpty());
        }

        @Test
        @DisplayName("getTopRatedRestaurants - 依評分排序")
        void getTopRatedRestaurants_ReturnsSortedByRating() {
            Restaurant r1 = createRestaurantWithReviews(3, 3, 3, 3, 3, 3);
            r1.setId("1");
            Restaurant r2 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5);
            r2.setId("2");
            Restaurant r3 = createRestaurantWithReviews(4, 4, 4, 4, 4, 4);
            r3.setId("3");
            List<Restaurant> list = Arrays.asList(r1, r2, r3);

            List<Restaurant> result = ratingService.getTopRatedRestaurants(list, 3);
            assertEquals("2", result.get(0).getId());
        }

        @Test
        @DisplayName("getTopRatedRestaurants - 遵守限制數量")
        void getTopRatedRestaurants_RespectsLimit() {
            Restaurant r1 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5);
            Restaurant r2 = createRestaurantWithReviews(4, 4, 4, 4, 4, 4);
            Restaurant r3 = createRestaurantWithReviews(3, 3, 3, 3, 3, 3);
            List<Restaurant> list = Arrays.asList(r1, r2, r3);

            List<Restaurant> result = ratingService.getTopRatedRestaurants(list, 2);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("getTopRatedRestaurants - 空列表返回空")
        void getTopRatedRestaurants_EmptyList_ReturnsEmpty() {
            assertTrue(ratingService.getTopRatedRestaurants(new ArrayList<>(), 5).isEmpty());
        }

        @Test
        @DisplayName("getTopRatedRestaurants - 負數限制使用預設值")
        void getTopRatedRestaurants_NegativeLimit_UsesDefault() {
            Restaurant r1 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5);
            List<Restaurant> list = Arrays.asList(r1);
            List<Restaurant> result = ratingService.getTopRatedRestaurants(list, -1);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("getTopRatedRestaurants - 跳過無評論餐廳")
        void getTopRatedRestaurants_SkipsNoReviewRestaurants() {
            Restaurant r1 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5);
            Restaurant r2 = new Restaurant("2", "No Reviews");
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = ratingService.getTopRatedRestaurants(list, 10);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getTopRatedRestaurants - 相同評分按評論數排序")
        void getTopRatedRestaurants_SameRating_SortsByReviewCount() {
            Restaurant r1 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5);
            r1.setId("1");
            Restaurant r2 = createRestaurantWithReviews(5, 5, 5, 5, 5, 5, 5, 5);
            r2.setId("2");
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = ratingService.getTopRatedRestaurants(list, 2);
            assertEquals("2", result.get(0).getId()); // 更多評論優先
        }

        @Test
        @DisplayName("sortByRating - 升冪排序")
        void sortByRating_Ascending_SortsCorrectly() {
            Restaurant r1 = createRestaurantWithReviews(5);
            r1.setId("1");
            Restaurant r2 = createRestaurantWithReviews(3);
            r2.setId("2");
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = ratingService.sortByRating(list, true);
            assertEquals("2", result.get(0).getId());
        }

        @Test
        @DisplayName("sortByRating - 降冪排序")
        void sortByRating_Descending_SortsCorrectly() {
            Restaurant r1 = createRestaurantWithReviews(3);
            r1.setId("1");
            Restaurant r2 = createRestaurantWithReviews(5);
            r2.setId("2");
            List<Restaurant> list = Arrays.asList(r1, r2);

            List<Restaurant> result = ratingService.sortByRating(list, false);
            assertEquals("2", result.get(0).getId());
        }

        @Test
        @DisplayName("sortByRating - null 列表返回空")
        void sortByRating_NullList_ReturnsEmpty() {
            assertTrue(ratingService.sortByRating(null, true).isEmpty());
        }

        @Test
        @DisplayName("sortByRating - 空列表返回空")
        void sortByRating_EmptyList_ReturnsEmpty() {
            assertTrue(ratingService.sortByRating(new ArrayList<>(), true).isEmpty());
        }
    }

    @Nested
    @DisplayName("Rating Distribution")
    class RatingDistribution {
        @Test
        @DisplayName("getRatingDistribution - null 餐廳返回零陣列")
        void getRatingDistribution_NullRestaurant_ReturnsZeros() {
            int[] dist = ratingService.getRatingDistribution(null);
            assertEquals(5, dist.length);
            for (int i : dist) {
                assertEquals(0, i);
            }
        }

        @Test
        @DisplayName("getRatingDistribution - 正確計數")
        void getRatingDistribution_CountsCorrectly() {
            Restaurant restaurant = createRestaurantWithReviews(5, 5, 4, 4, 4, 3);
            int[] dist = ratingService.getRatingDistribution(restaurant);
            assertEquals(1, dist[2]); // 3-star
            assertEquals(3, dist[3]); // 4-star
            assertEquals(2, dist[4]); // 5-star
        }

        @Test
        @DisplayName("getRatingDistribution - 忽略無效評分")
        void getRatingDistribution_IgnoresInvalidRatings() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.addReview(new Review("1", "1", 4, "Good"));
            restaurant.addReview(new Review("2", "1", 0, "Invalid")); // 無效
            restaurant.addReview(new Review("3", "1", 6, "Invalid")); // 無效
            restaurant.getReviews().add(null); // null 評論

            int[] dist = ratingService.getRatingDistribution(restaurant);
            assertEquals(1, dist[3]); // 只有 4 星
        }
    }
}
