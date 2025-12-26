package org.example.restaurant.service;

import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service for searching and filtering restaurants.
 */
public class RestaurantSearchService {

    private final RestaurantRepository repository;
    private final RatingService ratingService;
    private final PriceAnalyzer priceAnalyzer;
    private final BusinessHoursService businessHoursService;
    private final RecommendationService recommendationService;

    public RestaurantSearchService(RestaurantRepository repository) {
        this.repository = repository;
        this.ratingService = new RatingService();
        this.priceAnalyzer = new PriceAnalyzer();
        this.businessHoursService = new BusinessHoursService();
        this.recommendationService = new RecommendationService(ratingService, priceAnalyzer);
    }

    /**
     * Search restaurants by name (exact match).
     * v(G) = ~5
     */
    public List<Restaurant> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> r.getName() != null && r.getName().equalsIgnoreCase(name.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Search restaurants by name (fuzzy match).
     * v(G) = ~8
     */
    public List<Restaurant> searchByNameFuzzy(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerKeyword = keyword.trim().toLowerCase();

        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> {
                    if (r.getName() == null) {
                        return false;
                    }
                    String lowerName = r.getName().toLowerCase();
                    // Check for contains or starts with
                    return lowerName.contains(lowerKeyword)
                            || lowerName.startsWith(lowerKeyword)
                            || calculateLevenshteinSimilarity(lowerName, lowerKeyword) > 0.6;
                })
                .sorted((r1, r2) -> {
                    // Prioritize exact starts with
                    boolean s1 = r1.getName().toLowerCase().startsWith(lowerKeyword);
                    boolean s2 = r2.getName().toLowerCase().startsWith(lowerKeyword);
                    if (s1 != s2) {
                        return s1 ? -1 : 1;
                    }
                    return r1.getName().compareToIgnoreCase(r2.getName());
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate Levenshtein similarity for fuzzy matching.
     * v(G) = ~6
     */
    private double calculateLevenshteinSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        int maxLen = Math.max(s1.length(), s2.length());
        return 1.0 - ((double) dp[s1.length()][s2.length()] / maxLen);
    }

    /**
     * Search restaurants by city.
     * v(G) = ~6
     */
    public List<Restaurant> searchByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerCity = city.trim().toLowerCase();

        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> {
                    if (r.getLocation() == null || r.getLocation().getCity() == null) {
                        return false;
                    }
                    return r.getLocation().getCity().toLowerCase().contains(lowerCity);
                })
                .collect(Collectors.toList());
    }

    /**
     * Search restaurants by district.
     * v(G) = ~6
     */
    public List<Restaurant> searchByDistrict(String district) {
        if (district == null || district.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerDistrict = district.trim().toLowerCase();

        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> {
                    if (r.getLocation() == null || r.getLocation().getDistrict() == null) {
                        return false;
                    }
                    return r.getLocation().getDistrict().toLowerCase().contains(lowerDistrict);
                })
                .collect(Collectors.toList());
    }

