package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GoF: STRATEGY PATTERN - Concrete Strategy for sector filtering
 * Filters properties based on specific sector ID
 */
public class SectorStrategy implements SearchStrategy {
    
    @Override
    public List<Property> filter(List<Property> properties, SearchCriteria criteria) {
        // OOP POLYMORPHISM: same SearchStrategy contract, sector-specific filtering.
        Integer sectorId = criteria.getSectorId();
        
        if (sectorId == null) {
            return properties;  // No sector filter applied
        }
        
        return properties.stream()
            .filter(property -> property.getSectorId() == sectorId)
            .collect(Collectors.toList());
    }
    
    @Override
    public String getStrategyName() {
        return "Sector Filter Strategy";
    }
}
