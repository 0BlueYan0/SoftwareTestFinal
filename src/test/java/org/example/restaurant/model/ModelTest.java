package org.example.restaurant.model;

import org.example.restaurant.exception.RestaurantNotFoundException;
import org.example.restaurant.exception.ValidationException;
import org.example.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    // Restaurant tests
    @Nested
    @DisplayName("Restaurant Tests")
    class RestaurantTests {
        @Test
        @DisplayName("具有主要菜系類型的 hasCuisineType")
        void restaurant_HasCuisineTypePrimary() {
            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            assertTrue(r.hasCuisineType(CuisineType.JAPANESE));
        }

        @Test
        @DisplayName("具有附加菜系類型的 hasCuisineType")
        void restaurant_HasCuisineTypeAdditional() {
            Restaurant r = new Restaurant("1", "Test");
            r.addCuisineType(CuisineType.SEAFOOD);
            assertTrue(r.hasCuisineType(CuisineType.SEAFOOD));
        }

        @Test
        @DisplayName("hasCuisineType null returns false")
        void restaurant_HasCuisineTypeNull() {
            Restaurant r = new Restaurant("1", "Test");
            assertFalse(r.hasCuisineType(null));
        }

        @Test
        @DisplayName("hasCuisineType 不存在的菜系返回 false")
        void restaurant_HasCuisineTypeNotExist() {
            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            assertFalse(r.hasCuisineType(CuisineType.CHINESE));
        }

        @Test
        @DisplayName("calculateAverageRating 正常計算")
        void restaurant_CalculateAverageRating() {
            Restaurant r = new Restaurant("1", "Test");
            r.addReview(new Review("1", "1", 4, "Good"));
            r.addReview(new Review("2", "1", 5, "Great"));
            assertEquals(4.5, r.calculateAverageRating());
        }

        @Test
        @DisplayName("calculateAverageRating 空評論返回 0")
        void restaurant_CalculateAverageRatingEmpty() {
            Restaurant r = new Restaurant("1", "Test");
            assertEquals(0.0, r.calculateAverageRating());
        }

        @Test
        @DisplayName("calculateAverageRating 忽略無效評分")
        void restaurant_CalculateAverageRatingInvalidRating() {
            Restaurant r = new Restaurant("1", "Test");
            r.addReview(new Review("1", "1", 4, "Good"));
            r.addReview(new Review("2", "1", 6, "Invalid")); // 無效評分 > 5
            r.addReview(new Review("3", "1", 0, "Invalid")); // 無效評分 < 1
            assertEquals(4.0, r.calculateAverageRating()); // 只計算有效評分
        }

        @Test
        @DisplayName("calculateAverageRating 忽略 null 評論")
        void restaurant_CalculateAverageRatingNullReview() {
            Restaurant r = new Restaurant("1", "Test");
            r.addReview(new Review("1", "1", 4, "Good"));
            r.getReviews().add(null);
            assertEquals(4.0, r.calculateAverageRating());
        }

        @Test
        @DisplayName("calculateMenuAveragePrice 正常計算")
        void restaurant_CalculateMenuAveragePrice() {
            Restaurant r = new Restaurant("1", "Test");
            MenuItem item1 = new MenuItem("1", "Item1", 100);
            item1.setAvailable(true);
            MenuItem item2 = new MenuItem("2", "Item2", 200);
            item2.setAvailable(true);
            r.addMenuItem(item1);
            r.addMenuItem(item2);
            assertEquals(150.0, r.calculateMenuAveragePrice());
        }

        @Test
        @DisplayName("calculateMenuAveragePrice 空菜單返回 0")
        void restaurant_CalculateMenuAveragePriceEmpty() {
            Restaurant r = new Restaurant("1", "Test");
            assertEquals(0.0, r.calculateMenuAveragePrice());
        }

        @Test
        @DisplayName("calculateMenuAveragePrice 忽略不可用項目")
        void restaurant_CalculateMenuAveragePriceUnavailable() {
            Restaurant r = new Restaurant("1", "Test");
            MenuItem item1 = new MenuItem("1", "Item1", 100);
            item1.setAvailable(true);
            MenuItem item2 = new MenuItem("2", "Item2", 200);
            item2.setAvailable(false);
            r.addMenuItem(item1);
            r.addMenuItem(item2);
            assertEquals(100.0, r.calculateMenuAveragePrice());
        }

        @Test
        @DisplayName("calculateMenuAveragePrice 忽略價格 <= 0 的項目")
        void restaurant_CalculateMenuAveragePriceInvalidPrice() {
            Restaurant r = new Restaurant("1", "Test");
            MenuItem item1 = new MenuItem("1", "Item1", 100);
            item1.setAvailable(true);
            MenuItem item2 = new MenuItem("2", "Item2", 0);
            item2.setAvailable(true);
            MenuItem item3 = new MenuItem("3", "Item3", -50);
            item3.setAvailable(true);
            r.addMenuItem(item1);
            r.addMenuItem(item2);
            r.addMenuItem(item3);
            assertEquals(100.0, r.calculateMenuAveragePrice());
        }

        @Test
        @DisplayName("calculateMenuAveragePrice 忽略 null 項目")
        void restaurant_CalculateMenuAveragePriceNullItem() {
            Restaurant r = new Restaurant("1", "Test");
            MenuItem item1 = new MenuItem("1", "Item1", 100);
            item1.setAvailable(true);
            r.addMenuItem(item1);
            r.getMenu().add(null);
            assertEquals(100.0, r.calculateMenuAveragePrice());
        }

        @Test
        @DisplayName("matchesKeyword in name")
        void restaurant_MatchesKeywordInName() {
            Restaurant r = new Restaurant("1", "Tokyo Sushi");
            assertTrue(r.matchesKeyword("Tokyo"));
            assertTrue(r.matchesKeyword("Sushi"));
            assertTrue(r.matchesKeyword("tokyo"));
            assertFalse(r.matchesKeyword("Taipei"));
        }

        @Test
        @DisplayName("matchesKeyword in description")
        void restaurant_MatchesKeywordInDescription() {
            Restaurant r = new Restaurant("1", "Test");
            r.setDescription("Best Japanese food in town");
            assertTrue(r.matchesKeyword("Japanese"));
            assertTrue(r.matchesKeyword("food"));
            assertFalse(r.matchesKeyword("Italian"));
        }

        @Test
        @DisplayName("matchesKeyword in cuisineType")
        void restaurant_MatchesKeywordInCuisineType() {
            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            assertTrue(r.matchesKeyword("日式"));
        }

        @Test
        @DisplayName("matchesKeyword in city")
        void restaurant_MatchesKeywordInCity() {
            Restaurant r = new Restaurant("1", "Test");
            r.setLocation(new Location(25.0, 121.0, "Address", "台北市"));
            assertTrue(r.matchesKeyword("台北"));
            assertTrue(r.matchesKeyword("台北市"));
            assertFalse(r.matchesKeyword("台中"));
        }

        @Test
        @DisplayName("matchesKeyword in address")
        void restaurant_MatchesKeywordInAddress() {
            Restaurant r = new Restaurant("1", "Test");
            r.setLocation(new Location(25.0, 121.0, "信義路100號", "台北市"));
            assertTrue(r.matchesKeyword("信義路"));
            assertTrue(r.matchesKeyword("100號"));
        }

        @Test
        @DisplayName("matchesKeyword null returns true")
        void restaurant_MatchesKeywordNull() {
            Restaurant r = new Restaurant("1", "Test");
            assertTrue(r.matchesKeyword(null));
        }

        @Test
        @DisplayName("matchesKeyword empty returns true")
        void restaurant_MatchesKeywordEmpty() {
            Restaurant r = new Restaurant("1", "Test");
            assertTrue(r.matchesKeyword(""));
            assertTrue(r.matchesKeyword("   "));
        }

        @Test
        @DisplayName("matchesKeyword 無配對返回 false")
        void restaurant_MatchesKeywordNoMatch() {
            Restaurant r = new Restaurant("1", "Test");
            assertFalse(r.matchesKeyword("NotExist"));
        }

        @Test
        @DisplayName("addCuisineType null 不會新增")
        void restaurant_AddCuisineTypeNull() {
            Restaurant r = new Restaurant("1", "Test");
            r.addCuisineType(null);
            assertTrue(r.getAdditionalCuisineTypes().isEmpty());
        }

        @Test
        @DisplayName("addMenuItem null 不會新增")
        void restaurant_AddMenuItemNull() {
            Restaurant r = new Restaurant("1", "Test");
            r.addMenuItem(null);
            assertTrue(r.getMenu().isEmpty());
        }

        @Test
        @DisplayName("addReview null 不會新增")
        void restaurant_AddReviewNull() {
            Restaurant r = new Restaurant("1", "Test");
            r.addReview(null);
            assertTrue(r.getReviews().isEmpty());
        }

        @Test
        @DisplayName("isOpenNow businessHours null 返回 false")
        void restaurant_IsOpenNowNull() {
            Restaurant r = new Restaurant("1", "Test");
            assertFalse(r.isOpenNow());
        }

        @Test
        @DisplayName("getReviewCount 正常計數")
        void restaurant_GetReviewCount() {
            Restaurant r = new Restaurant("1", "Test");
            assertEquals(0, r.getReviewCount());
            r.addReview(new Review("1", "1", 4, "Good"));
            r.addReview(new Review("2", "1", 5, "Great"));
            assertEquals(2, r.getReviewCount());
        }

        @Test
        @DisplayName("getMenuItemCount 正常計數")
        void restaurant_GetMenuItemCount() {
            Restaurant r = new Restaurant("1", "Test");
            assertEquals(0, r.getMenuItemCount());
            r.addMenuItem(new MenuItem("1", "Item1", 100));
            r.addMenuItem(new MenuItem("2", "Item2", 200));
            assertEquals(2, r.getMenuItemCount());
        }

        @Test
        @DisplayName("equals 和 hashCode")
        void restaurant_EqualsAndHashCode() {
            Restaurant r1 = new Restaurant("1", "Test1");
            Restaurant r2 = new Restaurant("1", "Test2"); // 同 ID 不同名字
            Restaurant r3 = new Restaurant("2", "Test1"); // 不同 ID 同名字

            assertEquals(r1, r2); // 相同 ID 應該相等
            assertNotEquals(r1, r3); // 不同 ID 應該不相等
            assertEquals(r1.hashCode(), r2.hashCode());
            assertEquals(r1, r1); // 自己等於自己
            assertNotEquals(r1, null);
            assertNotEquals(r1, "not a restaurant");
        }

        @Test
        @DisplayName("toString 包含必要資訊")
        void restaurant_ToString() {
            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE,
                    new Location(25.0, 121.0, "Address", "台北市"));
            String str = r.toString();
            assertTrue(str.contains("id='1'"));
            assertTrue(str.contains("name='Test'"));
            assertTrue(str.contains("JAPANESE"));
            assertTrue(str.contains("台北市"));
        }

        @Test
        @DisplayName("toString location null 顯示 N/A")
        void restaurant_ToStringLocationNull() {
            Restaurant r = new Restaurant("1", "Test");
            String str = r.toString();
            assertTrue(str.contains("N/A"));
        }

        @Test
        @DisplayName("預設值檢查")
        void restaurant_DefaultValues() {
            Restaurant r = new Restaurant();
            assertNotNull(r.getAdditionalCuisineTypes());
            assertTrue(r.getAdditionalCuisineTypes().isEmpty());
            assertNotNull(r.getMenu());
            assertTrue(r.getMenu().isEmpty());
            assertNotNull(r.getReviews());
            assertTrue(r.getReviews().isEmpty());
            assertTrue(r.isActive()); // 預設是 active
        }

        @Test
        @DisplayName("setter 方法測試")
        void restaurant_Setters() {
            Restaurant r = new Restaurant();

            r.setId("123");
            assertEquals("123", r.getId());

            r.setName("TestName");
            assertEquals("TestName", r.getName());

            r.setDescription("Desc");
            assertEquals("Desc", r.getDescription());

            r.setCuisineType(CuisineType.ITALIAN);
            assertEquals(CuisineType.ITALIAN, r.getCuisineType());

            r.setAveragePrice(350.0);
            assertEquals(350.0, r.getAveragePrice());

            r.setPriceLevel(3);
            assertEquals(3, r.getPriceLevel());

            r.setActive(false);
            assertFalse(r.isActive());

            r.setPhoneNumber("02-12345678");
            assertEquals("02-12345678", r.getPhoneNumber());

            r.setWebsite("https://test.com");
            assertEquals("https://test.com", r.getWebsite());

            r.setCapacity(50);
            assertEquals(50, r.getCapacity());

            r.setHasDelivery(true);
            assertTrue(r.isHasDelivery());

            r.setHasTakeout(true);
            assertTrue(r.isHasTakeout());

            r.setHasParking(true);
            assertTrue(r.isHasParking());

            r.setAcceptsReservations(true);
            assertTrue(r.isAcceptsReservations());
        }
    }

    // Location tests
    @Nested
    @DisplayName("Location Tests")
    class LocationTests {
        @Test
        @DisplayName("有效座標 isValid returns true")
        void location_IsValidTrue() {
            Location loc = new Location(25.0, 121.0);
            assertTrue(loc.isValid());
        }

        @Test
        @DisplayName("無效緯度 isValid returns false")
        void location_IsValidFalseLatitude() {
            Location loc = new Location(91.0, 121.0);
            Location loc2 = new Location(-91.0, 121.0);
            assertFalse(loc.isValid());
            assertFalse(loc2.isValid());
        }

        @Test
        @DisplayName("無效經度 isValid returns false")
        void location_IsValidFalseLongitude() {
            Location loc = new Location(25.0, 181.0);
            Location loc2 = new Location(25.0, -181.0);
            assertFalse(loc.isValid());
            assertFalse(loc2.isValid());
        }

        @Test
        @DisplayName("getFullAddress 組合正確")
        void location_GetFullAddress() {
            Location loc = new Location(25.0, 121.0, "信義路100號", "台北市");
            loc.setPostalCode("106");
            loc.setDistrict("大安區");
            String fullAddress = loc.getFullAddress();
            assertTrue(fullAddress.contains("106"));
            assertTrue(fullAddress.contains("台北市"));
            assertTrue(fullAddress.contains("大安區"));
            assertTrue(fullAddress.contains("信義路100號"));
            assertTrue(fullAddress.contains("台北市大安區信義路100號"));
            assertEquals("106 台北市大安區信義路100號", fullAddress);
        }

        @Test
        @DisplayName("getFullAddress 空 postalCode")
        void location_GetFullAddressEmptyPostalCode() {
            Location loc = new Location(25.0, 121.0, "信義路100號", "台北市");
            loc.setPostalCode("");
            String fullAddress = loc.getFullAddress();
            assertFalse(fullAddress.contains("  ")); // 不應有多餘空格
            assertTrue(fullAddress.contains("台北市"));
        }

        @Test
        @DisplayName("getFullAddress 空 city")
        void location_GetFullAddressEmptyCity() {
            Location loc = new Location(25.0, 121.0, "信義路100號", "");
            String fullAddress = loc.getFullAddress();
            assertTrue(fullAddress.contains("信義路100號"));
        }

        @Test
        @DisplayName("getFullAddress 空 district")
        void location_GetFullAddressEmptyDistrict() {
            Location loc = new Location(25.0, 121.0, "信義路100號", "台北市");
            loc.setDistrict("");
            String fullAddress = loc.getFullAddress();
            assertTrue(fullAddress.contains("台北市信義路100號"));
        }

        @Test
        @DisplayName("getFullAddress 空 address")
        void location_GetFullAddressEmptyAddress() {
            Location loc = new Location(25.0, 121.0, "", "台北市");
            String fullAddress = loc.getFullAddress();
            assertTrue(fullAddress.contains("台北市"));
        }

        @Test
        @DisplayName("getFullAddress 全部為空")
        void location_GetFullAddressAllEmpty() {
            Location loc = new Location(25.0, 121.0);
            String fullAddress = loc.getFullAddress();
            assertEquals("", fullAddress);
        }

        @Test
        @DisplayName("equals 自己等於自己")
        void location_EqualsSelf() {
            Location loc = new Location(25.0, 121.0);
            assertEquals(loc, loc);
        }

        @Test
        @DisplayName("equals null 返回 false")
        void location_EqualsNull() {
            Location loc = new Location(25.0, 121.0);
            assertNotEquals(loc, null);
        }

        @Test
        @DisplayName("equals 不同類別返回 false")
        void location_EqualsDifferentClass() {
            Location loc = new Location(25.0, 121.0);
            assertNotEquals(loc, "not a location");
        }

        @Test
        @DisplayName("equals 相同座標返回 true")
        void location_EqualsSameCoordinates() {
            Location loc1 = new Location(25.0, 121.0);
            Location loc2 = new Location(25.0, 121.0);
            assertEquals(loc1, loc2);
            assertEquals(loc1.hashCode(), loc2.hashCode());
        }

        @Test
        @DisplayName("equals 不同座標返回 false")
        void location_EqualsDifferentCoordinates() {
            Location loc1 = new Location(25.0, 121.0);
            Location loc2 = new Location(25.1, 121.0);
            Location loc3 = new Location(25.0, 121.1);
            assertNotEquals(loc1, loc2);
            assertNotEquals(loc1, loc3);
        }

        @Test
        @DisplayName("isValid 邊界值 latitude")
        void location_IsValidBoundaryLatitude() {
            assertTrue(new Location(90.0, 0).isValid());
            assertTrue(new Location(-90.0, 0).isValid());
        }

        @Test
        @DisplayName("isValid 邊界值 longitude")
        void location_IsValidBoundaryLongitude() {
            assertTrue(new Location(0, 180.0).isValid());
            assertTrue(new Location(0, -180.0).isValid());
        }

        @Test
        @DisplayName("toString 包含資訊")
        void location_ToString() {
            Location loc = new Location(25.0, 121.0, "Address", "City");
            String str = loc.toString();
            assertTrue(str.contains("Location"));
            assertTrue(str.contains("latitude=25.0"));
            assertTrue(str.contains("longitude=121.0"));
        }

        @Test
        @DisplayName("預設建構子")
        void location_DefaultConstructor() {
            Location loc = new Location();
            assertEquals(0.0, loc.getLatitude());
            assertEquals(0.0, loc.getLongitude());
        }

        @Test
        @DisplayName("setter 方法")
        void location_Setters() {
            Location loc = new Location();
            loc.setLatitude(25.0);
            assertEquals(25.0, loc.getLatitude());
            loc.setLongitude(121.0);
            assertEquals(121.0, loc.getLongitude());
            loc.setAddress("Address");
            assertEquals("Address", loc.getAddress());
            loc.setCity("City");
            assertEquals("City", loc.getCity());
            loc.setDistrict("District");
            assertEquals("District", loc.getDistrict());
            loc.setPostalCode("100");
            assertEquals("100", loc.getPostalCode());
        }
    }

    // MenuItem tests
    @Nested
    @DisplayName("MenuItem Tests")
    class MenuItemTests {
        @Test
        @DisplayName("matchesDietaryRestrictions all true") // 全飲食限制
        void menuItem_MatchesDietaryRestrictionsAllTrue() {
            MenuItem item = new MenuItem("1", "Salad", 150);
            item.setVegetarian(true);
            item.setVegan(true);
            item.setGlutenFree(true);
            assertTrue(item.matchesDietaryRestrictions(true, true, true));
        }

        @Test
        @DisplayName("matchesDietaryRestrictions fails when not vegan") // 不純素食
        void menuItem_MatchesDietaryRestrictionsFailsNotVegan() {
            MenuItem item = new MenuItem("1", "Cheese", 150);
            item.setVegetarian(true);
            item.setVegan(false);
            assertFalse(item.matchesDietaryRestrictions(false, true, false));
        }

        @Test
        @DisplayName("matchesDietaryRestrictions fails when not vegetarian") // 不素食
        void menuItem_MatchesDietaryRestrictionsFailsNotVegetarian() {
            MenuItem item = new MenuItem("1", "Meat", 150);
            item.setVegetarian(false);
            assertFalse(item.matchesDietaryRestrictions(true, false, false));
        }

        @Test
        @DisplayName("matchesDietaryRestrictions fails when not gluten free") // 不無麩質
        void menuItem_MatchesDietaryRestrictionsFailsNotGlutenFree() {
            MenuItem item = new MenuItem("1", "Bread", 150);
            item.setGlutenFree(false);
            item.setVegetarian(true);
            assertFalse(item.matchesDietaryRestrictions(false, false, true));
        }

        @Test
        @DisplayName("matchesDietaryRestrictions fails when not vegan and not vegetarian") // 不純素食且不素食
        void menuItem_MatchesDietaryRestrictionsFailsNotVeganNotVegetarian() {
            MenuItem item = new MenuItem("1", "Cheese", 150);
            item.setVegetarian(false);
            item.setVegan(false);
            assertFalse(item.matchesDietaryRestrictions(true, true, true));
        }

        @Test
        @DisplayName("matchesDietaryRestrictions fails when not vegan and not gluten free") // 不純素食且不無麩質
        void menuItem_MatchesDietaryRestrictionsFailsNotVeganNotGlutenFree() {
            MenuItem item = new MenuItem("1", "Cheese", 150);
            item.setVegan(false);
            item.setGlutenFree(false);
            assertFalse(item.matchesDietaryRestrictions(false, false, true));
        }

        @Test
        @DisplayName("isInPriceRange 正常範圍")
        void menuItem_IsInPriceRangeTrue() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            assertTrue(item.isInPriceRange(200, 500));
            assertTrue(item.isInPriceRange(350, 500));
            assertTrue(item.isInPriceRange(200, 350));
        }

        @Test
        @DisplayName("isInPriceRange 無效範圍")
        void menuItem_IsInPriceRangeInvalid() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            assertFalse(item.isInPriceRange(500, 200));
            assertFalse(item.isInPriceRange(200, 100));
            assertFalse(item.isInPriceRange(100, 200));
        }

        @Test
        @DisplayName("isInPriceRange 有負值")
        void menuItem_IsInPriceRangeNegative() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            assertFalse(item.isInPriceRange(-100, 500));
            assertFalse(item.isInPriceRange(200, -100));
            assertFalse(item.isInPriceRange(-100, -100));
        }

        @Test
        @DisplayName("matchesDietaryRestrictions no requirements")
        void menuItem_MatchesDietaryRestrictionsNoRequirements() {
            MenuItem item = new MenuItem("1", "Pizza", 150);
            assertTrue(item.matchesDietaryRestrictions(false, false, false));
        }

        @Test
        @DisplayName("equals 自己等於自己")
        void menuItem_EqualsSelf() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            assertEquals(item, item);
        }

        @Test
        @DisplayName("equals null 返回 false")
        void menuItem_EqualsNull() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            assertNotEquals(item, null);
        }

        @Test
        @DisplayName("equals 不同類別返回 false")
        void menuItem_EqualsDifferentClass() {
            MenuItem item = new MenuItem("1", "Pizza", 350);
            assertNotEquals(item, "not a menuItem");
        }

        @Test
        @DisplayName("equals 相同 ID 返回 true")
        void menuItem_EqualsSameId() {
            MenuItem item1 = new MenuItem("1", "Pizza", 350);
            MenuItem item2 = new MenuItem("1", "Different", 500);
            assertEquals(item1, item2);
            assertEquals(item1.hashCode(), item2.hashCode());
        }

        @Test
        @DisplayName("equals 不同 ID 返回 false")
        void menuItem_EqualsDifferentId() {
            MenuItem item1 = new MenuItem("1", "Pizza", 350);
            MenuItem item2 = new MenuItem("2", "Pizza", 350);
            assertNotEquals(item1, item2);
        }

        @Test
        @DisplayName("toString 包含資訊")
        void menuItem_ToString() {
            MenuItem item = new MenuItem("1", "Pizza", 350, "Main");
            String str = item.toString();
            assertTrue(str.contains("id='1'"));
            assertTrue(str.contains("name='Pizza'"));
            assertTrue(str.contains("price=350"));
            assertTrue(str.contains("category='Main'"));
        }

        @Test
        @DisplayName("預設建構子")
        void menuItem_DefaultConstructor() {
            MenuItem item = new MenuItem();
            assertTrue(item.isAvailable()); // 預設可用
        }

        @Test
        @DisplayName("四參數建構子")
        void menuItem_FourArgConstructor() {
            MenuItem item = new MenuItem("1", "Pizza", 350, "Main");
            assertEquals("1", item.getId());
            assertEquals("Pizza", item.getName());
            assertEquals(350, item.getPrice());
            assertEquals("Main", item.getCategory());
        }

        @Test
        @DisplayName("setter 方法")
        void menuItem_Setters() {
            MenuItem item = new MenuItem();
            item.setId("1");
            assertEquals("1", item.getId());
            item.setName("Pizza");
            assertEquals("Pizza", item.getName());
            item.setDescription("Delicious");
            assertEquals("Delicious", item.getDescription());
            item.setPrice(350);
            assertEquals(350, item.getPrice());
            item.setCategory("Main");
            assertEquals("Main", item.getCategory());
            item.setVegetarian(true);
            assertTrue(item.isVegetarian());
            item.setVegan(true);
            assertTrue(item.isVegan());
            item.setGlutenFree(true);
            assertTrue(item.isGlutenFree());
            item.setSpicy(true);
            assertTrue(item.isSpicy());
            item.setAvailable(false);
            assertFalse(item.isAvailable());
            item.setCalories(500);
            assertEquals(500, item.getCalories());
        }
    }

    // Review tests
    @Nested
    @DisplayName("Review Tests")
    class ReviewTests {
        @Test
        @DisplayName("isValid true for 有效評價")
        void review_IsValidTrue() {
            Review r = new Review("1", "r1", 4, "Good");
            assertTrue(r.isValid());
        }

        @Test
        @DisplayName("isValid false for 无效評價")
        void review_IsValidFalseInvalidRating() {
            Review r = new Review("1", "r1", 6, "Good");
            assertFalse(r.isValid());
            Review r2 = new Review("1", "r1", -1, "Good");
            assertFalse(r2.isValid());
        }

        @Test
        @DisplayName("getWeight 有驗證的增加權重")
        void review_GetWeightVerified() {
            Review verified = new Review("1", "r1", 4, "Good");
            verified.setUserLevel(3);
            verified.setVerified(true);

            Review unverified = new Review("2", "r1", 4, "Good");
            unverified.setUserLevel(3);
            unverified.setVerified(false);

            assertTrue(verified.getWeight() > unverified.getWeight());
        }

        @Test
        @DisplayName("isRecent true for 新評價")
        void review_IsRecentTrue() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setCreatedAt(LocalDateTime.now().minusDays(30));
            assertTrue(r.isRecent());
        }

        @Test
        @DisplayName("isRecent false for 舊評價")
        void review_IsRecentFalse() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setCreatedAt(LocalDateTime.now().minusMonths(7));
            assertFalse(r.isRecent());
        }

        @Test
        @DisplayName("hasUserLevelAtLeast 最低等級檢查")
        void review_HasUserLevelAtLeast() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setUserLevel(3);

            assertTrue(r.hasUserLevelAtLeast(1));
            assertTrue(r.hasUserLevelAtLeast(2));
            assertTrue(r.hasUserLevelAtLeast(3));
            assertFalse(r.hasUserLevelAtLeast(4));
            assertFalse(r.hasUserLevelAtLeast(5));
        }

        @Test
        @DisplayName("isExpertReview 專家評論判斷")
        void review_IsExpertReview() {
            Review expert4 = new Review("1", "r1", 4, "Good");
            expert4.setUserLevel(4);

            Review expert5 = new Review("2", "r1", 4, "Good");
            expert5.setUserLevel(5);

            Review normal = new Review("3", "r1", 4, "Good");
            normal.setUserLevel(3);

            assertTrue(expert4.isExpertReview());
            assertTrue(expert5.isExpertReview());
            assertFalse(normal.isExpertReview());
        }

        @Test
        @DisplayName("compareUserLevelTo 等級比較")
        void review_CompareUserLevelTo() {
            Review r1 = new Review("1", "r1", 4, "Good");
            r1.setUserLevel(3);

            Review r2 = new Review("2", "r1", 4, "Good");
            r2.setUserLevel(5);

            Review r3 = new Review("3", "r1", 4, "Good");
            r3.setUserLevel(3);

            assertTrue(r1.compareUserLevelTo(r2) < 0); // 3 < 5
            assertTrue(r2.compareUserLevelTo(r1) > 0); // 5 > 3
            assertEquals(0, r1.compareUserLevelTo(r3)); // 3 == 3
            assertTrue(r1.compareUserLevelTo(null) > 0); // null 處理
        }

        @Test
        @DisplayName("isValid false 當 restaurantId 為 null")
        void review_IsValidFalseNullRestaurantId() {
            Review r = new Review("1", null, 4, "Good");
            assertFalse(r.isValid());
        }

        @Test
        @DisplayName("isValid false 當 restaurantId 為空")
        void review_IsValidFalseEmptyRestaurantId() {
            Review r = new Review("1", "", 4, "Good");
            assertFalse(r.isValid());

            Review r2 = new Review("1", "   ", 4, "Good");
            assertFalse(r2.isValid());
        }

        @Test
        @DisplayName("isValid false 當 userLevel 無效")
        void review_IsValidFalseInvalidUserLevel() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setUserLevel(0); // < 1
            assertFalse(r.isValid());

            Review r2 = new Review("2", "r1", 4, "Good");
            r2.setUserLevel(6); // > 5
            assertFalse(r2.isValid());
        }

        @Test
        @DisplayName("isValid true 邊界值處理")
        void review_IsValidRatingBoundary() {
            Review r1 = new Review("1", "r1", 1, "Min");
            assertTrue(r1.isValid());

            Review r5 = new Review("2", "r1", 5, "Max");
            assertTrue(r5.isValid());
        }

        @Test
        @DisplayName("getWeight helpfulCount > 5 but <= 10 加成 1.1")
        void review_GetWeightHelpfulCount6To10() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setUserLevel(3);
            r.setHelpfulCount(6);

            Review base = new Review("2", "r1", 4, "Good");
            base.setUserLevel(3);
            base.setHelpfulCount(0);

            assertTrue(r.getWeight() > base.getWeight());
        }

        @Test
        @DisplayName("getWeight helpfulCount > 10 加成 1.2")
        void review_GetWeightHelpfulCountOver10() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setUserLevel(3);
            r.setHelpfulCount(11);

            Review mid = new Review("2", "r1", 4, "Good");
            mid.setUserLevel(3);
            mid.setHelpfulCount(6);

            assertTrue(r.getWeight() > mid.getWeight());
        }

        @Test
        @DisplayName("getWeight 低等級未驗證低 helpfulCount")
        void review_GetWeightMinimum() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setUserLevel(1);
            r.setVerified(false);
            r.setHelpfulCount(0);

            assertEquals(0.2, r.getWeight()); // 1 * 0.2 = 0.2
        }

        @Test
        @DisplayName("isRecent false 當 createdAt 為 null")
        void review_IsRecentNullCreatedAt() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setCreatedAt(null);
            assertFalse(r.isRecent());
        }

        @Test
        @DisplayName("isRecent 邊界剛好 6 個月")
        void review_IsRecentBoundary() {
            Review r = new Review("1", "r1", 4, "Good");
            r.setCreatedAt(LocalDateTime.now().minusMonths(6).plusDays(1));
            assertTrue(r.isRecent()); // 剛好在 6 個月內
        }

        @Test
        @DisplayName("toString 長評論截斷")
        void review_ToStringLongComment() {
            String longComment = "This is a very long comment that exceeds fifty characters and should be truncated";
            Review r = new Review("1", "r1", 4, longComment);
            String str = r.toString();
            assertTrue(str.contains("..."));
            assertFalse(str.contains(longComment)); // 完整評論不應出現
        }

        @Test
        @DisplayName("toString 短評論不截斷")
        void review_ToStringShortComment() {
            String shortComment = "Short comment";
            Review r = new Review("1", "r1", 4, shortComment);
            String str = r.toString();
            assertTrue(str.contains(shortComment));
            assertFalse(str.contains("..."));
        }

        @Test
        @DisplayName("toString null 評論處理")
        void review_ToStringNullComment() {
            Review r = new Review("1", "r1", 4, null);
            String str = r.toString();
            assertTrue(str.contains("comment='null'"));
        }

        @Test
        @DisplayName("equals 和 hashCode")
        void review_EqualsAndHashCode() {
            Review r1 = new Review("1", "r1", 4, "Good");
            Review r2 = new Review("1", "r2", 5, "Different"); // 同 ID 不同內容
            Review r3 = new Review("2", "r1", 4, "Good"); // 不同 ID

            assertEquals(r1, r2); // 相同 ID 應該相等
            assertNotEquals(r1, r3); // 不同 ID 應該不相等
            assertEquals(r1.hashCode(), r2.hashCode()); // 相同 ID hashCode 相同
            assertEquals(r1, r1); // 自己等於自己
            assertNotEquals(r1, null);
            assertNotEquals(r1, "not a review");
        }

        @Test
        @DisplayName("預設值檢查")
        void review_DefaultValues() {
            Review r = new Review();
            assertEquals(1, r.getUserLevel()); // 預設 userLevel 是 1
            assertNotNull(r.getCreatedAt()); // 預設有創建時間
        }

        @Test
        @DisplayName("setter 方法測試")
        void review_Setters() {
            Review r = new Review();

            r.setId("123");
            assertEquals("123", r.getId());

            r.setRestaurantId("rest1");
            assertEquals("rest1", r.getRestaurantId());

            r.setUserId("user1");
            assertEquals("user1", r.getUserId());

            r.setUserName("TestUser");
            assertEquals("TestUser", r.getUserName());

            r.setRating(5);
            assertEquals(5, r.getRating());

            r.setComment("Great!");
            assertEquals("Great!", r.getComment());

            LocalDateTime now = LocalDateTime.now();
            r.setCreatedAt(now);
            assertEquals(now, r.getCreatedAt());

            LocalDateTime updated = LocalDateTime.now().plusDays(1);
            r.setUpdatedAt(updated);
            assertEquals(updated, r.getUpdatedAt());

            r.setHelpfulCount(10);
            assertEquals(10, r.getHelpfulCount());

            r.setVerified(true);
            assertTrue(r.isVerified());

            r.setUserLevel(4);
            assertEquals(4, r.getUserLevel());
        }
    }

    // BusinessHours tests
    @Nested
    @DisplayName("BusinessHours Tests")
    class BusinessHoursTests {
        @Test
        @DisplayName("TimeSlot 包含 overnight") // 時間跨越午夜
        void businessHours_TimeSlotContainsOvernight() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(
                    LocalTime.of(22, 0), LocalTime.of(2, 0));
            assertTrue(slot.contains(LocalTime.of(22, 0))); // 邊界：開始時間
            assertTrue(slot.contains(LocalTime.of(23, 0)));
            assertTrue(slot.contains(LocalTime.of(0, 0))); // 午夜
            assertTrue(slot.contains(LocalTime.of(1, 0)));
            assertTrue(slot.contains(LocalTime.of(2, 0))); // 邊界：結束時間
            assertFalse(slot.contains(LocalTime.of(12, 0)));
            assertFalse(slot.contains(LocalTime.of(21, 0)));
        }

        @Test
        @DisplayName("TimeSlot 包含 null return false") // null 檢查
        void businessHours_TimeSlotContainsNull() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));
            assertFalse(slot.contains(null));
        }

        @Test
        @DisplayName("TimeSlot 正常時段包含") // 一般營業時間
        void businessHours_TimeSlotContainsNormal() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));
            assertTrue(slot.contains(LocalTime.of(9, 0))); // 邊界：開始
            assertTrue(slot.contains(LocalTime.of(12, 0))); // 中間
            assertTrue(slot.contains(LocalTime.of(21, 0))); // 邊界：結束
            assertFalse(slot.contains(LocalTime.of(8, 0))); // 開門前
            assertFalse(slot.contains(LocalTime.of(22, 0))); // 關門後
        }

        @Test
        @DisplayName("setHours 設定營業時間")
        void businessHours_SetHours() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));

            BusinessHours.TimeSlot slot = bh.getHours(DayOfWeek.MONDAY);
            assertNotNull(slot);
            assertEquals(LocalTime.of(9, 0), slot.getOpenTime());
            assertEquals(LocalTime.of(21, 0), slot.getCloseTime());
        }

        @Test
        @DisplayName("setHours null 不會設定")
        void businessHours_SetHoursNull() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.MONDAY, null, LocalTime.of(21, 0));
            assertNull(bh.getHours(DayOfWeek.MONDAY));

            bh.setHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), null);
            assertNull(bh.getHours(DayOfWeek.TUESDAY));
        }

        @Test
        @DisplayName("setClosed 設定休息日")
        void businessHours_SetClosed() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(18, 0));
            bh.setClosed(DayOfWeek.SUNDAY);

            assertNull(bh.getHours(DayOfWeek.SUNDAY));
        }

        @Test
        @DisplayName("isOpenAt 檢查特定時間是否營業")
        void businessHours_IsOpenAt() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
            bh.setHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));

            // 週一 12:00 應該營業
            LocalDateTime mondayNoon = LocalDateTime.of(2025, 12, 29, 12, 0); // 2025/12/29 is Monday
            assertTrue(bh.isOpenAt(mondayNoon));

            // 週一 22:00 應該休息
            LocalDateTime mondayLate = LocalDateTime.of(2025, 12, 29, 22, 0);
            assertFalse(bh.isOpenAt(mondayLate));

            // 週三沒設定應該休息
            LocalDateTime wednesday = LocalDateTime.of(2025, 12, 31, 12, 0);
            assertFalse(bh.isOpenAt(wednesday));
        }

        @Test
        @DisplayName("isOpenAt null return false")
        void businessHours_IsOpenAtNull() {
            BusinessHours bh = new BusinessHours();
            assertFalse(bh.isOpenAt(null));
        }

        @Test
        @DisplayName("getNextOpenTime 取得下次營業時間")
        void businessHours_GetNextOpenTime() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
            bh.setHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));

            // 週一 7:00，下次營業是同天 9:00
            LocalDateTime monday7am = LocalDateTime.of(2025, 12, 29, 7, 0);
            LocalDateTime nextOpen = bh.getNextOpenTime(monday7am);
            assertNotNull(nextOpen);
            assertEquals(LocalDateTime.of(2025, 12, 29, 9, 0), nextOpen);
        }

        @Test
        @DisplayName("getNextOpenTime 已經營業中返回當前時間")
        void businessHours_GetNextOpenTimeAlreadyOpen() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));

            // 週一 12:00 已經營業中
            LocalDateTime mondayNoon = LocalDateTime.of(2025, 12, 29, 12, 0);
            LocalDateTime nextOpen = bh.getNextOpenTime(mondayNoon);
            assertNotNull(nextOpen);
            assertEquals(mondayNoon, nextOpen);
        }

        @Test
        @DisplayName("closedOnHolidays 假日休息設定")
        void businessHours_ClosedOnHolidays() {
            BusinessHours bh = new BusinessHours();

            bh.setClosedOnHolidays(true);
            assertTrue(bh.isClosedOnHolidays());

            bh.setClosedOnHolidays(false);
            assertFalse(bh.isClosedOnHolidays());
        }

        @Test
        @DisplayName("TimeSlot equals 相等性")
        void businessHours_TimeSlotEquals() {
            BusinessHours.TimeSlot slot1 = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));
            BusinessHours.TimeSlot slot2 = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));
            BusinessHours.TimeSlot slot3 = new BusinessHours.TimeSlot(
                    LocalTime.of(10, 0), LocalTime.of(22, 0));

            assertEquals(slot1, slot2);
            assertNotEquals(slot1, slot3);
            assertNotEquals(slot1, null);
            assertEquals(slot1, slot1);
        }

        @Test
        @DisplayName("TimeSlot hashCode 一致性")
        void businessHours_TimeSlotHashCode() {
            BusinessHours.TimeSlot slot1 = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));
            BusinessHours.TimeSlot slot2 = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));

            assertEquals(slot1.hashCode(), slot2.hashCode());
        }

        @Test
        @DisplayName("TimeSlot toString 格式")
        void businessHours_TimeSlotToString() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));
            String str = slot.toString();
            assertTrue(str.contains("09:00"));
            assertTrue(str.contains("21:00"));
        }

        @Test
        @DisplayName("toString 包含所有日期")
        void businessHours_ToString() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
            bh.setClosed(DayOfWeek.SUNDAY);
            String str = bh.toString();
            assertTrue(str.contains("MONDAY"));
            assertTrue(str.contains("SUNDAY"));
            assertTrue(str.contains("Closed"));
            assertTrue(str.contains("BusinessHours"));
        }

        @Test
        @DisplayName("getNextOpenTime null 使用當前時間")
        void businessHours_GetNextOpenTimeNull() {
            BusinessHours bh = new BusinessHours();
            // 設定每天都營業
            for (DayOfWeek day : DayOfWeek.values()) {
                bh.setHours(day, LocalTime.of(0, 0), LocalTime.of(23, 59));
            }
            LocalDateTime nextOpen = bh.getNextOpenTime(null);
            assertNotNull(nextOpen);
        }

        @Test
        @DisplayName("getNextOpenTime 從不營業返回 null")
        void businessHours_GetNextOpenTimeNeverOpens() {
            BusinessHours bh = new BusinessHours();
            // 不設定任何營業時間
            LocalDateTime monday = LocalDateTime.of(2025, 12, 29, 12, 0);
            LocalDateTime nextOpen = bh.getNextOpenTime(monday);
            assertNull(nextOpen);
        }

        @Test
        @DisplayName("getNextOpenTime 跨天找到下次營業")
        void businessHours_GetNextOpenTimeNextDay() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));

            // 週一 22:00 已過營業時間，下次營業是週二 9:00
            LocalDateTime mondayLate = LocalDateTime.of(2025, 12, 29, 22, 0);
            LocalDateTime nextOpen = bh.getNextOpenTime(mondayLate);
            assertNotNull(nextOpen);
            assertEquals(LocalDateTime.of(2025, 12, 30, 9, 0), nextOpen);
        }

        @Test
        @DisplayName("getNextOpenTime 當天已過營業時間")
        void businessHours_GetNextOpenTimePassedToday() {
            BusinessHours bh = new BusinessHours();
            bh.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
            bh.setHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));

            // 週一 13:00 已過當天營業時間
            LocalDateTime mondayAfter = LocalDateTime.of(2025, 12, 29, 13, 0);
            LocalDateTime nextOpen = bh.getNextOpenTime(mondayAfter);
            assertNotNull(nextOpen);
            assertEquals(LocalDateTime.of(2025, 12, 30, 9, 0), nextOpen);
        }

        @Test
        @DisplayName("TimeSlot 預設建構子")
        void businessHours_TimeSlotDefaultConstructor() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot();
            assertNull(slot.getOpenTime());
            assertNull(slot.getCloseTime());
        }

        @Test
        @DisplayName("TimeSlot setter 方法")
        void businessHours_TimeSlotSetters() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot();
            slot.setOpenTime(LocalTime.of(8, 0));
            assertEquals(LocalTime.of(8, 0), slot.getOpenTime());
            slot.setCloseTime(LocalTime.of(20, 0));
            assertEquals(LocalTime.of(20, 0), slot.getCloseTime());
        }

        @Test
        @DisplayName("TimeSlot contains null openTime")
        void businessHours_TimeSlotContainsNullOpenTime() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot();
            slot.setCloseTime(LocalTime.of(21, 0));
            assertFalse(slot.contains(LocalTime.of(12, 0)));
        }

        @Test
        @DisplayName("TimeSlot contains null closeTime")
        void businessHours_TimeSlotContainsNullCloseTime() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot();
            slot.setOpenTime(LocalTime.of(9, 0));
            assertFalse(slot.contains(LocalTime.of(12, 0)));
        }

        @Test
        @DisplayName("TimeSlot equals 不同類別")
        void businessHours_TimeSlotEqualsDifferentClass() {
            BusinessHours.TimeSlot slot = new BusinessHours.TimeSlot(
                    LocalTime.of(9, 0), LocalTime.of(21, 0));
            assertNotEquals(slot, "not a TimeSlot");
        }

        @Test
        @DisplayName("setWeeklyHours 設定整週")
        void businessHours_SetWeeklyHours() {
            BusinessHours bh = new BusinessHours();
            java.util.Map<DayOfWeek, BusinessHours.TimeSlot> hours = new java.util.EnumMap<>(DayOfWeek.class);
            hours.put(DayOfWeek.MONDAY, new BusinessHours.TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0)));
            bh.setWeeklyHours(hours);
            assertNotNull(bh.getWeeklyHours());
            assertNotNull(bh.getHours(DayOfWeek.MONDAY));
        }
    }

    // CuisineType tests
    @Nested
    @DisplayName("CuisineType Tests")
    class CuisineTypeTests {
        @Test
        @DisplayName("fromDisplayName finds type") // 從顯示名稱找到類型
        void cuisineType_FromDisplayNameFinds() {
            assertEquals(CuisineType.JAPANESE, CuisineType.fromDisplayName("日式料理"));
        }

        @Test
        @DisplayName("fromDisplayName null returns null") // null 檢查
        void cuisineType_FromDisplayNameNull() {
            assertNull(CuisineType.fromDisplayName(null));
        }

        @Test
        @DisplayName("fromDisplayName unknown returns OTHER") // 未知類型返回 OTHER
        void cuisineType_FromDisplayNameUnknown() {
            assertEquals(CuisineType.OTHER, CuisineType.fromDisplayName("Unknown Cuisine"));
        }
    }

    // SearchCriteria tests
    @Nested
    @DisplayName("SearchCriteria Tests")
    class SearchCriteriaTests {
        @Test
        @DisplayName("builder pattern") // 建構器模式
        void searchCriteria_BuilderPattern() {
            SearchCriteria criteria = new SearchCriteria()
                    .keyword("sushi")
                    .city("台北")
                    .cuisineType(CuisineType.JAPANESE)
                    .minRating(4.0)
                    .limit(10);

            assertEquals("sushi", criteria.getKeyword());
            assertEquals("台北", criteria.getCity());
            assertEquals(CuisineType.JAPANESE, criteria.getCuisineType());
            assertEquals(4.0, criteria.getMinRating());
            assertEquals(10, criteria.getLimit());
        }

        @Test
        @DisplayName("hasLocationFilter true 當所有位置參數都有值")
        void searchCriteria_HasLocationFilterTrue() {
            SearchCriteria criteria = new SearchCriteria()
                    .nearLocation(25.0, 121.0, 5.0);
            assertTrue(criteria.hasLocationFilter());
        }

        @Test
        @DisplayName("hasLocationFilter false 當緯度為 null")
        void searchCriteria_HasLocationFilterFalseLatNull() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLongitude(121.0);
            criteria.setRadiusKm(5.0);
            assertFalse(criteria.hasLocationFilter());
        }

        @Test
        @DisplayName("hasLocationFilter false 當經度為 null")
        void searchCriteria_HasLocationFilterFalseLonNull() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLatitude(25.0);
            criteria.setRadiusKm(5.0);
            assertFalse(criteria.hasLocationFilter());
        }

        @Test
        @DisplayName("hasLocationFilter false 當半徑為 null")
        void searchCriteria_HasLocationFilterFalseRadiusNull() {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setLatitude(25.0);
            criteria.setLongitude(121.0);
            criteria.setRadiusKm(null);
            assertFalse(criteria.hasLocationFilter());
        }

        @Test
        @DisplayName("hasLocationFilter false 當半徑為 0")
        void searchCriteria_HasLocationFilterFalseRadiusZero() {
            SearchCriteria criteria = new SearchCriteria()
                    .nearLocation(25.0, 121.0, 0);
            assertFalse(criteria.hasLocationFilter());

            SearchCriteria criteria2 = new SearchCriteria()
                    .nearLocation(25.0, 121.0, -1);
            assertFalse(criteria2.hasLocationFilter());
        }

        @Test
        @DisplayName("hasPriceFilter true 當有最小價格")
        void searchCriteria_HasPriceFilterMinPrice() {
            SearchCriteria criteria = new SearchCriteria().minPrice(100.0);
            assertTrue(criteria.hasPriceFilter());
        }

        @Test
        @DisplayName("hasPriceFilter true 當有最大價格")
        void searchCriteria_HasPriceFilterMaxPrice() {
            SearchCriteria criteria = new SearchCriteria().maxPrice(500.0);
            assertTrue(criteria.hasPriceFilter());
        }

        @Test
        @DisplayName("hasPriceFilter true 當有價格等級")
        void searchCriteria_HasPriceFilterPriceLevel() {
            SearchCriteria criteria = new SearchCriteria().priceLevel(3);
            assertTrue(criteria.hasPriceFilter());
        }

        @Test
        @DisplayName("hasPriceFilter false 當沒有任何價格篩選")
        void searchCriteria_HasPriceFilterFalse() {
            SearchCriteria criteria = new SearchCriteria();
            assertFalse(criteria.hasPriceFilter());
        }

        @Test
        @DisplayName("hasRatingFilter true 當有最小評分")
        void searchCriteria_HasRatingFilterMinRating() {
            SearchCriteria criteria = new SearchCriteria().minRating(4.0);
            assertTrue(criteria.hasRatingFilter());
        }

        @Test
        @DisplayName("hasRatingFilter true 當有最大評分")
        void searchCriteria_HasRatingFilterMaxRating() {
            SearchCriteria criteria = new SearchCriteria().maxRating(4.5);
            assertTrue(criteria.hasRatingFilter());
        }

        @Test
        @DisplayName("hasRatingFilter false 當沒有任何評分篩選")
        void searchCriteria_HasRatingFilterFalse() {
            SearchCriteria criteria = new SearchCriteria();
            assertFalse(criteria.hasRatingFilter());
        }

        @Test
        @DisplayName("isEmpty true 當沒有任何篩選條件")
        void searchCriteria_IsEmptyTrue() {
            SearchCriteria criteria = new SearchCriteria();
            assertTrue(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有關鍵字")
        void searchCriteria_IsEmptyFalseKeyword() {
            SearchCriteria criteria = new SearchCriteria().keyword("test");
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有城市")
        void searchCriteria_IsEmptyFalseCity() {
            SearchCriteria criteria = new SearchCriteria().city("台北");
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有區")
        void searchCriteria_IsEmptyFalseDistrict() {
            SearchCriteria criteria = new SearchCriteria().district("大安區");
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有菜系")
        void searchCriteria_IsEmptyFalseCuisineType() {
            SearchCriteria criteria = new SearchCriteria().cuisineType(CuisineType.JAPANESE);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有最小評分")
        void searchCriteria_IsEmptyFalseMinRating() {
            SearchCriteria criteria = new SearchCriteria().minRating(4.0);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有最大評分")
        void searchCriteria_IsEmptyFalseMaxRating() {
            SearchCriteria criteria = new SearchCriteria().maxRating(5.0);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有最小價格")
        void searchCriteria_IsEmptyFalseMinPrice() {
            SearchCriteria criteria = new SearchCriteria().minPrice(100.0);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有最大價格")
        void searchCriteria_IsEmptyFalseMaxPrice() {
            SearchCriteria criteria = new SearchCriteria().maxPrice(500.0);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有價格等級")
        void searchCriteria_IsEmptyFalsePriceLevel() {
            SearchCriteria criteria = new SearchCriteria().priceLevel(2);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有即時營業")
        void searchCriteria_IsEmptyFalseOpenNow() {
            SearchCriteria criteria = new SearchCriteria().openNow(true);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有外送")
        void searchCriteria_IsEmptyFalseHasDelivery() {
            SearchCriteria criteria = new SearchCriteria().hasDelivery(true);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有外帶")
        void searchCriteria_IsEmptyFalseHasTakeout() {
            SearchCriteria criteria = new SearchCriteria().hasTakeout(true);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有停車位")
        void searchCriteria_IsEmptyFalseHasParking() {
            SearchCriteria criteria = new SearchCriteria().hasParking(true);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有預約")
        void searchCriteria_IsEmptyFalseAcceptsReservations() {
            SearchCriteria criteria = new SearchCriteria().acceptsReservations(true);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("isEmpty false 當有位置篩選")
        void searchCriteria_IsEmptyFalseLocationFilter() {
            SearchCriteria criteria = new SearchCriteria().nearLocation(25.0, 121.0, 5.0);
            assertFalse(criteria.isEmpty());
        }

        @Test
        @DisplayName("addCuisineType null 不會新增")
        void searchCriteria_AddCuisineTypeNull() {
            SearchCriteria criteria = new SearchCriteria().addCuisineType(null);
            assertTrue(criteria.getCuisineTypes().isEmpty());
        }

        @Test
        @DisplayName("addCuisineType 可以新增多個")
        void searchCriteria_AddCuisineTypeMultiple() {
            SearchCriteria criteria = new SearchCriteria()
                    .addCuisineType(CuisineType.JAPANESE)
                    .addCuisineType(CuisineType.CHINESE)
                    .addCuisineType(CuisineType.ITALIAN);
            assertEquals(3, criteria.getCuisineTypes().size());
            assertTrue(criteria.getCuisineTypes().contains(CuisineType.JAPANESE));
            assertTrue(criteria.getCuisineTypes().contains(CuisineType.CHINESE));
            assertTrue(criteria.getCuisineTypes().contains(CuisineType.ITALIAN));
        }

        @Test
        @DisplayName("sortBy 和 ascending 設定")
        void searchCriteria_SortByAndAscending() {
            SearchCriteria criteria = new SearchCriteria()
                    .sortBy(SearchCriteria.SortType.RATING)
                    .ascending(false);

            assertEquals(SearchCriteria.SortType.RATING, criteria.getSortBy());
            assertFalse(criteria.isAscending());
        }

        @Test
        @DisplayName("offset 設定")
        void searchCriteria_Offset() {
            SearchCriteria criteria = new SearchCriteria().offset(20);
            assertEquals(20, criteria.getOffset());
        }

        @Test
        @DisplayName("預設值檢查")
        void searchCriteria_DefaultValues() {
            SearchCriteria criteria = new SearchCriteria();
            assertEquals(20, criteria.getLimit()); // 預設 limit 是 20
            assertEquals(0, criteria.getOffset()); // 預設 offset 是 0
            assertTrue(criteria.isAscending()); // 預設 ascending 是 true
            assertNotNull(criteria.getCuisineTypes());
            assertTrue(criteria.getCuisineTypes().isEmpty());
        }

        @Test
        @DisplayName("setter 方法測試")
        void searchCriteria_Setters() {
            SearchCriteria criteria = new SearchCriteria();

            criteria.setKeyword("pizza");
            assertEquals("pizza", criteria.getKeyword());

            criteria.setCity("高雄");
            assertEquals("高雄", criteria.getCity());

            criteria.setDistrict("前鎮區");
            assertEquals("前鎮區", criteria.getDistrict());

            criteria.setCuisineType(CuisineType.ITALIAN);
            assertEquals(CuisineType.ITALIAN, criteria.getCuisineType());

            criteria.setMinRating(3.5);
            assertEquals(3.5, criteria.getMinRating());

            criteria.setMaxRating(4.5);
            assertEquals(4.5, criteria.getMaxRating());

            criteria.setMinPrice(200.0);
            assertEquals(200.0, criteria.getMinPrice());

            criteria.setMaxPrice(800.0);
            assertEquals(800.0, criteria.getMaxPrice());

            criteria.setPriceLevel(3);
            assertEquals(3, criteria.getPriceLevel());

            criteria.setOpenNow(true);
            assertTrue(criteria.getOpenNow());

            criteria.setHasDelivery(true);
            assertTrue(criteria.getHasDelivery());

            criteria.setHasTakeout(true);
            assertTrue(criteria.getHasTakeout());

            criteria.setHasParking(true);
            assertTrue(criteria.getHasParking());

            criteria.setAcceptsReservations(true);
            assertTrue(criteria.getAcceptsReservations());

            criteria.setLatitude(22.0);
            assertEquals(22.0, criteria.getLatitude());

            criteria.setLongitude(120.0);
            assertEquals(120.0, criteria.getLongitude());

            criteria.setRadiusKm(10.0);
            assertEquals(10.0, criteria.getRadiusKm());

            criteria.setSortBy(SearchCriteria.SortType.DISTANCE);
            assertEquals(SearchCriteria.SortType.DISTANCE, criteria.getSortBy());

            criteria.setAscending(false);
            assertFalse(criteria.isAscending());

            criteria.setLimit(50);
            assertEquals(50, criteria.getLimit());

            criteria.setOffset(10);
            assertEquals(10, criteria.getOffset());
        }
    }

    // UserPreferences tests
    @Nested
    @DisplayName("UserPreferences Tests")
    class UserPreferencesTests {
        @Test
        @DisplayName("calculatePreferenceScore 喜歡菜系加分")
        void userPreferences_CalculatePreferenceScoreFavorite() {
            UserPreferences prefs = new UserPreferences();
            prefs.addFavoriteCuisine(CuisineType.JAPANESE);
            prefs.setMaxPriceLevel(3);
            prefs.setMinAcceptableRating(3.0);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2);
            r.addReview(new Review("1", "1", 4, "Good"));

            double score = prefs.calculatePreferenceScore(r);
            assertTrue(score > 50); // 應該高於基礎分數
        }

        @Test
        @DisplayName("calculatePreferenceScore 不喜歡菜系扣分")
        void userPreferences_CalculatePreferenceScoreDisliked() {
            UserPreferences prefs = new UserPreferences();
            prefs.addDislikedCuisine(CuisineType.JAPANESE);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2);
            r.addReview(new Review("1", "1", 4, "Good"));

            double score = prefs.calculatePreferenceScore(r);
            assertTrue(score < 50); // 應該低於基礎分數
        }

        @Test
        @DisplayName("calculatePreferenceScore null 餐廳返回 0")
        void userPreferences_CalculatePreferenceScoreNullRestaurant() {
            UserPreferences prefs = new UserPreferences();
            assertEquals(0.0, prefs.calculatePreferenceScore(null));
        }

        @Test
        @DisplayName("calculatePreferenceScore 附加菜系喜好加分")
        void userPreferences_CalculatePreferenceScoreAdditionalFavorite() {
            UserPreferences prefs = new UserPreferences();
            prefs.addFavoriteCuisine(CuisineType.SEAFOOD);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.addCuisineType(CuisineType.SEAFOOD);
            r.setPriceLevel(2);
            r.addReview(new Review("1", "1", 4, "Good"));

            UserPreferences basePrefs = new UserPreferences();
            Restaurant baseR = new Restaurant("2", "Test2", CuisineType.JAPANESE, null);
            baseR.setPriceLevel(2);
            baseR.addReview(new Review("2", "2", 4, "Good"));

            assertTrue(prefs.calculatePreferenceScore(r) > basePrefs.calculatePreferenceScore(baseR));
        }

        @Test
        @DisplayName("calculatePreferenceScore 附加菜系不喜歡扣分")
        void userPreferences_CalculatePreferenceScoreAdditionalDisliked() {
            UserPreferences prefs = new UserPreferences();
            prefs.addDislikedCuisine(CuisineType.SEAFOOD);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.addCuisineType(CuisineType.SEAFOOD);
            r.setPriceLevel(2);
            r.addReview(new Review("1", "1", 4, "Good"));

            UserPreferences basePrefs = new UserPreferences();
            double baseScore = basePrefs.calculatePreferenceScore(r);
            double score = prefs.calculatePreferenceScore(r);

            assertTrue(score < baseScore);
        }

        @Test
        @DisplayName("calculatePreferenceScore 價格超出範圍扣分")
        void userPreferences_CalculatePreferenceScorePriceTooHigh() {
            UserPreferences prefs = new UserPreferences();
            prefs.setMaxPriceLevel(2);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(3); // 超出 maxPriceLevel
            r.addReview(new Review("1", "1", 4, "Good"));

            UserPreferences basePrefs = new UserPreferences();
            basePrefs.setMaxPriceLevel(4);
            double baseScore = basePrefs.calculatePreferenceScore(r);
            double score = prefs.calculatePreferenceScore(r);

            assertTrue(score < baseScore);
        }

        @Test
        @DisplayName("calculatePreferenceScore 價格在範圍內加分")
        void userPreferences_CalculatePreferenceScorePriceInRange() {
            UserPreferences prefs = new UserPreferences();
            prefs.setMaxPriceLevel(3);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2); // 在 maxPriceLevel 範圍內
            r.addReview(new Review("1", "1", 4, "Good"));

            double score = prefs.calculatePreferenceScore(r);
            assertTrue(score > 50);
        }

        @Test
        @DisplayName("calculatePreferenceScore 評分低於最低標準扣分")
        void userPreferences_CalculatePreferenceScoreRatingTooLow() {
            UserPreferences prefs = new UserPreferences();
            prefs.setMinAcceptableRating(4.5);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2);
            r.addReview(new Review("1", "1", 3, "OK")); // 評分 3，低於 4.5

            double score = prefs.calculatePreferenceScore(r);
            assertTrue(score < 50);
        }

        @Test
        @DisplayName("calculatePreferenceScore 需要停車位但沒有停車位扣分")
        void userPreferences_CalculatePreferenceScoreRequiresParkingNoParking() {
            UserPreferences prefs = new UserPreferences();
            prefs.setRequiresParking(true);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2);
            r.setHasParking(false);
            r.addReview(new Review("1", "1", 4, "Good"));

            UserPreferences basePrefs = new UserPreferences();
            double baseScore = basePrefs.calculatePreferenceScore(r);
            double score = prefs.calculatePreferenceScore(r);

            assertTrue(score < baseScore);
        }

        @Test
        @DisplayName("calculatePreferenceScore 需要停車位且有停車位加分")
        void userPreferences_CalculatePreferenceScoreRequiresParkingHasParking() {
            UserPreferences prefs = new UserPreferences();
            prefs.setRequiresParking(true);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2);
            r.setHasParking(true);
            r.addReview(new Review("1", "1", 4, "Good"));

            UserPreferences basePrefs = new UserPreferences();
            double baseScore = basePrefs.calculatePreferenceScore(r);
            double score = prefs.calculatePreferenceScore(r);

            assertTrue(score > baseScore);
        }

        @Test
        @DisplayName("calculatePreferenceScore 偏好外送且有外送加分")
        void userPreferences_CalculatePreferenceScorePreferDelivery() {
            UserPreferences prefs = new UserPreferences();
            prefs.setPreferDelivery(true);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2);
            r.setHasDelivery(true);
            r.addReview(new Review("1", "1", 4, "Good"));

            UserPreferences basePrefs = new UserPreferences();
            double baseScore = basePrefs.calculatePreferenceScore(r);
            double score = prefs.calculatePreferenceScore(r);

            assertTrue(score > baseScore);
        }

        @Test
        @DisplayName("calculatePreferenceScore 偏好外帶且有外帶加分")
        void userPreferences_CalculatePreferenceScorePreferTakeout() {
            UserPreferences prefs = new UserPreferences();
            prefs.setPreferTakeout(true);

            Restaurant r = new Restaurant("1", "Test", CuisineType.JAPANESE, null);
            r.setPriceLevel(2);
            r.setHasTakeout(true);
            r.addReview(new Review("1", "1", 4, "Good"));

            UserPreferences basePrefs = new UserPreferences();
            double baseScore = basePrefs.calculatePreferenceScore(r);
            double score = prefs.calculatePreferenceScore(r);

            assertTrue(score > baseScore);
        }

        @Test
        @DisplayName("addFavoriteCuisine removes from disliked")
        void userPreferences_AddFavoriteRemovesDisliked() {
            UserPreferences prefs = new UserPreferences();
            prefs.addDislikedCuisine(CuisineType.JAPANESE);
            prefs.addFavoriteCuisine(CuisineType.JAPANESE);

            assertTrue(prefs.likesCuisine(CuisineType.JAPANESE));
            assertFalse(prefs.dislikesCuisine(CuisineType.JAPANESE));
        }

        @Test
        @DisplayName("addDislikedCuisine removes from favorite")
        void userPreferences_AddDislikedRemovesFavorite() {
            UserPreferences prefs = new UserPreferences();
            prefs.addFavoriteCuisine(CuisineType.JAPANESE);
            prefs.addDislikedCuisine(CuisineType.JAPANESE);

            assertFalse(prefs.likesCuisine(CuisineType.JAPANESE));
            assertTrue(prefs.dislikesCuisine(CuisineType.JAPANESE));
        }

        @Test
        @DisplayName("addFavoriteCuisine null 不會新增")
        void userPreferences_AddFavoriteCuisineNull() {
            UserPreferences prefs = new UserPreferences();
            prefs.addFavoriteCuisine(null);
            assertTrue(prefs.getFavoriteCuisines().isEmpty());
        }

        @Test
        @DisplayName("addDislikedCuisine null 不會新增")
        void userPreferences_AddDislikedCuisineNull() {
            UserPreferences prefs = new UserPreferences();
            prefs.addDislikedCuisine(null);
            assertTrue(prefs.getDislikedCuisines().isEmpty());
        }

        @Test
        @DisplayName("likesCuisine null 返回 false")
        void userPreferences_LikesCuisineNull() {
            UserPreferences prefs = new UserPreferences();
            assertFalse(prefs.likesCuisine(null));
        }

        @Test
        @DisplayName("dislikesCuisine null 返回 false")
        void userPreferences_DislikesCuisineNull() {
            UserPreferences prefs = new UserPreferences();
            assertFalse(prefs.dislikesCuisine(null));
        }

        @Test
        @DisplayName("setMaxPriceLevel 邊界值處理")
        void userPreferences_SetMaxPriceLevelBoundary() {
            UserPreferences prefs = new UserPreferences();

            prefs.setMaxPriceLevel(0); // 低於最小值
            assertEquals(1, prefs.getMaxPriceLevel());

            prefs.setMaxPriceLevel(5); // 高於最大值
            assertEquals(4, prefs.getMaxPriceLevel());

            prefs.setMaxPriceLevel(2); // 正常值
            assertEquals(2, prefs.getMaxPriceLevel());
        }

        @Test
        @DisplayName("setMinAcceptableRating 邊界值處理")
        void userPreferences_SetMinAcceptableRatingBoundary() {
            UserPreferences prefs = new UserPreferences();

            prefs.setMinAcceptableRating(-1); // 低於最小值
            assertEquals(0.0, prefs.getMinAcceptableRating());

            prefs.setMinAcceptableRating(6); // 高於最大值
            assertEquals(5.0, prefs.getMinAcceptableRating());

            prefs.setMinAcceptableRating(3.5); // 正常值
            assertEquals(3.5, prefs.getMinAcceptableRating());
        }

        @Test
        @DisplayName("預設值檢查")
        void userPreferences_DefaultValues() {
            UserPreferences prefs = new UserPreferences();
            assertEquals(4, prefs.getMaxPriceLevel());
            assertEquals(0.0, prefs.getMinAcceptableRating());
            assertEquals(10.0, prefs.getMaxDistanceKm());
            assertNotNull(prefs.getFavoriteCuisines());
            assertTrue(prefs.getFavoriteCuisines().isEmpty());
            assertNotNull(prefs.getDislikedCuisines());
            assertTrue(prefs.getDislikedCuisines().isEmpty());
            assertFalse(prefs.isPreferVegetarian());
            assertFalse(prefs.isPreferVegan());
            assertFalse(prefs.isPreferGlutenFree());
            assertFalse(prefs.isRequiresParking());
            assertFalse(prefs.isPreferDelivery());
            assertFalse(prefs.isPreferTakeout());
        }

        @Test
        @DisplayName("setter 方法測試")
        void userPreferences_Setters() {
            UserPreferences prefs = new UserPreferences();

            prefs.setPreferVegetarian(true);
            assertTrue(prefs.isPreferVegetarian());

            prefs.setPreferVegan(true);
            assertTrue(prefs.isPreferVegan());

            prefs.setPreferGlutenFree(true);
            assertTrue(prefs.isPreferGlutenFree());

            prefs.setRequiresParking(true);
            assertTrue(prefs.isRequiresParking());

            prefs.setPreferDelivery(true);
            assertTrue(prefs.isPreferDelivery());

            prefs.setPreferTakeout(true);
            assertTrue(prefs.isPreferTakeout());

            prefs.setMaxDistanceKm(5.0);
            assertEquals(5.0, prefs.getMaxDistanceKm());

            Location loc = new Location(25.0, 121.0);
            prefs.setUserLocation(loc);
            assertEquals(loc, prefs.getUserLocation());

            Set<CuisineType> favorites = new HashSet<>();
            favorites.add(CuisineType.JAPANESE);
            prefs.setFavoriteCuisines(favorites);
            assertEquals(favorites, prefs.getFavoriteCuisines());

            Set<CuisineType> disliked = new HashSet<>();
            disliked.add(CuisineType.CHINESE);
            prefs.setDislikedCuisines(disliked);
            assertEquals(disliked, prefs.getDislikedCuisines());
        }
    }

    // Repository tests
    @Nested
    @DisplayName("Repository Tests")
    class RepositoryTests {
        @Test
        @DisplayName("save and find") // 儲存和查找
        void repository_SaveAndFind() {
            RestaurantRepository repo = new RestaurantRepository();
            Restaurant r = new Restaurant("1", "Test");
            repo.save(r);

            assertTrue(repo.findById("1").isPresent());
            assertEquals("Test", repo.findById("1").get().getName());
        }

        @Test
        @DisplayName("getById throws when not found") // 當找不到時拋出異常
        void repository_GetByIdThrows() {
            RestaurantRepository repo = new RestaurantRepository();
            assertThrows(RestaurantNotFoundException.class,
                    () -> repo.getById("nonexistent"));
        }

        @Test
        @DisplayName("count") // 總數
        void repository_Count() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test1"));
            repo.save(new Restaurant("2", "Test2"));
            assertEquals(2, repo.count());
        }

        @Test
        @DisplayName("save null 餐廳拋出異常")
        void repository_SaveNullThrows() {
            RestaurantRepository repo = new RestaurantRepository();
            assertThrows(IllegalArgumentException.class, () -> repo.save(null));
        }

        @Test
        @DisplayName("save null ID 拋出異常")
        void repository_SaveNullIdThrows() {
            RestaurantRepository repo = new RestaurantRepository();
            Restaurant r = new Restaurant();
            r.setId(null);
            assertThrows(IllegalArgumentException.class, () -> repo.save(r));
        }

        @Test
        @DisplayName("save 空 ID 拋出異常")
        void repository_SaveEmptyIdThrows() {
            RestaurantRepository repo = new RestaurantRepository();
            Restaurant r = new Restaurant();
            r.setId("");
            assertThrows(IllegalArgumentException.class, () -> repo.save(r));

            Restaurant r2 = new Restaurant();
            r2.setId("   ");
            assertThrows(IllegalArgumentException.class, () -> repo.save(r2));
        }

        @Test
        @DisplayName("findById null 返回空")
        void repository_FindByIdNull() {
            RestaurantRepository repo = new RestaurantRepository();
            assertFalse(repo.findById(null).isPresent());
        }

        @Test
        @DisplayName("findById 找不到返回空")
        void repository_FindByIdNotFound() {
            RestaurantRepository repo = new RestaurantRepository();
            assertFalse(repo.findById("nonexistent").isPresent());
        }

        @Test
        @DisplayName("findAll 返回所有餐廳")
        void repository_FindAll() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test1"));
            repo.save(new Restaurant("2", "Test2"));
            repo.save(new Restaurant("3", "Test3"));

            List<Restaurant> all = repo.findAll();
            assertEquals(3, all.size());
        }

        @Test
        @DisplayName("findAll 空倉庫返回空列表")
        void repository_FindAllEmpty() {
            RestaurantRepository repo = new RestaurantRepository();
            List<Restaurant> all = repo.findAll();
            assertTrue(all.isEmpty());
        }

        @Test
        @DisplayName("findByName null 返回空")
        void repository_FindByNameNull() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test"));
            List<Restaurant> result = repo.findByName(null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findByName 空字串返回空")
        void repository_FindByNameEmpty() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test"));
            List<Restaurant> result = repo.findByName("");
            assertTrue(result.isEmpty());

            List<Restaurant> result2 = repo.findByName("   ");
            assertTrue(result2.isEmpty());
        }

        @Test
        @DisplayName("findByName 不分大小寫匹配")
        void repository_FindByNameMatches() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Tokyo Sushi"));
            repo.save(new Restaurant("2", "Osaka Ramen"));
            repo.save(new Restaurant("3", "Sushi Master"));

            List<Restaurant> result = repo.findByName("sushi");
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("findByName 無匹配返回空")
        void repository_FindByNameNoMatch() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Tokyo Sushi"));

            List<Restaurant> result = repo.findByName("pizza");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findByName 餐廳名稱為 null 時不匹配")
        void repository_FindByNameNullRestaurantName() {
            RestaurantRepository repo = new RestaurantRepository();
            Restaurant r = new Restaurant();
            r.setId("1");
            r.setName(null);
            repo.save(r);

            List<Restaurant> result = repo.findByName("test");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("delete 移除餐廳")
        void repository_Delete() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test"));
            assertTrue(repo.exists("1"));

            repo.delete("1");
            assertFalse(repo.exists("1"));
        }

        @Test
        @DisplayName("delete null 不執行任何操作")
        void repository_DeleteNull() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test"));
            repo.delete(null);
            assertEquals(1, repo.count());
        }

        @Test
        @DisplayName("delete 不存在的 ID 不執行任何操作")
        void repository_DeleteNonexistent() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test"));
            repo.delete("nonexistent");
            assertEquals(1, repo.count());
        }

        @Test
        @DisplayName("deleteAll 清除所有餐廳")
        void repository_DeleteAll() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test1"));
            repo.save(new Restaurant("2", "Test2"));
            assertEquals(2, repo.count());

            repo.deleteAll();
            assertEquals(0, repo.count());
        }

        @Test
        @DisplayName("exists null 返回 false")
        void repository_ExistsNull() {
            RestaurantRepository repo = new RestaurantRepository();
            assertFalse(repo.exists(null));
        }

        @Test
        @DisplayName("exists 存在時返回 true")
        void repository_ExistsTrue() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test"));
            assertTrue(repo.exists("1"));
        }

        @Test
        @DisplayName("exists 不存在時返回 false")
        void repository_ExistsFalse() {
            RestaurantRepository repo = new RestaurantRepository();
            assertFalse(repo.exists("nonexistent"));
        }

        @Test
        @DisplayName("getById 找到時返回餐廳")
        void repository_GetByIdFound() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Test"));
            Restaurant r = repo.getById("1");
            assertEquals("Test", r.getName());
        }

        @Test
        @DisplayName("save 更新已存在的餐廳")
        void repository_SaveUpdates() {
            RestaurantRepository repo = new RestaurantRepository();
            repo.save(new Restaurant("1", "Original"));
            repo.save(new Restaurant("1", "Updated"));

            assertEquals(1, repo.count());
            assertEquals("Updated", repo.findById("1").get().getName());
        }
    }

    // Exception tests
    @Nested
    @DisplayName("Exception Tests")
    class ExceptionTests {
        @Test
        @DisplayName("getField returns field") // getField 返回 field
        void validationException_GetField() {
            ValidationException ex = new ValidationException("Error", "fieldName");
            assertEquals("fieldName", ex.getField());
        }

        @Test
        @DisplayName("getRestaurantId returns id") // getRestaurantId 返回 id
        void restaurantNotFoundException_GetRestaurantId() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Not found", "123");
            assertEquals("123", ex.getRestaurantId());
        }
    }
}
