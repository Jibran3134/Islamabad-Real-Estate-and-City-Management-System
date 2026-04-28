package com.myapp.model;

import java.math.BigDecimal;

/**
 * UC4 - SearchCriteria Model Class
 * Holds all filter values for property search
 * OOP Principle: ENCAPSULATION - All fields private with getters/setters
 * 
 * Uses BigDecimal for price (matching existing Property model)
 */
public class SearchCriteria {
    
    private String location;          // e.g., "F-7", "G-10"
    private BigDecimal minPrice;      // minimum price range
    private BigDecimal maxPrice;      // maximum price range
    private String propertyType;      // "Residential", "Commercial", "Industrial"
    private Integer sectorId;         // specific sector ID (optional)
    
    // Default constructor
    public SearchCriteria() {}
    
    // Parameterized constructor
    public SearchCriteria(String location, BigDecimal minPrice, BigDecimal maxPrice, String propertyType, Integer sectorId) {
        this.location = location;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.propertyType = propertyType;
        this.sectorId = sectorId;
    }
    
    // Getters and Setters
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    
    public Integer getSectorId() { return sectorId; }
    public void setSectorId(Integer sectorId) { this.sectorId = sectorId; }
    
    // Check if any filter is applied
    public boolean hasFilters() {
        return (location != null && !location.trim().isEmpty()) ||
               (minPrice != null) ||
               (maxPrice != null) ||
               (propertyType != null && !propertyType.trim().isEmpty()) ||
               (sectorId != null);
    }
    
    @Override
    public String toString() {
        return "SearchCriteria{location='" + location + "', price=" + minPrice + "-" + maxPrice + 
               ", type='" + propertyType + "', sectorId=" + sectorId + "}";
    }
}
