package org.example.restaurant.model;

import java.util.Objects;

/**
 * Represents a geographical location with coordinates and address information.
 */
public class Location {
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String district;
    private String postalCode;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(double latitude, double longitude, String address, String city) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public boolean isValid() {
        return latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (postalCode != null && !postalCode.isEmpty()) {
            sb.append(postalCode).append(" ");
        }
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }
        if (district != null && !district.isEmpty()) {
            sb.append(district);
        }
        if (address != null && !address.isEmpty()) {
            sb.append(address);
        }
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Location location = (Location) o;
        return Double.compare(location.latitude, latitude) == 0
                && Double.compare(location.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
