package com.myapp.service;

import java.util.function.Predicate;

/**
 * GoF DESIGN PATTERN: STRATEGY
 * Different validation rules can be swapped at runtime without changing calling code
 * OOP Principle: POLYMORPHISM - Strategy can be changed dynamically
 */
public class SectorCapacityValidator {
    
    // Predicate is a functional interface - Strategy pattern implementation
    private Predicate<Integer> validationStrategy;
    
    // Default validation strategy - Capacity must be between 1 and 10,000
    public SectorCapacityValidator() {
        this.validationStrategy = capacity -> capacity > 0 && capacity <= 10000;
    }
    
    /**
     * STRATEGY PATTERN - Change validation rules at runtime
     * OOP Principle: POLYMORPHISM - Different strategies can be plugged in
     */
    public void setValidationStrategy(Predicate<Integer> strategy) {
        this.validationStrategy = strategy;
    }
    
    /**
     * Validates capacity limit using current strategy
     * INFORMATION EXPERT - This class owns validation rules
     */
    public ValidationResult validate(int capacityLimit) {
        // Business validation rules
        if (capacityLimit <= 0) {
            return new ValidationResult(false, "Capacity limit must be greater than 0");
        }
        if (capacityLimit > 10000) {
            return new ValidationResult(false, "Capacity limit cannot exceed 10,000 properties");
        }
        if (!validationStrategy.test(capacityLimit)) {
            return new ValidationResult(false, "Capacity limit does not meet system requirements");
        }
        return new ValidationResult(true, "Valid capacity limit");
    }
    
    /**
     * Inner class for validation result (HIGH COHESION)
     * Bundles validation status with error message
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
