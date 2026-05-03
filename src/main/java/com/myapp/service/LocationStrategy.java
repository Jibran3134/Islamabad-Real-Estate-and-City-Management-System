package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GoF: STRATEGY PATTERN - Concrete Strategy for location filtering
 * Filters properties based on location text (case-insensitive contains)
 */
public class LocationStrategy implements SearchStrategy {
    
    @Override
    public List<Property> filter(List<Property> properties, SearchCriteria criteria) {
        // OOP POLYMORPHISM: this concrete strategy provides location-specific filter behavior.
        String location = criteria.getLocation();
        
        if (location == null || location.trim().isEmpty()) {
            return properties;  // No location filter applied
        }
        
        String searchTerm = location.toLowerCase().trim();
        
        return properties.stream()
            .filter(property -> property.getLocation() != null && 
                    property.getLocation().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "Location Filter Strategy";
    }
}
