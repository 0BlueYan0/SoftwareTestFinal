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
}
