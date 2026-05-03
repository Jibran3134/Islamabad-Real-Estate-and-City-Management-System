package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.repository.PropertyRepository;
import com.myapp.repository.SectorRepository;
import com.myapp.repository.AuditLogRepository;
import com.myapp.service.PropertyValidator.ValidationResult;
import com.myapp.adapter.MediaStorageAdapter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * PropertyService contains business logic for property-related operations.
 * 
 * GRASP: HIGH COHESION - Only handles property-related business logic
 * GRASP: LOW COUPLING - Talks only to repositories, never directly to database
 * 
 * Handles: UC3 (Add Property Listing), UC4 (Search), UC5 (Modify), UC9 (Mark Sold)
 */
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final SectorRepository sectorRepository;           // UC3: sector availability check
    private final AuditLogRepository auditLogRepository;       // UC3: audit logging
    private final PropertyValidator validator;                  // UC3: GoF Strategy
    private final MediaStorageAdapter storageAdapter;           // UC3: GoF Adapter

    public PropertyService() {
        this.propertyRepository = new PropertyRepository();
        this.sectorRepository = new SectorRepository();
        this.auditLogRepository = new AuditLogRepository();
        this.validator = new PropertyValidator();               // GoF STRATEGY
        this.storageAdapter = new MediaStorageAdapter();        // GoF ADAPTER
    }

    // ========== EXISTING METHODS (UC4, UC5, UC6, UC9) ==========

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

    // ========== UC3: ADD PROPERTY LISTING ==========

    /**
     * UC3 Main Method: Add new property listing
     * 
     * SD Flow:
     *   SD1: Agent opens add listing form
     *   SD2: System validates property data → PropertyValidator.validate()
     *   SD3: System uploads images → MediaStorageAdapter.storeImages()
     *   SD4: System saves to database → PropertyRepository.savePropertyWithTransaction()
     *   SD5: System logs action → AuditLogRepository.logUpdate()
     * 
     * Extensions:
     *   1. Invalid data → validation error
     *   2. Sector frozen → reject listing
     *   3. Image upload fails → reject listing
     *   4. DB failure → rollback + crash handler
     * 
     * @param title Property title
     * @param description Property description
     * @param location Property location
     * @param price Property price (BigDecimal for accuracy)
     * @param propertyType Residential/Commercial/Industrial
     * @param sellingMethod Fixed Price/Bidding
     * @param sectorId Target sector ID
     * @param agentId Agent creating the listing
     * @param imageFiles Raw image data (can be null)
     * @param originalFileNames Original file names for images (can be null)
     * @return AddPropertyResult with success/failure and message
     */
    public AddPropertyResult addPropertyListing(
            String title, String description, String location, BigDecimal price, 
            String propertyType, String sellingMethod,
            int sectorId, int agentId, 
            List<byte[]> imageFiles, List<String> originalFileNames) throws SQLException {
        
        // Step 1: Validate property data (SD2 - Strategy Pattern)
        ValidationResult validation = validator.validate(title, location, price, propertyType, sectorId);
        if (!validation.isValid()) {
            return new AddPropertyResult(false, validation.getErrorMessage(), 0);
        }
        
        // Step 2: Check if sector is available for listing (SD4 step 3 - not frozen)
        boolean sectorAvailable = sectorRepository.isSectorAvailableForListing(sectorId);
        if (!sectorAvailable) {
            // UC3 Alternative Scenario: Sector is frozen (links to UC2)
            return new AddPropertyResult(false, "Cannot add listing: Sector is FROZEN. Contact Authority.", 0);
        }

        // Step 3: Upload images (SD3 - Adapter Pattern)
        List<String> storedImagePaths = storageAdapter.storeImages(imageFiles, originalFileNames);
        if (imageFiles != null && !imageFiles.isEmpty() && storedImagePaths.isEmpty()) {
            // UC3 Alternative Scenario: Image upload fails
            return new AddPropertyResult(false, "Image upload failed. Please check file formats (PNG/JPG) and size (<10MB).", 0);
        }
        
        // Step 4: Create property object using existing model (GRASP Creator)
        Property property = new Property(sectorId, agentId, title, description, price, 
                                         propertyType, location, "For Sale", sellingMethod);
        
        // Step 5: Save to database with transaction (SD4)
        // Wrapped in crash handler (Alt Scenario 4)
        try {
            int savedPropertyId = propertyRepository.savePropertyWithTransaction(property, storedImagePaths);
            
            if (savedPropertyId > 0) {
                // Step 6: Log the listing creation (Pure Fabrication)
                // Non-blocking — property save succeeds even if logging fails
                try {
                    auditLogRepository.logUpdate(sectorId, agentId, 
                        "PROPERTY_LISTING_CREATED - Property ID: " + savedPropertyId + 
                        ", Title: " + title + ", Price: " + price);
                } catch (Exception logEx) {
                    System.err.println("[AUDIT] Logging failed but property was saved: " + logEx.getMessage());
                }
                
                return new AddPropertyResult(true, 
                    "Property listing added successfully! Property ID: " + savedPropertyId, savedPropertyId);
            } else {
                return new AddPropertyResult(false, "Failed to save property to database", 0);
            }
            
        } catch (SQLException e) {
            // Alt Scenario 4: handleSystemCrash()
            System.err.println("[CRASH HANDLER] System error during property save: " + e.getMessage());
            return new AddPropertyResult(false, 
                "System error occurred. Transaction rolled back. Please try again. Error: " + e.getMessage(), 0);
        }
    }
    
    /**
     * UC3 Result wrapper class - HIGH COHESION
     */
    public static class AddPropertyResult {
        private final boolean success;
        private final String message;
        private final int propertyId;
        
        public AddPropertyResult(boolean success, String message, int propertyId) {
            this.success = success;
            this.message = message;
            this.propertyId = propertyId;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getPropertyId() { return propertyId; }
    }
}
