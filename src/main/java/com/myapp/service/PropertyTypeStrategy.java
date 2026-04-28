package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GoF: STRATEGY PATTERN - Concrete Strategy for property type filtering
 * Filters properties based on Residential, Commercial, or Industrial type
 */
public class PropertyTypeStrategy implements SearchStrategy {
    
    @Override
    public List<Property> filter(List<Property> properties, SearchCriteria criteria) {
        String propertyType = criteria.getPropertyType();
        
        if (propertyType == null || propertyType.trim().isEmpty()) {
            return properties;  // No type filter applied
        }
        
        return properties.stream()
            .filter(property -> property.getPropertyType() != null &&
                    propertyType.equalsIgnoreCase(property.getPropertyType()))
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "Property Type Filter Strategy";
    }
}
