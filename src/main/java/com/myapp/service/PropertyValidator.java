package com.myapp.service;

import java.math.BigDecimal;
import java.util.function.Predicate;

/**
 * GoF DESIGN PATTERN: STRATEGY - Different validation rules can be swapped
 * GRASP: INFORMATION EXPERT - This class owns all validation rules
 * GRASP: HIGH COHESION - Only handles property validation
 * 
 * UC3 SD2: validate(location, price, type, sector)
 */
public class PropertyValidator {
    
    // Strategy pattern - validation rules can be changed at runtime
    private Predicate<String> locationValidator;
    private Predicate<BigDecimal> priceValidator;
    private Predicate<String> typeValidator;
    
    public PropertyValidator() {
        // Default validation strategies
        this.locationValidator = location -> location != null && !location.trim().isEmpty() && location.length() >= 3;
        this.priceValidator = price -> price != null && price.compareTo(BigDecimal.ZERO) > 0 && price.compareTo(new BigDecimal("1000000000")) < 0;
        this.typeValidator = type -> type != null && (type.equals("Residential") || type.equals("Commercial") || type.equals("Industrial"));
    }
    
    /**
     * STRATEGY PATTERN - Change validation rules at runtime
     */
    public void setLocationValidator(Predicate<String> validator) {
        this.locationValidator = validator;
    }
    
    public void setPriceValidator(Predicate<BigDecimal> validator) {
        this.priceValidator = validator;
    }
    
    /**
     * SD2 step 3: validate(location, price, type, sector)
     * INFORMATION EXPERT - Validates all property fields
     */
    public ValidationResult validate(String title, String location, BigDecimal price, String propertyType, int sectorId) {
        
        // Title validation
        if (title == null || title.trim().isEmpty()) {
            return new ValidationResult(false, "Title cannot be empty");
        }
        
        // Location validation
        if (!locationValidator.test(location)) {
            return new ValidationResult(false, "Location must be at least 3 characters long");
        }
        
        // Price validation
        if (!priceValidator.test(price)) {
            return new ValidationResult(false, "Price must be between 1 and 1,000,000,000");
        }
        
        // Property type validation
        if (!typeValidator.test(propertyType)) {
            return new ValidationResult(false, "Property type must be Residential, Commercial, or Industrial");
        }
        
        // Sector ID validation
        if (sectorId <= 0) {
            return new ValidationResult(false, "Invalid sector selected");
        }
        
        return new ValidationResult(true, "All fields are valid");
    }
    
    /**
     * SD2 step 4: checkFieldFormats()
     * Individual field validations for real-time feedback
     */
    public boolean isValidLocation(String location) {
        return locationValidator.test(location);
    }
    
    public boolean isValidPrice(BigDecimal price) {
        return priceValidator.test(price);
    }
    
    public boolean isValidPropertyType(String propertyType) {
        return typeValidator.test(propertyType);
    }
    
    /**
     * Inner class for validation result - HIGH COHESION
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() { return isValid; }
        public String getErrorMessage() { return errorMessage; }
    }
}
