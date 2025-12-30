package org.example.restaurant.service;

import org.example.restaurant.model.BusinessHours;
import org.example.restaurant.model.Restaurant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for handling business hours operations.
 */
public class BusinessHoursService {

    // Taiwan public holidays (simplified)
    private static final Set<LocalDate> HOLIDAYS = new HashSet<>();

    static {
        // 2024-2025 Taiwan holidays (example)
        HOLIDAYS.add(LocalDate.of(2024, 1, 1)); // New Year
        HOLIDAYS.add(LocalDate.of(2024, 2, 8)); // Lunar New Year Eve
        HOLIDAYS.add(LocalDate.of(2024, 2, 9)); // Lunar New Year
        HOLIDAYS.add(LocalDate.of(2024, 2, 10));
        HOLIDAYS.add(LocalDate.of(2024, 2, 11));
        HOLIDAYS.add(LocalDate.of(2024, 2, 12));
        HOLIDAYS.add(LocalDate.of(2024, 2, 13));
        HOLIDAYS.add(LocalDate.of(2024, 2, 14)); // Valentine's also LNY
        HOLIDAYS.add(LocalDate.of(2024, 4, 4)); // Children's Day
        HOLIDAYS.add(LocalDate.of(2024, 4, 5)); // Tomb Sweeping Day
        HOLIDAYS.add(LocalDate.of(2024, 6, 10)); // Dragon Boat Festival
        HOLIDAYS.add(LocalDate.of(2024, 9, 17)); // Mid-Autumn Festival
        HOLIDAYS.add(LocalDate.of(2024, 10, 10)); // National Day
        HOLIDAYS.add(LocalDate.of(2025, 1, 1)); // New Year 2025
        HOLIDAYS.add(LocalDate.of(2025, 1, 28)); // Lunar New Year 2025
        HOLIDAYS.add(LocalDate.of(2025, 1, 29));
        HOLIDAYS.add(LocalDate.of(2025, 1, 30));
        HOLIDAYS.add(LocalDate.of(2025, 1, 31));
        HOLIDAYS.add(LocalDate.of(2025, 2, 1));
    }

    /**
     * Check if a restaurant is open now.
     * v(G) = ~8
     */
    public boolean isOpenNow(Restaurant restaurant) {
        return isOpenAt(restaurant, LocalDateTime.now());
    }

    /**
     * Check if a restaurant is open at a specific time.
     * v(G) = ~12
     */
    public boolean isOpenAt(Restaurant restaurant, LocalDateTime dateTime) {
        if (restaurant == null || dateTime == null) {
            return false;
        }

        if (!restaurant.isActive()) {
            return false;
        }

        BusinessHours hours = restaurant.getBusinessHours();
        if (hours == null) {
            return false;
        }

        // Check holiday closure
        LocalDate date = dateTime.toLocalDate();
        if (hours.isClosedOnHolidays() && isHoliday(date)) {
            return false;
        }

        // Check regular hours
        DayOfWeek day = dateTime.getDayOfWeek();
        BusinessHours.TimeSlot slot = hours.getHours(day);

        if (slot == null) {
            return false;
        }

        LocalTime time = dateTime.toLocalTime();
        return slot.contains(time);
    }

