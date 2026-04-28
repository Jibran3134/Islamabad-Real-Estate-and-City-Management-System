package com.myapp.controller;

import com.myapp.model.Property;
import com.myapp.service.PropertyService;
import java.util.List;

/**
 * PropertyController handles all requests related to property listings.
 */
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController() {
        this.propertyService = new PropertyService();
    }

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
}