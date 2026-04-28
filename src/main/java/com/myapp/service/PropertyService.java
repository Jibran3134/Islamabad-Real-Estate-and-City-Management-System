package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.repository.PropertyRepository;
import java.util.List;

/**
 * PropertyService contains business logic for property-related operations.
 */
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService() {
        this.propertyRepository = new PropertyRepository();
    }

    public Property getPropertyById(int propertyId) {
        return propertyRepository.findById(propertyId);
    }

    public boolean updateListingStatus(int propertyId, String newStatus) {
        return propertyRepository.updateListingStatus(propertyId, newStatus);
    }

    public List<Property> getPropertiesAvailableForBidding() {
        return propertyRepository.findPropertiesOpenForBidding();
    }

    public List<Property> searchProperties(String sector, String type) {
        return propertyRepository.search(sector, type);
    }
}
