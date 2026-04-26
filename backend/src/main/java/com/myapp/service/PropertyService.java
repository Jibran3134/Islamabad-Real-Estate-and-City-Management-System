package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.repository.PropertyRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * SERVICE (Model Layer - Business Logic): Handles property-related business rules.
 * Validates data and enforces listing policies before interacting with the repository.
 */
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService() throws SQLException {
        this.propertyRepository = new PropertyRepository();
    }

    // ── Create Listing ────────────────────────────────────────────────

    /**
     * Creates a new property listing after validation.
     * @return the new property ID
     * @throws IllegalArgumentException if validation fails
     */
    public int createProperty(String title, String description, String propertyType,
                              String sector, String address, double areaSqft,
                              BigDecimal price, int bedrooms, int bathrooms,
                              int listedBy) throws SQLException {

        // Validate required fields
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Property title is required.");
        }
        if (!isValidPropertyType(propertyType)) {
            throw new IllegalArgumentException("Invalid property type. Must be: house, apartment, plot, commercial, or farmhouse.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (areaSqft <= 0) {
            throw new IllegalArgumentException("Area must be greater than zero.");
        }

        Property property = new Property(title, description, propertyType, sector,
                address, areaSqft, price, bedrooms, bathrooms, listedBy);
        return propertyRepository.create(property);
    }

    // ── Read Listings ─────────────────────────────────────────────────

    /**
     * Gets a property by its ID.
     */
    public Property getPropertyById(int propertyId) throws SQLException {
        return propertyRepository.findById(propertyId);
    }

    /**
     * Gets all properties (for admin dashboard).
     */
    public List<Property> getAllProperties() throws SQLException {
        return propertyRepository.findAll();
    }

    /**
     * Gets all available properties (for public listings page).
     */
    public List<Property> getAvailableProperties() throws SQLException {
        return propertyRepository.findAvailable();
    }

    /**
     * Gets all properties listed by a specific agent.
     */
    public List<Property> getPropertiesByAgent(int agentId) throws SQLException {
        return propertyRepository.findByListedBy(agentId);
    }

    /**
     * Searches properties by sector and/or type.
     */
    public List<Property> searchProperties(String sector, String propertyType) throws SQLException {
        return propertyRepository.search(sector, propertyType);
    }

    // ── Update Listing ────────────────────────────────────────────────

    /**
     * Updates a property listing.
     */
    public boolean updateProperty(Property property) throws SQLException {
        if (property.getPrice() != null && property.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        return propertyRepository.update(property);
    }

    /**
     * Marks a property as sold.
     */
    public boolean markAsSold(int propertyId) throws SQLException {
        Property property = propertyRepository.findById(propertyId);
        if (property != null) {
            property.setStatus("sold");
            return propertyRepository.update(property);
        }
        return false;
    }

    /**
     * Puts a property up for auction.
     */
    public boolean putOnAuction(int propertyId) throws SQLException {
        Property property = propertyRepository.findById(propertyId);
        if (property != null) {
            property.setStatus("auction");
            return propertyRepository.update(property);
        }
        return false;
    }

    // ── Delete Listing ────────────────────────────────────────────────

    /**
     * Deletes a property listing.
     */
    public boolean deleteProperty(int propertyId) throws SQLException {
        return propertyRepository.delete(propertyId);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private boolean isValidPropertyType(String type) {
        return type != null && (type.equals("house") || type.equals("apartment") ||
                type.equals("plot") || type.equals("commercial") || type.equals("farmhouse"));
    }
}
