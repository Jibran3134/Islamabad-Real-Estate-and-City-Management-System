package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GoF: STRATEGY PATTERN - Concrete Strategy for price range filtering
 * Filters properties based on min and max price (uses BigDecimal)
 */
public class PriceRangeStrategy implements SearchStrategy {
    
    @Override
    public List<Property> filter(List<Property> properties, SearchCriteria criteria) {
        // OOP POLYMORPHISM: same SearchStrategy method, different price-range behavior.
        BigDecimal minPrice = criteria.getMinPrice();
        BigDecimal maxPrice = criteria.getMaxPrice();
        
        if (minPrice == null && maxPrice == null) {
            return properties;  // No price filter applied
        }
        
        return properties.stream()
            .filter(property -> {
                BigDecimal price = property.getPrice();
                if (price == null) return false;
                boolean meetsMin = (minPrice == null) || (price.compareTo(minPrice) >= 0);
                boolean meetsMax = (maxPrice == null) || (price.compareTo(maxPrice) <= 0);
                return meetsMin && meetsMax;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "Price Range Filter Strategy";
    }
}
