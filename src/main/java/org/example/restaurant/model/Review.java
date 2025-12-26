package org.example.restaurant.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a review for a restaurant.
 */
public class Review {
    private String id;
    private String restaurantId;
    private String userId;
    private String userName;
    private int rating; // 1-5 stars
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int helpfulCount;
    private boolean verified;
    private int userLevel; // 1-5, used for weighted ratings

    public Review() {
        this.createdAt = LocalDateTime.now();
        this.userLevel = 1;
    }

    public Review(String id, String restaurantId, int rating, String comment) {
        this();
        this.id = id;
        this.restaurantId = restaurantId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public boolean isValid() {
        if (rating < 1 || rating > 5) {
            return false;
        }
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            return false;
        }
        if (userLevel < 1 || userLevel > 5) {
            return false;
        }
        return true;
    }

    public double getWeight() {
        // Weight based on user level and verification status
        double weight = userLevel * 0.2; // 0.2 to 1.0
        if (verified) {
            weight *= 1.5;
        }
        if (helpfulCount > 10) {
            weight *= 1.2;
        } else if (helpfulCount > 5) {
            weight *= 1.1;
        }
        return Math.min(weight, 2.0); // Cap at 2.0
    }

    public boolean isRecent() {
        if (createdAt == null) {
            return false;
        }
        return createdAt.isAfter(LocalDateTime.now().minusMonths(6));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", rating=" + rating +
                ", comment='" + (comment != null && comment.length() > 50 ? comment.substring(0, 50) + "..." : comment)
                + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