    /**
     * Search restaurants by cuisine type.
     * v(G) = ~5
     */
    public List<Restaurant> searchByCuisineType(CuisineType cuisineType) {
        if (cuisineType == null) {
            return new ArrayList<>();
        }

        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> r.hasCuisineType(cuisineType))
                .collect(Collectors.toList());
    }

    /**
     * Search restaurants by multiple cuisine types.
     * v(G) = ~7
     */
    public List<Restaurant> searchByMultipleCuisineTypes(Set<CuisineType> cuisineTypes) {
        if (cuisineTypes == null || cuisineTypes.isEmpty()) {
            return new ArrayList<>();
        }

        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> {
                    for (CuisineType type : cuisineTypes) {
                        if (r.hasCuisineType(type)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Search with multiple criteria.
     * v(G) = ~25
     */
    public List<Restaurant> searchByMultipleCriteria(SearchCriteria criteria) {
        if (criteria == null) {
            return getAllRestaurants(); // Return only active restaurants
        }

        List<Restaurant> results = repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .collect(Collectors.toList());

        // Apply keyword filter
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            String keyword = criteria.getKeyword().trim().toLowerCase();
            results = results.stream()
                    .filter(r -> r.matchesKeyword(keyword))
                    .collect(Collectors.toList());
        }

        // Apply city filter
        if (criteria.getCity() != null && !criteria.getCity().trim().isEmpty()) {
            String city = criteria.getCity().trim().toLowerCase();
            results = results.stream()
                    .filter(r -> r.getLocation() != null
                            && r.getLocation().getCity() != null
                            && r.getLocation().getCity().toLowerCase().contains(city))
                    .collect(Collectors.toList());
        }

        // Apply district filter
        if (criteria.getDistrict() != null && !criteria.getDistrict().trim().isEmpty()) {
            String district = criteria.getDistrict().trim().toLowerCase();
            results = results.stream()
                    .filter(r -> r.getLocation() != null
                            && r.getLocation().getDistrict() != null
                            && r.getLocation().getDistrict().toLowerCase().contains(district))
                    .collect(Collectors.toList());
        }

        // Apply cuisine type filter
        if (criteria.getCuisineType() != null) {
            CuisineType type = criteria.getCuisineType();
            results = results.stream()
                    .filter(r -> r.hasCuisineType(type))
                    .collect(Collectors.toList());
        }

        // Apply multiple cuisine types filter
        if (criteria.getCuisineTypes() != null && !criteria.getCuisineTypes().isEmpty()) {
            Set<CuisineType> types = criteria.getCuisineTypes();
            results = results.stream()
                    .filter(r -> {
                        for (CuisineType t : types) {
                            if (r.hasCuisineType(t)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // Apply rating filter
        if (criteria.hasRatingFilter()) {
            results = ratingService.filterByRatingRange(results,
                    criteria.getMinRating(), criteria.getMaxRating());
        }

        // Apply price filter
        if (criteria.hasPriceFilter()) {
            if (criteria.getPriceLevel() != null) {
                results = priceAnalyzer.filterByPriceLevel(results, criteria.getPriceLevel());
            } else {
                results = priceAnalyzer.filterByPriceRange(results,
                        criteria.getMinPrice(), criteria.getMaxPrice());
            }
        }

        // Apply open now filter
        if (Boolean.TRUE.equals(criteria.getOpenNow())) {
            results = businessHoursService.findOpenNow(results);
        }

        // Apply feature filters
        if (Boolean.TRUE.equals(criteria.getHasDelivery())) {
            results = results.stream()
                    .filter(Restaurant::isHasDelivery)
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(criteria.getHasTakeout())) {
            results = results.stream()
                    .filter(Restaurant::isHasTakeout)
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(criteria.getHasParking())) {
            results = results.stream()
                    .filter(Restaurant::isHasParking)
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(criteria.getAcceptsReservations())) {
            results = results.stream()
                    .filter(Restaurant::isAcceptsReservations)
                    .collect(Collectors.toList());
        }

        // Apply location filter
        if (criteria.hasLocationFilter()) {
            Location userLocation = new Location(criteria.getLatitude(), criteria.getLongitude());
            results = recommendationService.findNearby(userLocation, results, criteria.getRadiusKm());
        }

        // Apply sorting
        results = sortResults(results, criteria);

        // Apply pagination
        int offset = criteria.getOffset();
        int limit = criteria.getLimit();

        if (offset >= results.size()) {
            return new ArrayList<>();
        }

        int endIndex = Math.min(offset + limit, results.size());
        return results.subList(offset, endIndex);
    }

    /**
     * Sort search results based on criteria.
     * v(G) = ~10
     */
    public List<Restaurant> sortResults(List<Restaurant> results, SearchCriteria criteria) {
        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }

        if (criteria == null || criteria.getSortBy() == null) {
            return new ArrayList<>(results);
        }

        Comparator<Restaurant> comparator;

        switch (criteria.getSortBy()) {
            case NAME:
                comparator = Comparator.comparing(
                        r -> r.getName() != null ? r.getName() : "",
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case RATING:
                comparator = Comparator.comparingDouble(
                        r -> ratingService.calculateAverageRating(r));
                break;
            case PRICE:
                comparator = Comparator.comparingDouble(
                        r -> priceAnalyzer.calculateAveragePrice(r));
                break;
            case REVIEW_COUNT:
                comparator = Comparator.comparingInt(Restaurant::getReviewCount);
                break;
            case DISTANCE:
                if (criteria.hasLocationFilter()) {
                    Location userLocation = new Location(criteria.getLatitude(), criteria.getLongitude());
                    comparator = Comparator.comparingDouble(
                            r -> r.getLocation() != null
                                    ? recommendationService.calculateDistance(userLocation, r.getLocation())
                                    : Double.MAX_VALUE);
                } else {
                    comparator = Comparator.comparing(r -> r.getName() != null ? r.getName() : "");
                }
                break;
            case RELEVANCE:
            default:
                // For relevance, prioritize by rating * review count
                comparator = Comparator.comparingDouble(
                        r -> ratingService.calculateAverageRating(r) * Math.log10(r.getReviewCount() + 1));
                break;
        }

        if (!criteria.isAscending()) {
            comparator = comparator.reversed();
        }

        return results.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Get all restaurants.
     */
    public List<Restaurant> getAllRestaurants() {
        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .collect(Collectors.toList());
    }

    /**
     * Count total restaurants.
     */
    public long countRestaurants() {
        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .count();
    }

    /**
     * Search with keyword across multiple fields.
     * v(G) = ~8
     */
    public List<Restaurant> searchGlobal(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerKeyword = keyword.trim().toLowerCase();

        return repository.findAll().stream()
                .filter(r -> r != null && r.isActive())
                .filter(r -> matchesGlobalSearch(r, lowerKeyword))
                .sorted((r1, r2) -> {
                    // Prioritize name matches
                    boolean name1 = r1.getName() != null
                            && r1.getName().toLowerCase().contains(lowerKeyword);
                    boolean name2 = r2.getName() != null
                            && r2.getName().toLowerCase().contains(lowerKeyword);
                    if (name1 != name2) {
                        return name1 ? -1 : 1;
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    private boolean matchesGlobalSearch(Restaurant r, String keyword) {
        // Check name
        if (r.getName() != null && r.getName().toLowerCase().contains(keyword)) {
            return true;
        }
        // Check description
        if (r.getDescription() != null && r.getDescription().toLowerCase().contains(keyword)) {
            return true;
        }
        // Check cuisine type
        if (r.getCuisineType() != null &&
                r.getCuisineType().getDisplayName().toLowerCase().contains(keyword)) {
            return true;
        }
        // Check city
        if (r.getLocation() != null && r.getLocation().getCity() != null &&
                r.getLocation().getCity().toLowerCase().contains(keyword)) {
            return true;
        }
        // Check address
        if (r.getLocation() != null && r.getLocation().getAddress() != null &&
                r.getLocation().getAddress().toLowerCase().contains(keyword)) {
            return true;
        }
        return false;
    }
}
