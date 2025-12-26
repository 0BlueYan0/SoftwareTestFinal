package org.example.restaurant.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a restaurant with all its attributes.
 */
public class Restaurant {
    private String id;
    private String name;
    private String description;
    private Location location;
    private CuisineType cuisineType;
    private Set<CuisineType> additionalCuisineTypes;
    private List<MenuItem> menu;
    private List<Review> reviews;
    private BusinessHours businessHours;
    private double averagePrice;
    private int priceLevel; // 1-4 ($ to $$$$)
    private boolean active;
    private String phoneNumber;
    private String website;
    private int capacity;
    private boolean hasDelivery;
    private boolean hasTakeout;
    private boolean hasParking;
    private boolean acceptsReservations;

    public Restaurant() {
        this.additionalCuisineTypes = new HashSet<>();
        this.menu = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.active = true;
    }

    public Restaurant(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public Restaurant(String id, String name, CuisineType cuisineType, Location location) {
        this(id, name);
        this.cuisineType = cuisineType;
        this.location = location;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public CuisineType getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(CuisineType cuisineType) {
        this.cuisineType = cuisineType;
    }

    public Set<CuisineType> getAdditionalCuisineTypes() {
        return additionalCuisineTypes;
    }

    public void setAdditionalCuisineTypes(Set<CuisineType> additionalCuisineTypes) {
        this.additionalCuisineTypes = additionalCuisineTypes;
    }

    public void addCuisineType(CuisineType type) {
        if (type != null) {
            additionalCuisineTypes.add(type);
        }
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public void setMenu(List<MenuItem> menu) {
        this.menu = menu;
    }

    public void addMenuItem(MenuItem item) {
        if (item != null) {
            menu.add(item);
        }
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        if (review != null) {
            reviews.add(review);
        }
    }

    public BusinessHours getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(BusinessHours businessHours) {
        this.businessHours = businessHours;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isHasDelivery() {
        return hasDelivery;
    }

    public void setHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }

    public boolean isHasTakeout() {
        return hasTakeout;
    }

    public void setHasTakeout(boolean hasTakeout) {
        this.hasTakeout = hasTakeout;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isAcceptsReservations() {
        return acceptsReservations;
    }

    public void setAcceptsReservations(boolean acceptsReservations) {
        this.acceptsReservations = acceptsReservations;
    }

    // Business methods
    public boolean hasCuisineType(CuisineType type) {
        if (type == null) {
            return false;
        }
        if (cuisineType == type) {
            return true;
        }
        return additionalCuisineTypes.contains(type);
    }

    public boolean isOpenNow() {
        if (businessHours == null) {
            return false;
        }
        return businessHours.isOpenNow();
    }

    public double calculateAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        int count = 0;
        for (Review review : reviews) {
            if (review != null && review.getRating() >= 1 && review.getRating() <= 5) {
                sum += review.getRating();
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    public double calculateMenuAveragePrice() {
        if (menu == null || menu.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        int count = 0;
        for (MenuItem item : menu) {
            if (item != null && item.getPrice() > 0 && item.isAvailable()) {
                sum += item.getPrice();
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    public int getReviewCount() {
        return reviews != null ? reviews.size() : 0;
    }

    public int getMenuItemCount() {
        return menu != null ? menu.size() : 0;
    }

    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase();

        if (name != null && name.toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        if (description != null && description.toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        if (cuisineType != null && cuisineType.getDisplayName().toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        if (location != null) {
            if (location.getCity() != null && location.getCity().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (location.getAddress() != null && location.getAddress().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cuisineType=" + cuisineType +
                ", city=" + (location != null ? location.getCity() : "N/A") +
                ", rating=" + String.format("%.1f", calculateAverageRating()) +
                '}';
    }
}