    /**
     * Check if a date is a public holiday.
     * v(G) = ~4
     */
    public boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return HOLIDAYS.contains(date);
    }

    /**
     * Add a custom holiday.
     */
    public void addHoliday(LocalDate date) {
        if (date != null) {
            HOLIDAYS.add(date);
        }
    }

    /**
     * Remove a holiday.
     */
    public void removeHoliday(LocalDate date) {
        if (date != null) {
            HOLIDAYS.remove(date);
        }
    }

    /**
     * Find restaurants that are open at a specific time.
     * v(G) = ~6
     */
    public List<Restaurant> findOpenRestaurants(List<Restaurant> restaurants,
            LocalDateTime dateTime) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }

        final LocalDateTime checkTime = dateTime;

        return restaurants.stream()
                .filter(r -> r != null)
                .filter(r -> isOpenAt(r, checkTime))
                .collect(Collectors.toList());
    }

    /**
     * Find restaurants open now.
     */
    public List<Restaurant> findOpenNow(List<Restaurant> restaurants) {
        return findOpenRestaurants(restaurants, LocalDateTime.now());
    }

    /**
     * Get next open time for a restaurant.
     * v(G) = ~10
     */
    public LocalDateTime getNextOpenTime(Restaurant restaurant) {
        return getNextOpenTime(restaurant, LocalDateTime.now());
    }

    public LocalDateTime getNextOpenTime(Restaurant restaurant, LocalDateTime from) {
        if (restaurant == null || from == null) {
            return null;
        }

        if (!restaurant.isActive()) {
            return null;
        }

        BusinessHours hours = restaurant.getBusinessHours();
        if (hours == null) {
            return null;
        }

        // Check if currently open
        if (isOpenAt(restaurant, from)) {
            return from;
        }

        // Search next 14 days
        for (int i = 0; i <= 14; i++) {
            LocalDate date = from.toLocalDate().plusDays(i);

            // Skip holidays if restaurant closes on holidays
            if (hours.isClosedOnHolidays() && isHoliday(date)) {
                continue;
            }

            DayOfWeek day = date.getDayOfWeek();
            BusinessHours.TimeSlot slot = hours.getHours(day);

            if (slot == null) {
                continue;
            }

            LocalDateTime openTime = LocalDateTime.of(date, slot.getOpenTime());

            if (i == 0) {
                // Same day - only if opening time is in the future
                if (openTime.isAfter(from)) {
                    return openTime;
                }
            } else {
                return openTime;
            }
        }

        return null; // Restaurant doesn't open in the next 14 days
    }

    /**
     * Get closing time for today.
     * v(G) = ~6
     */
    public LocalTime getClosingTimeToday(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }

        BusinessHours hours = restaurant.getBusinessHours();
        if (hours == null) {
            return null;
        }

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        BusinessHours.TimeSlot slot = hours.getHours(today);

        if (slot == null) {
            return null;
        }

        return slot.getCloseTime();
    }

    /**
     * Check if restaurant is closing soon (within minutes).
     * v(G) = ~8
     */
    public boolean isClosingSoon(Restaurant restaurant, int withinMinutes) {
        if (restaurant == null || withinMinutes <= 0) {
            return false;
        }

        if (!isOpenNow(restaurant)) {
            return false;
        }

        LocalTime closingTime = getClosingTimeToday(restaurant);
        if (closingTime == null) {
            return false;
        }

        LocalTime now = LocalTime.now();
        LocalTime threshold = now.plusMinutes(withinMinutes);

        // Handle overnight closing (simplified for same-day checks)
        // If closing time is earlier than now, it means it closes tomorrow morning
        // (e.g. 02:00 vs 23:00)
        // For this simple logic, we might skip it or handle it complexly.
        // Existing logic skipped it.
        if (closingTime.isBefore(now)) {
            return false;
        }

        // Fix: If threshold wraps around midnight (e.g., now 23:30 + 60m = 00:30)
        // And closingTime >= now (e.g., 23:45)
        // Then closingTime is definitely before threshold (conceptually)
        if (threshold.isBefore(now)) {
            return true;
        }

        return closingTime.isBefore(threshold) || closingTime.equals(threshold);
    }

    /**
     * Get restaurants that will close soon.
     * v(G) = ~4
     */
    public List<Restaurant> findClosingSoon(List<Restaurant> restaurants, int withinMinutes) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        return restaurants.stream()
                .filter(r -> r != null)
                .filter(r -> isClosingSoon(r, withinMinutes))
                .collect(Collectors.toList());
    }

    /**
     * Get operating days count per week.
     * v(G) = ~5
     */
    public int getOperatingDaysCount(Restaurant restaurant) {
        if (restaurant == null) {
            return 0;
        }

        BusinessHours hours = restaurant.getBusinessHours();
        if (hours == null) {
            return 0;
        }

        int count = 0;
        for (DayOfWeek day : DayOfWeek.values()) {
            if (hours.getHours(day) != null) {
                count++;
            }
        }

        return count;
    }

    /**
     * Get business hours summary string.
     * v(G) = ~8
     */
    public String getBusinessHoursSummary(Restaurant restaurant) {
        if (restaurant == null) {
            return "無資料";
        }

        BusinessHours hours = restaurant.getBusinessHours();
        if (hours == null) {
            return "無營業時間資料";
        }

        int operatingDays = getOperatingDaysCount(restaurant);

        if (operatingDays == 0) {
            return "目前休業中";
        } else if (operatingDays == 7) {
            return "每日營業";
        } else if (operatingDays >= 5) {
            return "週一至週五營業";
        } else {
            return "部分時段營業";
        }
    }

    /**
     * Check if restaurant is open 24 hours on a day.
     * v(G) = ~5
     */
    public boolean is24Hours(Restaurant restaurant, DayOfWeek day) {
        if (restaurant == null || day == null) {
            return false;
        }

        BusinessHours hours = restaurant.getBusinessHours();
        if (hours == null) {
            return false;
        }

        BusinessHours.TimeSlot slot = hours.getHours(day);
        if (slot == null) {
            return false;
        }

        return slot.getOpenTime().equals(LocalTime.MIDNIGHT)
                && slot.getCloseTime().equals(LocalTime.of(23, 59));
    }

    /**
     * Calculate total operating hours per week.
     * v(G) = ~8
     */
    public double calculateWeeklyOperatingHours(Restaurant restaurant) {
        if (restaurant == null) {
            return 0.0;
        }

        BusinessHours hours = restaurant.getBusinessHours();
        if (hours == null) {
            return 0.0;
        }

        double totalHours = 0.0;

        for (DayOfWeek day : DayOfWeek.values()) {
            BusinessHours.TimeSlot slot = hours.getHours(day);
            if (slot != null && slot.getOpenTime() != null && slot.getCloseTime() != null) {
                LocalTime open = slot.getOpenTime();
                LocalTime close = slot.getCloseTime();

                // Calculate hours
                double hoursOpen;
                if (close.isBefore(open)) {
                    // Overnight - e.g., 22:00 to 02:00
                    hoursOpen = (24 - open.getHour()) + close.getHour();
                } else {
                    hoursOpen = close.getHour() - open.getHour();
                    hoursOpen += (close.getMinute() - open.getMinute()) / 60.0;
                }

                totalHours += hoursOpen;
            }
        }

        return Math.round(totalHours * 10.0) / 10.0;
    }
}
