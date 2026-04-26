package com.myapp.controller;

import com.myapp.model.Property;
import com.myapp.service.PropertyService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * CONTROLLER: Handles property-related requests from the View layer.
 * Coordinates between the frontend (View) and the PropertyService (Model).
 *
 * In MVC, the Controller:
 *   1. Receives user input from the View (e.g., form data for a new listing)
 *   2. Calls the appropriate Service/Model methods
 *   3. Returns the result to the View for display
 */
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController() throws SQLException {
        this.propertyService = new PropertyService();
    }

    // ── Create Property ───────────────────────────────────────────────

    /**
     * Handles new property listing requests from agents.
     * @return success or error message
     */
    public String handleCreateProperty(String title, String description, String propertyType,
                                       String sector, String address, double areaSqft,
                                       String priceStr, int bedrooms, int bathrooms,
                                       int listedBy) {
        try {
            BigDecimal price = new BigDecimal(priceStr);
            int propertyId = propertyService.createProperty(title, description, propertyType,
                    sector, address, areaSqft, price, bedrooms, bathrooms, listedBy);

            if (propertyId > 0) {
                return "SUCCESS: Property listed successfully! ID: " + propertyId;
            } else {
                return "ERROR: Failed to create listing.";
            }
        } catch (NumberFormatException e) {
            return "ERROR: Invalid price format.";
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }

    // ── View Properties ───────────────────────────────────────────────

    /**
     * Gets all available properties for the public listings page (View).
     */
    public List<Property> getAvailableProperties() {
        try {
            return propertyService.getAvailableProperties();
        } catch (SQLException e) {
            System.err.println("Error fetching properties: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Gets all properties (for admin dashboard).
     */
    public List<Property> getAllProperties() {
        try {
            return propertyService.getAllProperties();
        } catch (SQLException e) {
            System.err.println("Error fetching all properties: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Gets a single property's details (for the property-details View).
     */
    public Property getPropertyDetails(int propertyId) {
        try {
            return propertyService.getPropertyById(propertyId);
        } catch (SQLException e) {
            System.err.println("Error fetching property details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets all properties listed by a specific agent (for agent dashboard).
     */
    public List<Property> getAgentProperties(int agentId) {
        try {
            return propertyService.getPropertiesByAgent(agentId);
        } catch (SQLException e) {
            System.err.println("Error fetching agent properties: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Searches properties by sector and/or property type.
     */
    public List<Property> handleSearch(String sector, String propertyType) {
        try {
            return propertyService.searchProperties(sector, propertyType);
        } catch (SQLException e) {
            System.err.println("Error searching properties: " + e.getMessage());
            return List.of();
        }
    }

    // ── Update Property ───────────────────────────────────────────────

    /**
     * Handles property update requests.
     */
    public String handleUpdateProperty(Property property) {
        try {
            boolean success = propertyService.updateProperty(property);
            return success ? "SUCCESS: Property updated." : "ERROR: Property not found.";
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }

    /**
     * Puts a property up for auction.
     */
    public String handlePutOnAuction(int propertyId) {
        try {
            boolean success = propertyService.putOnAuction(propertyId);
            return success ? "SUCCESS: Property is now on auction." : "ERROR: Property not found.";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // ── Delete Property ───────────────────────────────────────────────

    /**
     * Handles property deletion requests.
     */
    public String handleDeleteProperty(int propertyId) {
        try {
            boolean success = propertyService.deleteProperty(propertyId);
            return success ? "SUCCESS: Property deleted." : "ERROR: Property not found.";
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }
}
