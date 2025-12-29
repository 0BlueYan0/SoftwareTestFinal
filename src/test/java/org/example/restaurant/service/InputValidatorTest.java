package org.example.restaurant.service;

import org.example.restaurant.exception.ValidationException;
import org.example.restaurant.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    private InputValidator validator;
    private Restaurant validRestaurant;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
        validRestaurant = new Restaurant("1", "Valid Restaurant");
        validRestaurant.setPhoneNumber("0912345678");
    }

    @Nested
    @DisplayName("Restaurant Validation")
    class RestaurantValidation {
        @Test
        @DisplayName("validateRestaurant - null 餐廳拋出異常")
        void validateRestaurant_NullRestaurant_ThrowsException() {
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(null));
        }

        @Test
        @DisplayName("validateRestaurant - null ID 拋出異常")
        void validateRestaurant_NullId_ThrowsException() {
            Restaurant restaurant = new Restaurant();
            restaurant.setName("Test");
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 空 ID 拋出異常")
        void validateRestaurant_EmptyId_ThrowsException() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId("  ");
            restaurant.setName("Test");
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - ID 過長拋出異常")
        void validateRestaurant_IdTooLong_ThrowsException() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId("a".repeat(51));
            restaurant.setName("Test");
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("isValidPhoneNumber - Null phone via validateRestaurant")
        void validateRestaurant_NullPhone_Success() {
            validRestaurant.setPhoneNumber(null);
            assertDoesNotThrow(() -> validator.validateRestaurant(validRestaurant));
        }

        @Test
        @DisplayName("isValidPhoneNumber - Empty phone via validateRestaurant")
        void validateRestaurant_EmptyPhone_Success() {
            validRestaurant.setPhoneNumber("");
            assertDoesNotThrow(() -> validator.validateRestaurant(validRestaurant));
        }

        @Test
        @DisplayName("isValidPhoneNumber - Invalid characters via validateRestaurant")
        void validateRestaurant_InvalidChars_ThrowsException() {
            validRestaurant.setPhoneNumber("0912-abc-789");
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateRestaurant(validRestaurant);
            });
            assertEquals("phoneNumber", exception.getField());
        }

        @Test
        @DisplayName("isValidPhoneNumber - Too short via validateRestaurant")
        void validateRestaurant_TooShort_ThrowsException() {
            validRestaurant.setPhoneNumber("123456"); // 6 digits
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateRestaurant(validRestaurant);
            });
            assertEquals("phoneNumber", exception.getField());
        }

        @Test
        @DisplayName("isValidPhoneNumber - Too long via validateRestaurant")
        void validateRestaurant_TooLong_ThrowsException() {
            validRestaurant.setPhoneNumber("1234567890123456"); // 16 digits
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateRestaurant(validRestaurant);
            });
            assertEquals("phoneNumber", exception.getField());
        }

        @Test
        @DisplayName("isValidPhoneNumber - Valid with format via validateRestaurant")
        void validateRestaurant_ValidWithFormat_Success() {
            validRestaurant.setPhoneNumber("(02) 1234-5678"); // Valid format
            assertDoesNotThrow(() -> validator.validateRestaurant(validRestaurant));
        }

        @Test
        @DisplayName("validateRestaurant - null 名稱拋出異常")
        void validateRestaurant_NullName_ThrowsException() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId("1");
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 名稱過短拋出異常")
        void validateRestaurant_NameTooShort_ThrowsException() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId("1");
            restaurant.setName("A");
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 名稱過長拋出異常")
        void validateRestaurant_NameTooLong_ThrowsException() {
            Restaurant restaurant = new Restaurant();
            restaurant.setId("1");
            restaurant.setName("a".repeat(101));
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 無效價格等級拋出異常")
        void validateRestaurant_InvalidPriceLevel_ThrowsException() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setPriceLevel(5);
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 負數平均價格拋出異常")
        void validateRestaurant_NegativeAveragePrice_ThrowsException() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setAveragePrice(-100);
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 有效電話號碼通過")
        void validateRestaurant_ValidPhoneNumber_Passes() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setPhoneNumber("02-1234-5678");
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 無效電話號碼拋出異常")
        void validateRestaurant_InvalidPhoneNumber_ThrowsException() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setPhoneNumber("abc");
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 有效網站通過")
        void validateRestaurant_ValidWebsite_Passes() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setWebsite("https://example.com");
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 無效網站拋出異常")
        void validateRestaurant_InvalidWebsite_ThrowsException() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setWebsite("not-a-url");
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 有效餐廳通過")
        void validateRestaurant_ValidRestaurant_Passes() {
            Restaurant restaurant = new Restaurant("1", "Valid Restaurant");
            restaurant.setPriceLevel(2);
            restaurant.setAveragePrice(300);
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 負數容量拋出異常")
        void validateRestaurant_NegativeCapacity_ThrowsException() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setCapacity(-1);
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 帶有有效位置通過")
        void validateRestaurant_WithValidLocation_Passes() {
            Restaurant restaurant = new Restaurant("1", "Test Restaurant");
            restaurant.setLocation(new Location(25.0, 121.0));
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 帶有有效營業時間通過")
        void validateRestaurant_WithValidBusinessHours_Passes() {
            Restaurant restaurant = new Restaurant("1", "Test Restaurant");
            BusinessHours hours = new BusinessHours();
            hours.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
            restaurant.setBusinessHours(hours);
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 負價格等級拋出異常")
        void validateRestaurant_NegativePriceLevel_ThrowsException() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setPriceLevel(-1);
            assertThrows(ValidationException.class, () -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - http 網站通過")
        void validateRestaurant_HttpWebsite_Passes() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setWebsite("http://example.com");
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 有效國際電話號碼通過")
        void validateRestaurant_InternationalPhoneNumber_Passes() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setPhoneNumber("+886 2 1234 5678");
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }

        @Test
        @DisplayName("validateRestaurant - 電話號碼包含特殊字符通過")
        void validateRestaurant_PhoneWithSpecialChars_Passes() {
            Restaurant restaurant = new Restaurant("1", "Test");
            restaurant.setPhoneNumber("(02) 1234-5678");
            assertDoesNotThrow(() -> validator.validateRestaurant(restaurant));
        }
    }

    @Nested
    @DisplayName("Business Hours Validation")
    class BusinessHoursValidation {
        @Test
        @DisplayName("validateBusinessHours - BusinessHours cannot be null")
        void validateBusinessHours_NullHours_ThrowsException() {
            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateBusinessHours(null);
            });
            assertEquals("businessHours", exception.getField());
        }

        @Test
        @DisplayName("validateBusinessHours - Open time is required")
        void validateBusinessHours_NullOpenTime_ThrowsException() {
            BusinessHours hours = new BusinessHours();
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(null, LocalTime.of(18, 0));
            hours.getWeeklyHours().put(DayOfWeek.MONDAY, slot);

            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateBusinessHours(hours);
            });
            assertEquals("openTime", exception.getField());
        }

        @Test
        @DisplayName("validateBusinessHours - Close time is required")
        void validateBusinessHours_NullCloseTime_ThrowsException() {
            BusinessHours hours = new BusinessHours();
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(LocalTime.of(9, 0), null);
            hours.getWeeklyHours().put(DayOfWeek.MONDAY, slot);

            ValidationException exception = assertThrows(ValidationException.class, () -> {
                validator.validateBusinessHours(hours);
            });
            assertEquals("closeTime", exception.getField());
        }

        @Test
        @DisplayName("validateBusinessHours - Valid hours")
        void validateBusinessHours_Valid_Success() {
            BusinessHours hours = new BusinessHours();
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(LocalTime.of(9, 0), LocalTime.of(18, 0));
            hours.getWeeklyHours().put(DayOfWeek.MONDAY, slot);

            assertDoesNotThrow(() -> validator.validateBusinessHours(hours));
        }

        @Test
        @DisplayName("validateBusinessHours - Weekly hours is null (Valid)")
        void validateBusinessHours_NullWeeklyHours_Success() throws Exception {
            BusinessHours hours = new BusinessHours();
            // Use reflection to set weeklyHours to null since there's no setter and getter
            // initializes it
            java.lang.reflect.Field field = BusinessHours.class.getDeclaredField("weeklyHours");
            field.setAccessible(true);
            field.set(hours, null);

            assertDoesNotThrow(() -> validator.validateBusinessHours(hours));
        }

        @Test
        @DisplayName("validateBusinessHours - Null slot in map (Valid)")
        void validateBusinessHours_NullSlot_Success() {
            BusinessHours hours = new BusinessHours();
            hours.getWeeklyHours().put(DayOfWeek.MONDAY, null);

            assertDoesNotThrow(() -> validator.validateBusinessHours(hours));
        }

        @Test
        @DisplayName("validateBusinessHours - null 營業時間通過")
        void validateBusinessHours_NullWeeklyHours_Passes() {
            BusinessHours hours = new BusinessHours();
            assertDoesNotThrow(() -> validator.validateBusinessHours(hours));
        }

        @Test
        @DisplayName("validateBusinessHours - 有效時間通過")
        void validateBusinessHours_ValidHours_Passes() {
            BusinessHours hours = new BusinessHours();
            hours.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
            assertDoesNotThrow(() -> validator.validateBusinessHours(hours));
        }
    }

    @Nested
    @DisplayName("Location Validation")
    class LocationValidation {
        @Test
        @DisplayName("validateLocation - null 位置拋出異常")
        void validateLocation_NullLocation_ThrowsException() {
            assertThrows(ValidationException.class, () -> validator.validateLocation(null));
        }

        @Test
        @DisplayName("validateLocation - 無效緯度拋出異常")
        void validateLocation_InvalidLatitude_ThrowsException() {
            Location location = new Location(91, 0);
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 無效經度拋出異常")
        void validateLocation_InvalidLongitude_ThrowsException() {
            Location location = new Location(0, 181);
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 城市名稱過長拋出異常")
        void validateLocation_CityTooLong_ThrowsException() {
            Location location = new Location(25.0, 121.5);
            location.setCity("a".repeat(101));
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 有效位置通過")
        void validateLocation_ValidLocation_Passes() {
            Location location = new Location(25.0330, 121.5654, "台北市信義區", "台北市");
            assertDoesNotThrow(() -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 緯度低於 -90 拋出異常")
        void validateLocation_LatitudeBelowMinus90_ThrowsException() {
            Location location = new Location(-91, 0);
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 經度低於 -180 拋出異常")
        void validateLocation_LongitudeBelowMinus180_ThrowsException() {
            Location location = new Location(0, -181);
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 城市名稱過短拋出異常")
        void validateLocation_CityTooShort_ThrowsException() {
            Location location = new Location(25.0, 121.5);
            location.setCity("A");
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 地址過長拋出異常")
        void validateLocation_AddressTooLong_ThrowsException() {
            Location location = new Location(25.0, 121.5);
            location.setAddress("a".repeat(201));
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 有效郵遞區號通過")
        void validateLocation_ValidPostalCode_Passes() {
            Location location = new Location(25.0, 121.5);
            location.setPostalCode("110");
            assertDoesNotThrow(() -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 無效郵遞區號拋出異常")
        void validateLocation_InvalidPostalCode_ThrowsException() {
            Location location = new Location(25.0, 121.5);
            location.setPostalCode("@#");
            assertThrows(ValidationException.class, () -> validator.validateLocation(location));
        }

        @Test
        @DisplayName("validateLocation - 字母數字郵遞區號通過")
        void validateLocation_AlphanumericPostalCode_Passes() {
            Location location = new Location(25.0, 121.5);
            location.setPostalCode("ABC-123");
            assertDoesNotThrow(() -> validator.validateLocation(location));
        }
    }

    @Nested
    @DisplayName("Review Validation")
    class ReviewValidation {
        @Test
        @DisplayName("validateReview - null 評論拋出異常")
        void validateReview_NullReview_ThrowsException() {
            assertThrows(ValidationException.class, () -> validator.validateReview(null));
        }

        @Test
        @DisplayName("validateReview - 無效評分拋出異常")
        void validateReview_InvalidRating_ThrowsException() {
            Review review = new Review("1", "r1", 6, "Good");
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 評分低於 1 拋出異常")
        void validateReview_RatingBelowOne_ThrowsException() {
            Review review = new Review("1", "r1", 0, "Good");
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 評論過長拋出異常")
        void validateReview_CommentTooLong_ThrowsException() {
            Review review = new Review("1", "r1", 4, "a".repeat(2001));
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 評論包含不當內容拋出異常")
        void validateReview_ProfanityInComment_ThrowsException() {
            Review review = new Review("1", "r1", 4, "This is spam content");
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 有效評論通過")
        void validateReview_ValidReview_Passes() {
            Review review = new Review("1", "r1", 4, "Great food!");
            assertDoesNotThrow(() -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - null ID 拋出異常")
        void validateReview_NullId_ThrowsException() {
            Review review = new Review();
            review.setRestaurantId("r1");
            review.setRating(4);
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - null 餐廳 ID 拋出異常")
        void validateReview_NullRestaurantId_ThrowsException() {
            Review review = new Review();
            review.setId("1");
            review.setRating(4);
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 無效用戶等級拋出異常")
        void validateReview_InvalidUserLevel_ThrowsException() {
            Review review = new Review("1", "r1", 4, "Good");
            review.setUserLevel(6);
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 用戶等級低於 1 拋出異常")
        void validateReview_UserLevelBelowOne_ThrowsException() {
            Review review = new Review("1", "r1", 4, "Good");
            review.setUserLevel(0);
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 負數幫助數拋出異常")
        void validateReview_NegativeHelpfulCount_ThrowsException() {
            Review review = new Review("1", "r1", 4, "Good");
            review.setHelpfulCount(-1);
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 用戶名過短拋出異常")
        void validateReview_UserNameTooShort_ThrowsException() {
            Review review = new Review("1", "r1", 4, "Good");
            review.setUserName("A");
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 用戶名過長拋出異常")
        void validateReview_UserNameTooLong_ThrowsException() {
            Review review = new Review("1", "r1", 4, "Good");
            review.setUserName("a".repeat(51));
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - scam 內容拋出異常")
        void validateReview_ScamContent_ThrowsException() {
            Review review = new Review("1", "r1", 4, "This is a scam");
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - fake 內容拋出異常")
        void validateReview_FakeContent_ThrowsException() {
            Review review = new Review("1", "r1", 4, "This is fake review");
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 無效使用者等級 (0) 拋出異常")
        void validateReview_UserLevelZero_ThrowsException() {
            Review review = new Review("1", "r1", 4, "Good");
            review.setUserLevel(0);
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }

        @Test
        @DisplayName("validateReview - 無效使用者等級 (>5) 拋出異常")
        void validateReview_UserLevelAboveFive_ThrowsException() {
            Review review = new Review("1", "r1", 4, "Good");
            review.setUserLevel(6);
            assertThrows(ValidationException.class, () -> validator.validateReview(review));
        }
    }

    @Nested
    @DisplayName("Search Criteria Validation")
    class SearchCriteriaValidation {
        @Test
        @DisplayName("validateSearchCriteria - null 條件拋出異常")
        void validateSearchCriteria_NullCriteria_ThrowsException() {
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(null));
        }

        @Test
        @DisplayName("validateSearchCriteria - 關鍵字過長拋出異常")
        void validateSearchCriteria_KeywordTooLong_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setKeyword("a".repeat(101));
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 無效評分範圍拋出異常")
        void validateSearchCriteria_InvalidRatingRange_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMinRating(4.0);
            criteria.setMaxRating(2.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 無效價格等級拋出異常")
        void validateSearchCriteria_InvalidPriceLevel_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setPriceLevel(5);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 有效條件通過")
        void validateSearchCriteria_ValidCriteria_Passes() {
            SearchCriteria criteria = new SearchCriteria()
                    .keyword("pizza")
                    .minRating(3.0)
                    .maxRating(5.0);
            assertDoesNotThrow(() -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 最小評分低於 0 拋出異常")
        void validateSearchCriteria_MinRatingBelowZero_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMinRating(-1.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 最小評分高於 5 拋出異常")
        void validateSearchCriteria_MinRatingAboveFive_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMinRating(6.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 最大評分低於 0 拋出異常")
        void validateSearchCriteria_MaxRatingBelowZero_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMaxRating(-1.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 最大評分高於 5 拋出異常")
        void validateSearchCriteria_MaxRatingAboveFive_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMaxRating(6.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 負數最小價格拋出異常")
        void validateSearchCriteria_NegativeMinPrice_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMinPrice(-1.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 負數最大價格拋出異常")
        void validateSearchCriteria_NegativeMaxPrice_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMaxPrice(-1.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 最小價格高於最大價格拋出異常")
        void validateSearchCriteria_MinPriceExceedsMax_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMinPrice(100.0);
            criteria.setMaxPrice(50.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 價格等級低於 1 拋出異常")
        void validateSearchCriteria_PriceLevelBelowOne_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setPriceLevel(0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 負數偏移拋出異常")
        void validateSearchCriteria_NegativeOffset_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setOffset(-1);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 限制低於 1 拋出異常")
        void validateSearchCriteria_LimitBelowOne_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLimit(0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 限制高於 100 拋出異常")
        void validateSearchCriteria_LimitAbove100_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLimit(101);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 最小評分大於最大評分拋出異常")
        void validateSearchCriteria_MinRatingExceedsMax_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setMinRating(5.0);
            criteria.setMaxRating(3.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 位置篩選無效緯度拋出異常")
        void validateSearchCriteria_LocationFilterInvalidLatitude_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLatitude(100.0);
            criteria.setLongitude(121.0);
            criteria.setRadiusKm(5.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 位置篩選無效經度拋出異常")
        void validateSearchCriteria_LocationFilterInvalidLongitude_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLatitude(25.0);
            criteria.setLongitude(200.0);
            criteria.setRadiusKm(5.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 位置篩選無效半徑 (>100) 拋出異常")
        void validateSearchCriteria_LocationFilterRadiusTooLarge_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLatitude(25.0);
            criteria.setLongitude(121.0);
            criteria.setRadiusKm(150.0);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 位置篩選無效半徑 (<=0) 不拋出異常（因為 hasLocationFilter 為 false）")
        void validateSearchCriteria_LocationFilterRadiusZeroOrNegative_NoException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLatitude(25.0);
            criteria.setLongitude(121.0);
            criteria.setRadiusKm(-1.0);
            assertDoesNotThrow(() -> validator.validateSearchCriteria(criteria));
        }

        @Test
        @DisplayName("validateSearchCriteria - 無效價格等級 (>4) 拋出異常")
        void validateSearchCriteria_PriceLevelAboveFour_ThrowsException() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setPriceLevel(5);
            assertThrows(ValidationException.class, () -> validator.validateSearchCriteria(criteria));
        }
    }

    @Nested
    @DisplayName("Menu Item Validation")
    class MenuItemValidation {
        @Test
        @DisplayName("validateMenuItem - null 項目拋出異常")
        void validateMenuItem_NullItem_ThrowsException() {
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(null));
        }

        @Test
        @DisplayName("validateMenuItem - 負數價格拋出異常")
        void validateMenuItem_NegativePrice_ThrowsException() {
            MenuItem item = new MenuItem("1", "Pizza", -50);
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - 價格過高拋出異常")
        void validateMenuItem_PriceTooHigh_ThrowsException() {
            MenuItem item = new MenuItem("1", "Pizza", 200000);
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - 有效項目通過")
        void validateMenuItem_ValidItem_Passes() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            assertDoesNotThrow(() -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - null ID 拋出異常")
        void validateMenuItem_NullId_ThrowsException() {
            MenuItem item = new MenuItem();
            item.setName("Pizza");
            item.setPrice(350);
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - null 名稱拋出異常")
        void validateMenuItem_NullName_ThrowsException() {
            MenuItem item = new MenuItem();
            item.setId("1");
            item.setPrice(350);
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - 名稱過長拋出異常")
        void validateMenuItem_NameTooLong_ThrowsException() {
            MenuItem item = new MenuItem("1", "a".repeat(101), 350);
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - 負數卡路里拋出異常")
        void validateMenuItem_NegativeCalories_ThrowsException() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            item.setCalories(-100);
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - 描述過長拋出異常")
        void validateMenuItem_DescriptionTooLong_ThrowsException() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            item.setDescription("a".repeat(501));
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }

        @Test
        @DisplayName("validateMenuItem - 類別過長拋出異常")
        void validateMenuItem_CategoryTooLong_ThrowsException() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            item.setCategory("a".repeat(51));
            assertThrows(ValidationException.class, () -> validator.validateMenuItem(item));
        }
    }

    @Nested
    @DisplayName("Private Method Reflection Tests")
    class ReflectionTests {
        @Test
        @DisplayName("isValidPhoneNumber - Private method reflection test for Null/Empty")
        void isValidPhoneNumber_ReflectionValues() throws Exception {
            java.lang.reflect.Method method = InputValidator.class.getDeclaredMethod("isValidPhoneNumber",
                    String.class);
            method.setAccessible(true);

            // Test null
            assertFalse((Boolean) method.invoke(validator, (String) null));

            // Test empty
            assertFalse((Boolean) method.invoke(validator, ""));
        }
    }
}
