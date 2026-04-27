package com.myapp.controller;

import com.myapp.model.Property;
import com.myapp.service.PropertyService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;


/**
 * PropertyController handles all requests related to property listings.
 *
 * Supports UC6, UC9 by providing property lookup and status update.
 */
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController() {
        this.propertyService = new PropertyService();
    }

    /**
     * Retrieve a property by its ID.
     * Used in UC6 to confirm property is open for bidding.
     */
    public String getPropertyById(int propertyId) {
        Property property = propertyService.getPropertyById(propertyId);

        if (property == null) {
            return "ERROR: Property not found.";
        }

        return "Property: " + property.getTitle()
                + " | Type: " + property.getPropertyType()
                + " | Price: " + property.getPrice()
                + " | Status: " + property.getListingStatus()
                + " | Selling Method: " + property.getSellingMethod();
    }

    /**
     * UC9: Update property listing status to "Sold" after winner is declared.
     */
    public String markPropertyAsSold(int propertyId) {
        boolean updated = propertyService.updateListingStatus(propertyId, "Sold");

        if (!updated) {
            return "ERROR: Could not update property status. Please try again.";
        }

        return "SUCCESS: Property " + propertyId + " has been marked as Sold.";
    }

    /**
     * Get all properties that are currently open for bidding.
     */
    public String getPropertiesOpenForBidding() {
        List<Property> properties = propertyService.getPropertiesAvailableForBidding();

        if (properties == null || properties.isEmpty()) {
            return "INFO: No properties currently open for bidding.";
        }

        StringBuilder result = new StringBuilder();
        result.append("Properties Open for Bidding (").append(properties.size()).append("):\n");

        for (Property p : properties) {
            result.append("ID: ").append(p.getPropertyId())
                  .append(" | ").append(p.getTitle())
                  .append(" | Price: ").append(p.getPrice())
                  .append("\n");
        }

        return result.toString();
    }
}