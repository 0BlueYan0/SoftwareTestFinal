package org.example.restaurant.service;

import org.example.restaurant.model.MenuItem;
import org.example.restaurant.model.Restaurant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for analyzing and filtering restaurants by price.
 */
public class PriceAnalyzer {

    public static final int PRICE_LEVEL_CHEAP = 1; // < $200
    public static final int PRICE_LEVEL_MODERATE = 2; // $200 - $500
    public static final int PRICE_LEVEL_EXPENSIVE = 3; // $500 - $1000
    public static final int PRICE_LEVEL_LUXURY = 4; // > $1000

    /**
     * Filter restaurants by price range.
     * v(G) = ~10
     */
    public List<Restaurant> filterByPriceRange(List<Restaurant> restaurants,
            Double minPrice, Double maxPrice) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        // Validate range
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return new ArrayList<>();
        }

        List<Restaurant> result = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            if (restaurant == null) {
                continue;
            }

            double avgPrice = calculateAveragePrice(restaurant);

            // Skip restaurants with no price data
            if (avgPrice <= 0 && restaurant.getAveragePrice() <= 0) {
                continue;
            }

            double price = avgPrice > 0 ? avgPrice : restaurant.getAveragePrice();

            boolean meetsMin = (minPrice == null) || (price >= minPrice);
            boolean meetsMax = (maxPrice == null) || (price <= maxPrice);

            if (meetsMin && meetsMax) {
                result.add(restaurant);
            }
        }

        return result;
    }

    /**
     * Calculate average price from menu items.
     * v(G) = ~8
     */
    public double calculateAveragePrice(Restaurant restaurant) {
        if (restaurant == null) {
            return 0.0;
        }

        List<MenuItem> menu = restaurant.getMenu();
        if (menu == null || menu.isEmpty()) {
            return restaurant.getAveragePrice();
        }

        double sum = 0;
        int count = 0;

        for (MenuItem item : menu) {
            if (item == null) {
                continue;
            }
            if (!item.isAvailable()) {
                continue;
            }
            if (item.getPrice() <= 0) {
                continue;
            }
            sum += item.getPrice();
            count++;
        }

        if (count == 0) {
            return restaurant.getAveragePrice();
        }

        return Math.round((sum / count) * 100.0) / 100.0;
    }

    /**
     * Sort restaurants by price.
     * v(G) = ~6
     */
    public List<Restaurant> sortByPrice(List<Restaurant> restaurants, boolean ascending) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        Comparator<Restaurant> comparator = (r1, r2) -> {
            double price1 = r1 != null ? getEffectivePrice(r1) : 0;
            double price2 = r2 != null ? getEffectivePrice(r2) : 0;
            return Double.compare(price1, price2);
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return restaurants.stream()
                .filter(r -> r != null)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private double getEffectivePrice(Restaurant restaurant) {
        double avgPrice = calculateAveragePrice(restaurant);
        return avgPrice > 0 ? avgPrice : restaurant.getAveragePrice();
    }

    /**
     * Recommend restaurants within budget.
     * v(G) = ~12
     */
    public List<Restaurant> recommendByBudget(List<Restaurant> restaurants, double budget) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (budget <= 0) {
            return new ArrayList<>();
        }

        List<Restaurant> eligible = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            if (restaurant == null) {
                continue;
            }

            double price = getEffectivePrice(restaurant);

            if (price <= 0) {
                // No price data, include with low priority
                continue;
            }

            if (price <= budget) {
                eligible.add(restaurant);
            } else if (price <= budget * 1.1) {
                // Slightly over budget but still reasonable
                eligible.add(restaurant);
            }
        }

        // Sort by value (rating / price ratio)
        RatingService ratingService = new RatingService();

        return eligible.stream()
                .sorted((r1, r2) -> {
                    double value1 = calculateValueScore(r1, ratingService);
                    double value2 = calculateValueScore(r2, ratingService);
                    return Double.compare(value2, value1);
                })
                .collect(Collectors.toList());
    }

    private double calculateValueScore(Restaurant restaurant, RatingService ratingService) {
        double price = getEffectivePrice(restaurant);
        double rating = ratingService.calculateAverageRating(restaurant);

        if (price <= 0) {
            return rating;
        }

        // Value = rating / normalized price
        return (rating * 100) / price;
    }

    /**
     * Categorize restaurant by price level.
     * v(G) = ~8
     */
    public int categorizePriceLevel(Restaurant restaurant) {
        if (restaurant == null) {
            return 0;
        }

        // If price level is already set, return it
        if (restaurant.getPriceLevel() > 0) {
            return restaurant.getPriceLevel();
        }

        double avgPrice = getEffectivePrice(restaurant);

        if (avgPrice <= 0) {
            return 0; // Unknown
        } else if (avgPrice < 200) {
            return PRICE_LEVEL_CHEAP;
        } else if (avgPrice < 500) {
            return PRICE_LEVEL_MODERATE;
        } else if (avgPrice < 1000) {
            return PRICE_LEVEL_EXPENSIVE;
        } else {
            return PRICE_LEVEL_LUXURY;
        }
    }

    /**
     * Filter restaurants by price level.
     * v(G) = ~6
     */
    public List<Restaurant> filterByPriceLevel(List<Restaurant> restaurants, int priceLevel) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }

        if (priceLevel < 1 || priceLevel > 4) {
            return new ArrayList<>(restaurants);
        }

        return restaurants.stream()
                .filter(r -> r != null)
                .filter(r -> categorizePriceLevel(r) == priceLevel)
                .collect(Collectors.toList());
    }

    /**
     * Get price range description.
     * v(G) = ~5
     */
    public String getPriceRangeDescription(Restaurant restaurant) {
        if (restaurant == null) {
            return "未知";
        }

        int level = categorizePriceLevel(restaurant);

        switch (level) {
            case PRICE_LEVEL_CHEAP:
                return "平價 (< $200)";
            case PRICE_LEVEL_MODERATE:
                return "中等 ($200 - $500)";
            case PRICE_LEVEL_EXPENSIVE:
                return "高級 ($500 - $1000)";
            case PRICE_LEVEL_LUXURY:
                return "奢華 (> $1000)";
            default:
                return "未知";
        }
    }

    /**
     * Get price statistics for a list of restaurants.
     * v(G) = ~6
     */
    public PriceStatistics calculatePriceStatistics(List<Restaurant> restaurants) {
        PriceStatistics stats = new PriceStatistics();

        if (restaurants == null || restaurants.isEmpty()) {
            return stats;
        }

        List<Double> prices = new ArrayList<>();

        for (Restaurant r : restaurants) {
            if (r != null) {
                double price = getEffectivePrice(r);
                if (price > 0) {
                    prices.add(price);
                }
            }
        }

        if (prices.isEmpty()) {
            return stats;
        }

        // Sort for median calculation
        prices.sort(Double::compareTo);

        stats.count = prices.size();
        stats.min = prices.get(0);
        stats.max = prices.get(prices.size() - 1);
        stats.average = prices.stream().mapToDouble(d -> d).average().orElse(0.0);
        stats.median = prices.get(prices.size() / 2);

        return stats;
    }

    /**
     * Check if restaurant is affordable (within average + 20%).
     * v(G) = ~5
     */
    public boolean isAffordable(Restaurant restaurant, List<Restaurant> referenceSet) {
        if (restaurant == null) {
            return false;
        }

        PriceStatistics stats = calculatePriceStatistics(referenceSet);
        if (stats.count == 0) {
            return true; // No reference, assume affordable
        }

        double price = getEffectivePrice(restaurant);
        if (price <= 0) {
            return true; // No price data
        }

        double threshold = stats.average * 1.2;
        return price <= threshold;
    }

    /**
     * Price statistics container class.
     */
    public static class PriceStatistics {
        public int count = 0;
        public double min = 0;
        public double max = 0;
        public double average = 0;
        public double median = 0;

        @Override
        public String toString() {
            return String.format("PriceStats{count=%d, min=%.2f, max=%.2f, avg=%.2f, median=%.2f}",
                    count, min, max, average, median);
        }
    }
}
