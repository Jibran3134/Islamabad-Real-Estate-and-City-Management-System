package com.myapp.controller;

import com.myapp.model.Property;
import com.myapp.service.PropertyService;
import com.myapp.service.PropertyService.AddPropertyResult;
import com.myapp.service.PropertyValidator;
import com.myapp.service.PropertyValidator.ValidationResult;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * PropertyController handles all requests related to property listings.
 * 
 * GRASP: CONTROLLER PATTERN - Handles all UC3/UC4/UC5 system events
 * GRASP: LOW COUPLING - Only calls service, never database directly
 */
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyValidator validator;      // UC3: Strategy pattern
    private int currentAgentId;

    // ========== EXISTING CONSTRUCTOR (for UC4/5/6/9) ==========
    
    public PropertyController() {
        this.propertyService = new PropertyService();
        this.validator = new PropertyValidator();
        this.currentAgentId = 1;  // Default agent
    }
    
    // UC3 Constructor with agent ID
    public PropertyController(int agentId) {
        this.propertyService = new PropertyService();
        this.validator = new PropertyValidator();
        this.currentAgentId = agentId;
    }

    // ========== EXISTING METHODS (UC4, UC5, UC6, UC9) ==========

    public List<Property> getAvailableProperties() {
        return propertyService.getPropertiesAvailableForBidding();
    }

    public List<Property> handleSearch(String sector, String type) {
        return propertyService.searchProperties(sector, type);
    }

    public String getPropertyById(int propertyId) {
        Property property = propertyService.getPropertyById(propertyId);
        if (property == null) return "ERROR: Property not found.";
        return "Property: " + property.getTitle() + " | Price: " + property.getPrice();
    }

    public String markPropertyAsSold(int propertyId) {
        boolean updated = propertyService.updateListingStatus(propertyId, "Sold");
        return updated ? "SUCCESS" : "ERROR";
    }

    // ========== UC3: ADD PROPERTY LISTING ==========

    /**
     * SD2 step 2: validatePropertyData()
     * Real-time validation before submission
     */
    public ValidationResult validatePropertyData(String title, String location, BigDecimal price, String propertyType, int sectorId) {
        return validator.validate(title, location, price, propertyType, sectorId);
    }

    /**
     * Individual field validations for real-time UI feedback
     */
    public boolean isValidLocation(String location) {
        return validator.isValidLocation(location);
    }

    public boolean isValidPrice(BigDecimal price) {
        return validator.isValidPrice(price);
    }

    public boolean isValidPropertyType(String propertyType) {
        return validator.isValidPropertyType(propertyType);
    }

    /**
     * UC3 Main Method: Add Property Listing
     * GRASP Controller - handles UC3 system event: submitPropertyListing()
     * 
     * Coordinates the entire add listing process:
     *   1. Basic input validation (controller level)
     *   2. Delegates to PropertyService for business logic
     *   3. Returns result to UI
     */
    public AddPropertyResult addPropertyListing(
            String title, String description, String location, BigDecimal price, 
            String propertyType, String sellingMethod,
            int sectorId, List<byte[]> imageFiles, List<String> originalFileNames) throws SQLException {
        
        // Basic input validation (controller level)
        if (title == null || title.trim().isEmpty()) {
            return new AddPropertyResult(false, "Title cannot be empty", 0);
        }
        
        if (location == null || location.trim().isEmpty()) {
            return new AddPropertyResult(false, "Location cannot be empty", 0);
        }
        
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return new AddPropertyResult(false, "Price must be greater than 0", 0);
        }
        
        if (sectorId <= 0) {
            return new AddPropertyResult(false, "Please select a valid sector", 0);
        }
        
        // Delegate to service for business logic
        return propertyService.addPropertyListing(
            title, description, location, price, propertyType, sellingMethod,
            sectorId, currentAgentId, imageFiles, originalFileNames);
    }
}