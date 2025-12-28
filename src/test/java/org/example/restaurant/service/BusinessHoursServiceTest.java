package org.example.restaurant.service;

import org.example.restaurant.model.BusinessHours;
import org.example.restaurant.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusinessHoursServiceTest {

    private BusinessHoursService service;

    @BeforeEach
    void setUp() {
        service = new BusinessHoursService();
    }

    private Restaurant createRestaurantWithHours(LocalTime open, LocalTime close) {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        for (DayOfWeek day : DayOfWeek.values()) {
            hours.setHours(day, open, close);
        }
        restaurant.setBusinessHours(hours);
        return restaurant;
    }

    // isOpenAt tests
    @Test
    @DisplayName("Is open at - null restaurant returns false")
    void isOpenAt_NullRestaurant_ReturnsFalse() {
        assertFalse(service.isOpenAt(null, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Is open at - null datetime returns false")
    void isOpenAt_NullDateTime_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertFalse(service.isOpenAt(restaurant, null));
    }

    @Test
    @DisplayName("Is open at - inactive restaurant returns false")
    void isOpenAt_InactiveRestaurant_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.setActive(false);
        assertFalse(service.isOpenAt(restaurant, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Is open at - no business hours returns false")
    void isOpenAt_NoBusinessHours_ReturnsFalse() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        assertFalse(service.isOpenAt(restaurant, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Is open at - within hours returns true")
    void isOpenAt_WithinHours_ReturnsTrue() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 12, 0); // Monday noon
        assertTrue(service.isOpenAt(restaurant, time));
    }

    @Test
    @DisplayName("Is open at - outside hours returns false")
    void isOpenAt_OutsideHours_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 22, 0); // Monday 10pm
        assertFalse(service.isOpenAt(restaurant, time));
    }

    // isHoliday tests
    @Test
    @DisplayName("Is holiday - null date returns false")
    void isHoliday_NullDate_ReturnsFalse() {
        assertFalse(service.isHoliday(null));
    }

    @Test
    @DisplayName("Is holiday - known holiday returns true")
    void isHoliday_KnownHoliday_ReturnsTrue() {
        assertTrue(service.isHoliday(LocalDate.of(2024, 1, 1)));
    }

    @Test
    @DisplayName("Is holiday - regular day returns false")
    void isHoliday_RegularDay_ReturnsFalse() {
        assertFalse(service.isHoliday(LocalDate.of(2024, 3, 15)));
    }

    // findOpenRestaurants tests
    @Test
    @DisplayName("Find open restaurants - null list returns empty")
    void findOpenRestaurants_NullList_ReturnsEmpty() {
        assertTrue(service.findOpenRestaurants(null, LocalDateTime.now()).isEmpty());
    }

    @Test
    @DisplayName("Find open restaurants - finds open ones")
    void findOpenRestaurants_FindsOpenOnes() {
        Restaurant r1 = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        r1.setId("1");
        Restaurant r2 = createRestaurantWithHours(LocalTime.of(18, 0), LocalTime.of(23, 0));
        r2.setId("2");
        List<Restaurant> list = Arrays.asList(r1, r2);

        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 12, 0);
        List<Restaurant> result = service.findOpenRestaurants(list, time);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    // getNextOpenTime tests
    @Test
    @DisplayName("Get next open time - null restaurant returns null")
    void getNextOpenTime_NullRestaurant_ReturnsNull() {
        assertNull(service.getNextOpenTime(null));
    }

    @Test
    @DisplayName("Get next open time - inactive restaurant returns null")
    void getNextOpenTime_InactiveRestaurant_ReturnsNull() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.setActive(false);
        assertNull(service.getNextOpenTime(restaurant, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Get next open time - finds next opening")
    void getNextOpenTime_FindsNextOpening() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        LocalDateTime from = LocalDateTime.of(2024, 1, 15, 22, 0);
        LocalDateTime next = service.getNextOpenTime(restaurant, from);
        assertNotNull(next);
        assertEquals(9, next.getHour());
    }

    // getClosingTimeToday tests
    @Test
    @DisplayName("Get closing time today - null restaurant returns null")
    void getClosingTimeToday_NullRestaurant_ReturnsNull() {
        assertNull(service.getClosingTimeToday(null));
    }

    @Test
    @DisplayName("Get closing time today - returns closing time")
    void getClosingTimeToday_ReturnsClosingTime() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        LocalTime closing = service.getClosingTimeToday(restaurant);
        assertNotNull(closing);
        assertEquals(21, closing.getHour());
    }

    // isClosingSoon tests
    @Test
    @DisplayName("Is closing soon - null restaurant returns false")
    void isClosingSoon_NullRestaurant_ReturnsFalse() {
        assertFalse(service.isClosingSoon(null, 30));
    }

    @Test
    @DisplayName("Is closing soon - zero minutes returns false")
    void isClosingSoon_ZeroMinutes_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertFalse(service.isClosingSoon(restaurant, 0));
    }

    // getOperatingDaysCount tests
    @Test
    @DisplayName("Get operating days count - null restaurant returns 0")
    void getOperatingDaysCount_NullRestaurant_ReturnsZero() {
        assertEquals(0, service.getOperatingDaysCount(null));
    }

    @Test
    @DisplayName("Get operating days count - counts correctly")
    void getOperatingDaysCount_CountsCorrectly() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertEquals(7, service.getOperatingDaysCount(restaurant));
    }

    // getBusinessHoursSummary tests
    @Test
    @DisplayName("Get business hours summary - null restaurant")
    void getBusinessHoursSummary_NullRestaurant() {
        String summary = service.getBusinessHoursSummary(null);
        assertEquals("無資料", summary);
    }

    @Test
    @DisplayName("Get business hours summary - no hours")
    void getBusinessHoursSummary_NoHours() {
        Restaurant restaurant = new Restaurant("1", "Test");
        String summary = service.getBusinessHoursSummary(restaurant);
        assertTrue(summary.contains("無營業時間"));
    }

    @Test
    @DisplayName("Get business hours summary - all week")
    void getBusinessHoursSummary_AllWeek() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        String summary = service.getBusinessHoursSummary(restaurant);
        assertTrue(summary.contains("每日營業"));
    }

    // is24Hours tests
    @Test
    @DisplayName("Is 24 hours - null restaurant returns false")
    void is24Hours_NullRestaurant_ReturnsFalse() {
        assertFalse(service.is24Hours(null, DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("Is 24 hours - checks correctly")
    void is24Hours_ChecksCorrectly() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.MIDNIGHT, LocalTime.of(23, 59));
        assertTrue(service.is24Hours(restaurant, DayOfWeek.MONDAY));
    }

    // calculateWeeklyOperatingHours tests
    @Test
    @DisplayName("Calculate weekly operating hours - null restaurant returns 0")
    void calculateWeeklyOperatingHours_NullRestaurant_ReturnsZero() {
        assertEquals(0.0, service.calculateWeeklyOperatingHours(null));
    }

    @Test
    @DisplayName("Calculate weekly operating hours - calculates correctly")
    void calculateWeeklyOperatingHours_CalculatesCorrectly() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        double hours = service.calculateWeeklyOperatingHours(restaurant);
        assertEquals(84.0, hours); // 12 hours * 7 days
    }

    // Holiday management tests
    @Test
    @DisplayName("Add and remove holiday")
    void addAndRemoveHoliday() {
        LocalDate testDate = LocalDate.of(2025, 12, 25);
        assertFalse(service.isHoliday(testDate));

        service.addHoliday(testDate);
        assertTrue(service.isHoliday(testDate));

        service.removeHoliday(testDate);
        assertFalse(service.isHoliday(testDate));
    }

    @Test
    @DisplayName("addHoliday null 不會拋出異常")
    void addHoliday_Null_NoException() {
        assertDoesNotThrow(() -> service.addHoliday(null));
    }

    @Test
    @DisplayName("removeHoliday null 不會拋出異常")
    void removeHoliday_Null_NoException() {
        assertDoesNotThrow(() -> service.removeHoliday(null));
    }

    @Test
    @DisplayName("isOpenAt 假日關閉時返回 false")
    void isOpenAt_HolidayClosed_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.getBusinessHours().setClosedOnHolidays(true);
        LocalDateTime holidayTime = LocalDateTime.of(2024, 1, 1, 12, 0); // New Year
        assertFalse(service.isOpenAt(restaurant, holidayTime));
    }

    @Test
    @DisplayName("isOpenAt 假日但不關閉時返回 true")
    void isOpenAt_HolidayNotClosed_ReturnsTrue() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.getBusinessHours().setClosedOnHolidays(false);
        LocalDateTime holidayTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        assertTrue(service.isOpenAt(restaurant, holidayTime));
    }

    @Test
    @DisplayName("isOpenAt 該日無營業時間返回 false")
    void isOpenAt_NoDayHours_ReturnsFalse() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        hours.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        // TUESDAY 沒有設定
        restaurant.setBusinessHours(hours);
        LocalDateTime tuesday = LocalDateTime.of(2024, 1, 16, 12, 0); // Tuesday
        assertFalse(service.isOpenAt(restaurant, tuesday));
    }

    @Test
    @DisplayName("findOpenRestaurants 空列表返回空")
    void findOpenRestaurants_EmptyList_ReturnsEmpty() {
        assertTrue(service.findOpenRestaurants(List.of(), LocalDateTime.now()).isEmpty());
    }

    @Test
    @DisplayName("findOpenRestaurants null 時間使用當前時間")
    void findOpenRestaurants_NullDateTime_UsesNow() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(0, 0), LocalTime.of(23, 59));
        List<Restaurant> result = service.findOpenRestaurants(List.of(restaurant), null);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findOpenRestaurants 過濾 null 餐廳")
    void findOpenRestaurants_FiltersNullRestaurants() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        List<Restaurant> list = Arrays.asList(restaurant, null);
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 12, 0);
        List<Restaurant> result = service.findOpenRestaurants(list, time);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getNextOpenTime 已營業中返回當前時間")
    void getNextOpenTime_AlreadyOpen_ReturnsCurrent() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        LocalDateTime from = LocalDateTime.of(2024, 1, 15, 12, 0);
        LocalDateTime next = service.getNextOpenTime(restaurant, from);
        assertEquals(from, next);
    }

    @Test
    @DisplayName("getNextOpenTime null from 返回 null")
    void getNextOpenTime_NullFrom_ReturnsNull() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertNull(service.getNextOpenTime(restaurant, null));
    }

    @Test
    @DisplayName("getNextOpenTime 無營業時間返回 null")
    void getNextOpenTime_NoBusinessHours_ReturnsNull() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        assertNull(service.getNextOpenTime(restaurant, LocalDateTime.now()));
    }

    @Test
    @DisplayName("getNextOpenTime 跳過假日")
    void getNextOpenTime_SkipsHolidays() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.getBusinessHours().setClosedOnHolidays(true);
        LocalDateTime fromHoliday = LocalDateTime.of(2024, 1, 1, 7, 0);
        LocalDateTime next = service.getNextOpenTime(restaurant, fromHoliday);
        assertNotNull(next);
        assertNotEquals(LocalDate.of(2024, 1, 1), next.toLocalDate());
    }

    @Test
    @DisplayName("getNextOpenTime 同日開門前返回開門時間")
    void getNextOpenTime_SameDayBeforeOpen_ReturnsOpenTime() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        LocalDateTime from = LocalDateTime.of(2024, 1, 15, 7, 0);
        LocalDateTime next = service.getNextOpenTime(restaurant, from);
        assertNotNull(next);
        assertEquals(LocalTime.of(9, 0), next.toLocalTime());
        assertEquals(LocalDate.of(2024, 1, 15), next.toLocalDate());
    }

    @Test
    @DisplayName("getClosingTimeToday 無營業時間返回 null")
    void getClosingTimeToday_NoBusinessHours_ReturnsNull() {
        Restaurant restaurant = new Restaurant("1", "Test");
        assertNull(service.getClosingTimeToday(restaurant));
    }

    @Test
    @DisplayName("getClosingTimeToday 今日無營業返回 null")
    void getClosingTimeToday_ClosedToday_ReturnsNull() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        // 不設定今日營業時間
        restaurant.setBusinessHours(hours);
        assertNull(service.getClosingTimeToday(restaurant));
    }

    @Test
    @DisplayName("isClosingSoon 負數分鐘返回 false")
    void isClosingSoon_NegativeMinutes_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertFalse(service.isClosingSoon(restaurant, -1));
    }

    @Test
    @DisplayName("isClosingSoon 未營業返回 false")
    void isClosingSoon_NotOpen_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(10, 0));
        // 營業時間很短，大多數時候不營業
        assertFalse(service.isClosingSoon(restaurant, 30));
    }

    @Test
    @DisplayName("findClosingSoon null 列表返回空")
    void findClosingSoon_NullList_ReturnsEmpty() {
        assertTrue(service.findClosingSoon(null, 30).isEmpty());
    }

    @Test
    @DisplayName("findClosingSoon 空列表返回空")
    void findClosingSoon_EmptyList_ReturnsEmpty() {
        assertTrue(service.findClosingSoon(List.of(), 30).isEmpty());
    }

    @Test
    @DisplayName("getOperatingDaysCount 無營業時間返回 0")
    void getOperatingDaysCount_NoBusinessHours_ReturnsZero() {
        Restaurant restaurant = new Restaurant("1", "Test");
        assertEquals(0, service.getOperatingDaysCount(restaurant));
    }

    @Test
    @DisplayName("getOperatingDaysCount 部分營業日正確計數")
    void getOperatingDaysCount_PartialDays_CountsCorrectly() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        hours.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        hours.setHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        hours.setHours(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.setBusinessHours(hours);
        assertEquals(3, service.getOperatingDaysCount(restaurant));
    }

    @Test
    @DisplayName("getBusinessHoursSummary 0 天營業返回休業中")
    void getBusinessHoursSummary_ZeroDays_ReturnsClosed() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        restaurant.setBusinessHours(new BusinessHours());
        String summary = service.getBusinessHoursSummary(restaurant);
        assertEquals("目前休業中", summary);
    }

    @Test
    @DisplayName("getBusinessHoursSummary 5-6 天營業返回週一至週五")
    void getBusinessHoursSummary_FiveToSixDays_ReturnsWeekdays() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        hours.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        hours.setHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        hours.setHours(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        hours.setHours(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        hours.setHours(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.setBusinessHours(hours);
        String summary = service.getBusinessHoursSummary(restaurant);
        assertEquals("週一至週五營業", summary);
    }

    @Test
    @DisplayName("getBusinessHoursSummary 1-4 天營業返回部分時段")
    void getBusinessHoursSummary_PartialDays_ReturnsPartial() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        hours.setHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        hours.setHours(DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(21, 0));
        restaurant.setBusinessHours(hours);
        String summary = service.getBusinessHoursSummary(restaurant);
        assertEquals("部分時段營業", summary);
    }

    @Test
    @DisplayName("is24Hours null day 返回 false")
    void is24Hours_NullDay_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.MIDNIGHT, LocalTime.of(23, 59));
        assertFalse(service.is24Hours(restaurant, null));
    }

    @Test
    @DisplayName("is24Hours 無營業時間返回 false")
    void is24Hours_NoBusinessHours_ReturnsFalse() {
        Restaurant restaurant = new Restaurant("1", "Test");
        assertFalse(service.is24Hours(restaurant, DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("is24Hours 該日無營業返回 false")
    void is24Hours_NoDayHours_ReturnsFalse() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        restaurant.setBusinessHours(new BusinessHours());
        assertFalse(service.is24Hours(restaurant, DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("is24Hours 非 24 小時返回 false")
    void is24Hours_Not24Hours_ReturnsFalse() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        assertFalse(service.is24Hours(restaurant, DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("calculateWeeklyOperatingHours 無營業時間返回 0")
    void calculateWeeklyOperatingHours_NoBusinessHours_ReturnsZero() {
        Restaurant restaurant = new Restaurant("1", "Test");
        assertEquals(0.0, service.calculateWeeklyOperatingHours(restaurant));
    }

    @Test
    @DisplayName("calculateWeeklyOperatingHours 跨夜計算正確")
    void calculateWeeklyOperatingHours_OvernightHours_CalculatesCorrectly() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        // 22:00 - 02:00 = 4 hours
        hours.setHours(DayOfWeek.FRIDAY, LocalTime.of(22, 0), LocalTime.of(2, 0));
        hours.setHours(DayOfWeek.SATURDAY, LocalTime.of(22, 0), LocalTime.of(2, 0));
        restaurant.setBusinessHours(hours);
        double weeklyHours = service.calculateWeeklyOperatingHours(restaurant);
        assertEquals(8.0, weeklyHours); // 4 hours * 2 days
    }

    @Test
    @DisplayName("calculateWeeklyOperatingHours 含分鐘計算正確")
    void calculateWeeklyOperatingHours_WithMinutes_CalculatesCorrectly() {
        Restaurant restaurant = new Restaurant("1", "Test");
        restaurant.setActive(true);
        BusinessHours hours = new BusinessHours();
        // 9:00 - 21:30 = 12.5 hours
        hours.setHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(21, 30));
        restaurant.setBusinessHours(hours);
        double weeklyHours = service.calculateWeeklyOperatingHours(restaurant);
        assertEquals(12.5, weeklyHours);
    }

    @Test
    @DisplayName("findOpenNow 包裝 findOpenRestaurants")
    void findOpenNow_WrapsFindOpenRestaurants() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(0, 0), LocalTime.of(23, 59));
        List<Restaurant> result = service.findOpenNow(List.of(restaurant));
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("isOpenNow 包裝 isOpenAt")
    void isOpenNow_WrapsIsOpenAt() {
        Restaurant restaurant = createRestaurantWithHours(LocalTime.of(0, 0), LocalTime.of(23, 59));
        assertTrue(service.isOpenNow(restaurant));
    }
}
