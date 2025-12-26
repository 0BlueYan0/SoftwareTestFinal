package org.example.restaurant.model;

/**
 * Enum representing different types of cuisines.
 */
public enum CuisineType {
    CHINESE("中式料理"),
    JAPANESE("日式料理"),
    KOREAN("韓式料理"),
    ITALIAN("義式料理"),
    FRENCH("法式料理"),
    AMERICAN("美式料理"),
    MEXICAN("墨西哥料理"),
    THAI("泰式料理"),
    VIETNAMESE("越南料理"),
    INDIAN("印度料理"),
    TAIWANESE("台式料理"),
    SEAFOOD("海鮮"),
    VEGETARIAN("素食"),
    FAST_FOOD("速食"),
    CAFE("咖啡廳"),
    DESSERT("甜點"),
    BBQ("燒烤"),
    HOT_POT("火鍋"),
    BUFFET("自助餐"),
    OTHER("其他");

    private final String displayName;

    CuisineType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CuisineType fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        for (CuisineType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return OTHER;
    }
}
