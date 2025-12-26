package org.example.restaurant.service;

import org.example.restaurant.exception.ValidationException;
import org.example.restaurant.model.*;

import java.util.List;

/**
 * Service for validating input data.
 * This class provides comprehensive validation with high cyclomatic complexity.
 */
public class InputValidator {

    /**
     * Validates a restaurant object.
     * v(G) = ~15
     */
    public void validateRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new ValidationException("Restaurant cannot be null", "restaurant");
        }

        // Validate ID
        if (restaurant.getId() == null || restaurant.getId().trim().isEmpty()) {
            throw new ValidationException("Restaurant ID is required", "id");
        }
        if (restaurant.getId().length() > 50) {
            throw new ValidationException("Restaurant ID cannot exceed 50 characters", "id");
        }

        // Validate name
        if (restaurant.getName() == null || restaurant.getName().trim().isEmpty()) {
            throw new ValidationException("Restaurant name is required", "name");
        }
        if (restaurant.getName().length() < 2) {
            throw new ValidationException("Restaurant name must be at least 2 characters", "name");
        }
        if (restaurant.getName().length() > 100) {
            throw new ValidationException("Restaurant name cannot exceed 100 characters", "name");
        }

        // Validate price level
        if (restaurant.getPriceLevel() < 0 || restaurant.getPriceLevel() > 4) {
            throw new ValidationException("Price level must be between 0 and 4", "priceLevel");
        }

        // Validate average price
        if (restaurant.getAveragePrice() < 0) {
            throw new ValidationException("Average price cannot be negative", "averagePrice");
        }

        // Validate capacity
        if (restaurant.getCapacity() < 0) {
            throw new ValidationException("Capacity cannot be negative", "capacity");
        }

        // Validate phone number format (if present)
        if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
            if (!isValidPhoneNumber(restaurant.getPhoneNumber())) {
                throw new ValidationException("Invalid phone number format", "phoneNumber");
            }
        }

        // Validate website format (if present)
        if (restaurant.getWebsite() != null && !restaurant.getWebsite().isEmpty()) {
            if (!isValidUrl(restaurant.getWebsite())) {
                throw new ValidationException("Invalid website URL format", "website");
            }
        }

        // Validate location if present
        if (restaurant.getLocation() != null) {
            validateLocation(restaurant.getLocation());
        }

        // Validate business hours if present
        if (restaurant.getBusinessHours() != null) {
            validateBusinessHours(restaurant.getBusinessHours());
        }
    }

    /**
     * Validates a location object.
     * v(G) = ~12
     */
    public void validateLocation(Location location) {
        if (location == null) {
            throw new ValidationException("Location cannot be null", "location");
        }

        // Validate latitude
        if (location.getLatitude() < -90 || location.getLatitude() > 90) {
            throw new ValidationException("Latitude must be between -90 and 90", "latitude");
        }

        // Validate longitude
        if (location.getLongitude() < -180 || location.getLongitude() > 180) {
            throw new ValidationException("Longitude must be between -180 and 180", "longitude");
        }

        // Validate city
        if (location.getCity() != null) {
            if (location.getCity().length() > 100) {
                throw new ValidationException("City name cannot exceed 100 characters", "city");
            }
            if (!location.getCity().isEmpty() && location.getCity().length() < 2) {
                throw new ValidationException("City name must be at least 2 characters", "city");
            }
        }

        // Validate address
        if (location.getAddress() != null && location.getAddress().length() > 200) {
            throw new ValidationException("Address cannot exceed 200 characters", "address");
        }

        // Validate postal code
        if (location.getPostalCode() != null && !location.getPostalCode().isEmpty()) {
            if (!isValidPostalCode(location.getPostalCode())) {
                throw new ValidationException("Invalid postal code format", "postalCode");
            }
        }
    }

    /**
     * Validates a review object.
     * v(G) = ~14
     */
    public void validateReview(Review review) {
        if (review == null) {
            throw new ValidationException("Review cannot be null", "review");
        }

        // Validate ID
        if (review.getId() == null || review.getId().trim().isEmpty()) {
            throw new ValidationException("Review ID is required", "id");
        }

        // Validate restaurant ID
        if (review.getRestaurantId() == null || review.getRestaurantId().trim().isEmpty()) {
            throw new ValidationException("Restaurant ID is required for review", "restaurantId");
        }

        // Validate rating
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new ValidationException("Rating must be between 1 and 5", "rating");
        }

        // Validate comment
        if (review.getComment() != null) {
            if (review.getComment().length() > 2000) {
                throw new ValidationException("Comment cannot exceed 2000 characters", "comment");
            }
            if (containsProfanity(review.getComment())) {
                throw new ValidationException("Comment contains inappropriate content", "comment");
            }
        }

        // Validate user level
        if (review.getUserLevel() < 1 || review.getUserLevel() > 5) {
            throw new ValidationException("User level must be between 1 and 5", "userLevel");
        }

        // Validate helpful count
        if (review.getHelpfulCount() < 0) {
            throw new ValidationException("Helpful count cannot be negative", "helpfulCount");
        }

        // Validate user name if present
        if (review.getUserName() != null) {
            if (review.getUserName().length() < 2) {
                throw new ValidationException("User name must be at least 2 characters", "userName");
            }
            if (review.getUserName().length() > 50) {
                throw new ValidationException("User name cannot exceed 50 characters", "userName");
            }
        }
    }

    /**
     * Validates search criteria.
     * v(G) = ~12
     */
    public void validateSearchCriteria(SearchCriteria criteria) {
        if (criteria == null) {
            throw new ValidationException("Search criteria cannot be null", "criteria");
        }

        // Validate keyword
        if (criteria.getKeyword() != null && criteria.getKeyword().length() > 100) {
            throw new ValidationException("Search keyword cannot exceed 100 characters", "keyword");
        }

        // Validate rating range
        if (criteria.getMinRating() != null) {
            if (criteria.getMinRating() < 0 || criteria.getMinRating() > 5) {
                throw new ValidationException("Minimum rating must be between 0 and 5", "minRating");
            }
        }
        if (criteria.getMaxRating() != null) {
            if (criteria.getMaxRating() < 0 || criteria.getMaxRating() > 5) {
                throw new ValidationException("Maximum rating must be between 0 and 5", "maxRating");
            }
        }
        if (criteria.getMinRating() != null && criteria.getMaxRating() != null) {
            if (criteria.getMinRating() > criteria.getMaxRating()) {
                throw new ValidationException("Minimum rating cannot exceed maximum rating", "minRating");
            }
        }

        // Validate price range
        if (criteria.getMinPrice() != null && criteria.getMinPrice() < 0) {
            throw new ValidationException("Minimum price cannot be negative", "minPrice");
        }
        if (criteria.getMaxPrice() != null && criteria.getMaxPrice() < 0) {
            throw new ValidationException("Maximum price cannot be negative", "maxPrice");
        }
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            if (criteria.getMinPrice() > criteria.getMaxPrice()) {
                throw new ValidationException("Minimum price cannot exceed maximum price", "minPrice");
            }
        }

        // Validate price level
        if (criteria.getPriceLevel() != null) {
            if (criteria.getPriceLevel() < 1 || criteria.getPriceLevel() > 4) {
                throw new ValidationException("Price level must be between 1 and 4", "priceLevel");
            }
        }

        // Validate location parameters
        if (criteria.hasLocationFilter()) {
            if (criteria.getLatitude() < -90 || criteria.getLatitude() > 90) {
                throw new ValidationException("Latitude must be between -90 and 90", "latitude");
            }
            if (criteria.getLongitude() < -180 || criteria.getLongitude() > 180) {
                throw new ValidationException("Longitude must be between -180 and 180", "longitude");
            }
            if (criteria.getRadiusKm() <= 0 || criteria.getRadiusKm() > 100) {
                throw new ValidationException("Radius must be between 0 and 100 km", "radiusKm");
            }
        }

        // Validate pagination
        if (criteria.getLimit() < 1 || criteria.getLimit() > 100) {
            throw new ValidationException("Limit must be between 1 and 100", "limit");
        }
        if (criteria.getOffset() < 0) {
            throw new ValidationException("Offset cannot be negative", "offset");
        }
    }

    /**
     * Validates business hours.
     * v(G) = ~8
     */
    public void validateBusinessHours(BusinessHours hours) {
        if (hours == null) {
            throw new ValidationException("Business hours cannot be null", "businessHours");
        }

        if (hours.getWeeklyHours() != null) {
            for (var entry : hours.getWeeklyHours().entrySet()) {
                BusinessHours.TimeSlot slot = entry.getValue();
                if (slot != null) {
                    if (slot.getOpenTime() == null) {
                        throw new ValidationException("Open time is required", "openTime");
                    }
                    if (slot.getCloseTime() == null) {
                        throw new ValidationException("Close time is required", "closeTime");
                    }
                }
            }
        }
    }

    /**
     * Validates a menu item.
     * v(G) = ~10
     */
    public void validateMenuItem(MenuItem item) {
        if (item == null) {
            throw new ValidationException("Menu item cannot be null", "menuItem");
        }

        // Validate ID
        if (item.getId() == null || item.getId().trim().isEmpty()) {
            throw new ValidationException("Menu item ID is required", "id");
        }

        // Validate name
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new ValidationException("Menu item name is required", "name");
        }
        if (item.getName().length() > 100) {
            throw new ValidationException("Menu item name cannot exceed 100 characters", "name");
        }

        // Validate price
        if (item.getPrice() < 0) {
            throw new ValidationException("Price cannot be negative", "price");
        }
        if (item.getPrice() > 100000) {
            throw new ValidationException("Price exceeds maximum allowed value", "price");
        }

        // Validate calories
        if (item.getCalories() < 0) {
            throw new ValidationException("Calories cannot be negative", "calories");
        }

        // Validate description
        if (item.getDescription() != null && item.getDescription().length() > 500) {
            throw new ValidationException("Description cannot exceed 500 characters", "description");
        }

        // Validate category
        if (item.getCategory() != null && item.getCategory().length() > 50) {
            throw new ValidationException("Category cannot exceed 50 characters", "category");
        }
    }

    // Helper methods
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Remove common formatting characters
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");
        // Check if remaining characters are digits
        if (!cleaned.matches("\\d+")) {
            return false;
        }
        // Check length (between 7 and 15 digits)
        return cleaned.length() >= 7 && cleaned.length() <= 15;
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        // Simple URL validation
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private boolean isValidPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isEmpty()) {
            return false;
        }
        // Taiwan postal code (3 or 5 digits) or generic alphanumeric
        return postalCode.matches("\\d{3,5}") ||
                postalCode.matches("[A-Za-z0-9\\s\\-]{3,10}");
    }

    private boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // Simple profanity check - would be more comprehensive in production
        String[] badWords = { "spam", "scam", "fake" };
        String lowerText = text.toLowerCase();
        for (String word : badWords) {
            if (lowerText.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
