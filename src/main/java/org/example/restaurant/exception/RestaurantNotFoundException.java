package org.example.restaurant.exception;

/**
 * Exception thrown when a restaurant is not found.
 */
public class RestaurantNotFoundException extends RuntimeException {
    private final String restaurantId;

    public RestaurantNotFoundException(String message) {
        super(message);
        this.restaurantId = null;
    }

    public RestaurantNotFoundException(String message, String restaurantId) {
        super(message);
        this.restaurantId = restaurantId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RestaurantNotFoundException{");
        sb.append("message='").append(getMessage()).append('\'');
        if (restaurantId != null) {
            sb.append(", restaurantId='").append(restaurantId).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
