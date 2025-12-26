package org.example.restaurant.model;

import java.util.Objects;

/**
 * Represents a menu item in a restaurant.
 */
public class MenuItem {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private boolean vegetarian;
    private boolean vegan;
    private boolean glutenFree;
    private boolean spicy;
    private boolean available;
    private int calories;

    public MenuItem() {
        this.available = true;
    }

    public MenuItem(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.available = true;
    }

    public MenuItem(String id, String name, double price, String category) {
        this(id, name, price);
        this.category = category;
    }

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public boolean isVegan() {
        return vegan;
    }

    public void setVegan(boolean vegan) {
        this.vegan = vegan;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public void setGlutenFree(boolean glutenFree) {
        this.glutenFree = glutenFree;
    }

    public boolean isSpicy() {
        return spicy;
    }

    public void setSpicy(boolean spicy) {
        this.spicy = spicy;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public boolean matchesDietaryRestrictions(boolean requireVegetarian, boolean requireVegan,
            boolean requireGlutenFree) {
        if (requireVegetarian && !vegetarian) {
            return false;
        }
        if (requireVegan && !vegan) {
            return false;
        }
        if (requireGlutenFree && !glutenFree) {
            return false;
        }
        return true;
    }

    public boolean isInPriceRange(double minPrice, double maxPrice) {
        if (minPrice < 0 || maxPrice < 0) {
            return false;
        }
        if (minPrice > maxPrice) {
            return false;
        }
        return price >= minPrice && price <= maxPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                '}';
    }
}
