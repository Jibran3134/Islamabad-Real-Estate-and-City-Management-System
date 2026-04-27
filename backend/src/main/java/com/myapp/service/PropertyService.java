package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.repository.PropertyRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;


/**
 * PropertyService contains business logic for property-related operations.
 *
 * Supports UC6 (property open for bidding check) and UC9 (mark as Sold).
 */
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService() {
        this.propertyRepository = new PropertyRepository();
    }

    /**
     * Retrieve a property by its ID.
     */
    public Property getPropertyById(int propertyId) {
        return propertyRepository.findById(propertyId);
    }

    /**
     * UC9: Update the property listing status.
     * Called with "Sold" after a winner is declared.
     */
    public boolean updateListingStatus(int propertyId, String newStatus) {
        return propertyRepository.updateListingStatus(propertyId, newStatus);
    }

    /**
     * UC6: Get all properties that use the Bidding selling method
     * and are currently listed For Sale.
     */
    public List<Property> getPropertiesAvailableForBidding() {
        return propertyRepository.findPropertiesOpenForBidding();
    }
}
