package org.example.restaurant.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the business hours of a restaurant.
 */
public class BusinessHours {
    private Map<DayOfWeek, TimeSlot> weeklyHours;
    private boolean closedOnHolidays;

    public BusinessHours() {
        this.weeklyHours = new EnumMap<>(DayOfWeek.class);
        this.closedOnHolidays = false;
    }

    public void setHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime) {
        if (openTime != null && closeTime != null) {
            weeklyHours.put(day, new TimeSlot(openTime, closeTime));
        }
    }

    public void setClosed(DayOfWeek day) {
        weeklyHours.put(day, null);
    }

    public TimeSlot getHours(DayOfWeek day) {
        return weeklyHours.get(day);
    }

    public boolean isClosedOnHolidays() {
        return closedOnHolidays;
    }

    public void setClosedOnHolidays(boolean closedOnHolidays) {
        this.closedOnHolidays = closedOnHolidays;
    }

    public Map<DayOfWeek, TimeSlot> getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(Map<DayOfWeek, TimeSlot> weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public boolean isOpenAt(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        DayOfWeek day = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        TimeSlot slot = weeklyHours.get(day);
        if (slot == null) {
            return false;
        }

        return slot.contains(time);
    }

    public boolean isOpenNow() {
        return isOpenAt(LocalDateTime.now());
    }

    public LocalDateTime getNextOpenTime(LocalDateTime from) {
        if (from == null) {
            from = LocalDateTime.now();
        }

        // Check up to 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate date = from.toLocalDate().plusDays(i);
            DayOfWeek day = date.getDayOfWeek();
            TimeSlot slot = weeklyHours.get(day);

            if (slot != null) {
                LocalDateTime openTime = LocalDateTime.of(date, slot.getOpenTime());
                if (i == 0) {
                    // Same day - check if it's still in the future
                    if (openTime.isAfter(from)) {
                        return openTime;
                    }
                    // Already open or past opening time
                    if (slot.contains(from.toLocalTime())) {
                        return from; // Already open
                    }
                } else {
                    return openTime;
                }
            }
        }
        return null; // Never opens
    }

    /**
     * Represents a time slot with open and close times.
     */
    public static class TimeSlot {
        private LocalTime openTime;
        private LocalTime closeTime;

        public TimeSlot() {
        }

        public TimeSlot(LocalTime openTime, LocalTime closeTime) {
            this.openTime = openTime;
            this.closeTime = closeTime;
        }

        public LocalTime getOpenTime() {
            return openTime;
        }

        public void setOpenTime(LocalTime openTime) {
            this.openTime = openTime;
        }

        public LocalTime getCloseTime() {
            return closeTime;
        }

        public void setCloseTime(LocalTime closeTime) {
            this.closeTime = closeTime;
        }

        public boolean contains(LocalTime time) {
            if (time == null || openTime == null || closeTime == null) {
                return false;
            }

            // Handle overnight hours (e.g., 22:00 - 02:00)
            if (closeTime.isBefore(openTime)) {
                return !time.isBefore(openTime) || !time.isAfter(closeTime);
            }

            return !time.isBefore(openTime) && !time.isAfter(closeTime);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TimeSlot timeSlot = (TimeSlot) o;
            return Objects.equals(openTime, timeSlot.openTime)
                    && Objects.equals(closeTime, timeSlot.closeTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(openTime, closeTime);
        }

        @Override
        public String toString() {
            return openTime + " - " + closeTime;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BusinessHours{\n");
        for (DayOfWeek day : DayOfWeek.values()) {
            TimeSlot slot = weeklyHours.get(day);
            sb.append("  ").append(day).append(": ");
            if (slot != null) {
                sb.append(slot);
            } else {
                sb.append("Closed");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
