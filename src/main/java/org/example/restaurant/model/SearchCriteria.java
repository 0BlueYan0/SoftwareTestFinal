package org.example.restaurant.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents search criteria for filtering restaurants.
 */
public class SearchCriteria {
    private String keyword;
    private String city;
    private String district;
    private CuisineType cuisineType;
    private Set<CuisineType> cuisineTypes;
    private Double minRating;
    private Double maxRating;
    private Double minPrice;
    private Double maxPrice;
    private Integer priceLevel;
    private Boolean openNow;
    private Boolean hasDelivery;
    private Boolean hasTakeout;
    private Boolean hasParking;
    private Boolean acceptsReservations;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private SortType sortBy;
    private boolean ascending;
    private int limit;
    private int offset;

    public enum SortType {
        NAME, RATING, PRICE, DISTANCE, REVIEW_COUNT, RELEVANCE
    }

    public SearchCriteria() {
        this.cuisineTypes = new HashSet<>();
        this.ascending = true;
        this.limit = 20;
        this.offset = 0;
    }

    // Builder pattern methods
    public SearchCriteria keyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public SearchCriteria city(String city) {
        this.city = city;
        return this;
    }

    public SearchCriteria district(String district) {
        this.district = district;
        return this;
    }

    public SearchCriteria cuisineType(CuisineType cuisineType) {
        this.cuisineType = cuisineType;
        return this;
    }

    public SearchCriteria addCuisineType(CuisineType type) {
        if (type != null) {
            this.cuisineTypes.add(type);
        }
        return this;
    }

    public SearchCriteria minRating(Double minRating) {
        this.minRating = minRating;
        return this;
    }

    public SearchCriteria maxRating(Double maxRating) {
        this.maxRating = maxRating;
        return this;
    }

    public SearchCriteria minPrice(Double minPrice) {
        this.minPrice = minPrice;
        return this;
    }

    public SearchCriteria maxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    public SearchCriteria priceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
        return this;
    }

    public SearchCriteria openNow(Boolean openNow) {
        this.openNow = openNow;
        return this;
    }

    public SearchCriteria hasDelivery(Boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
        return this;
    }

    public SearchCriteria hasTakeout(Boolean hasTakeout) {
        this.hasTakeout = hasTakeout;
        return this;
    }

    public SearchCriteria hasParking(Boolean hasParking) {
        this.hasParking = hasParking;
        return this;
    }

    public SearchCriteria acceptsReservations(Boolean acceptsReservations) {
        this.acceptsReservations = acceptsReservations;
        return this;
    }

    public SearchCriteria nearLocation(double latitude, double longitude, double radiusKm) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
        return this;
    }

    public SearchCriteria sortBy(SortType sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public SearchCriteria ascending(boolean ascending) {
        this.ascending = ascending;
        return this;
    }

    public SearchCriteria limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SearchCriteria offset(int offset) {
        this.offset = offset;
        return this;
    }

    // Getters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public CuisineType getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(CuisineType cuisineType) {
        this.cuisineType = cuisineType;
    }

    public Set<CuisineType> getCuisineTypes() {
        return cuisineTypes;
    }

    public void setCuisineTypes(Set<CuisineType> cuisineTypes) {
        this.cuisineTypes = cuisineTypes;
    }

    public Double getMinRating() {
        return minRating;
    }

    public void setMinRating(Double minRating) {
        this.minRating = minRating;
    }

    public Double getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(Double maxRating) {
        this.maxRating = maxRating;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public Boolean getHasDelivery() {
        return hasDelivery;
    }

    public void setHasDelivery(Boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }

    public Boolean getHasTakeout() {
        return hasTakeout;
    }

    public void setHasTakeout(Boolean hasTakeout) {
        this.hasTakeout = hasTakeout;
    }

    public Boolean getHasParking() {
        return hasParking;
    }

    public void setHasParking(Boolean hasParking) {
        this.hasParking = hasParking;
    }

    public Boolean getAcceptsReservations() {
        return acceptsReservations;
    }

    public void setAcceptsReservations(Boolean acceptsReservations) {
        this.acceptsReservations = acceptsReservations;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public SortType getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortType sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean hasLocationFilter() {
        return latitude != null && longitude != null && radiusKm != null && radiusKm > 0;
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null || priceLevel != null;
    }

    public boolean hasRatingFilter() {
        return minRating != null || maxRating != null;
    }

    public boolean isEmpty() {
        return keyword == null && city == null && district == null
                && cuisineType == null && cuisineTypes.isEmpty()
                && minRating == null && maxRating == null
                && minPrice == null && maxPrice == null && priceLevel == null
                && openNow == null && hasDelivery == null && hasTakeout == null
                && hasParking == null && acceptsReservations == null
                && !hasLocationFilter();
    }
}
