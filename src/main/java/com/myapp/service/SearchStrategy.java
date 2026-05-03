package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;
import java.util.List;

/**
 * GoF DESIGN PATTERN: STRATEGY - Interface for all search strategies
 * OOP Principle: POLYMORPHISM - Different strategies can be swapped at runtime
 * OOP Principle: ABSTRACTION - Hides filter implementation details
 *
 * OOP ABSTRACTION + POLYMORPHISM:
 * Controllers/services depend on this interface instead of concrete filters.
 * Each implementation can be used through the SearchStrategy type while
 * executing its own filter behavior.
 */
public interface SearchStrategy {
    
    /**
     * Filters properties based on specific strategy
     * @param properties List of properties to filter
     * @param criteria SearchCriteria containing filter values
     * @return Filtered list of properties
     */
    List<Property> filter(List<Property> properties, SearchCriteria criteria);
    
    /**
     * Returns the name of the strategy (for logging/debugging)
     */
    String getStrategyName();
}
