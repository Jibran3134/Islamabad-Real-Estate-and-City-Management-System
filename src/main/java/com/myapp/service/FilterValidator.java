package com.myapp.service;

import com.myapp.model.SearchCriteria;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * UC4 SD1: FilterValidator
 * GRASP: INFORMATION EXPERT - Knows all validation rules for search filters
 * GoF: FACTORY PATTERN - Hides which validator is created
 * GRASP: HIGH COHESION - Only handles filter validation
 */
public class FilterValidator {
    
    private final List<String> validationErrors;
    
    public FilterValidator() {
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * SD1 step: validateSearchParameters(filters)
     * Validates all filter values before search
     */
    public ValidationResult validate(SearchCriteria criteria) {
        validationErrors.clear();
        
        // Validate location (if provided)
        if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
            validateLocation(criteria.getLocation());
        }
        
        // Validate price range
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            validatePriceRange(criteria.getMinPrice(), criteria.getMaxPrice());
        }
        
        // Validate property type (if provided)
        if (criteria.getPropertyType() != null && !criteria.getPropertyType().trim().isEmpty()) {
            validatePropertyType(criteria.getPropertyType());
        }
        
        // Validate sector ID (if provided)
        if (criteria.getSectorId() != null) {
            validateSectorId(criteria.getSectorId());
        }
        
        if (validationErrors.isEmpty()) {
            return new ValidationResult(true, "All filters are valid");
        } else {
            return new ValidationResult(false, String.join("; ", validationErrors));
        }
    }
    
    private void validateLocation(String location) {
        if (location.length() < 2) {
            validationErrors.add("Location must be at least 2 characters");
        }
        if (location.length() > 100) {
            validationErrors.add("Location is too long (max 100 characters)");
        }
    }
    
    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            validationErrors.add("Minimum price cannot be negative");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            validationErrors.add("Maximum price cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            validationErrors.add("Minimum price cannot be greater than maximum price");
        }
    }
    
    private void validatePropertyType(String propertyType) {
        // Matches existing Property model values: Residential | Commercial | Industrial
        if (!propertyType.equalsIgnoreCase("Residential") && 
            !propertyType.equalsIgnoreCase("Commercial") && 
            !propertyType.equalsIgnoreCase("Industrial")) {
            validationErrors.add("Property type must be Residential, Commercial, or Industrial");
        }
    }
    
    private void validateSectorId(Integer sectorId) {
        if (sectorId <= 0) {
            validationErrors.add("Invalid sector ID");
        }
    }
    
    /**
     * Validation Result - HIGH COHESION
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String message;
        
        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
        
        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
    }
}
