package com.myapp.controller;

import com.myapp.model.Sector;
import com.myapp.service.SectorService;
import com.myapp.service.SectorService.UpdateResult;
import java.sql.SQLException;
import java.util.List;

/**
 * GRASP: CONTROLLER PATTERN
 * Handles all system events for UC1 - Define Sector Capacity Limits
 * Acts as intermediary between UI and Service layer
 * GRASP: LOW COUPLING - Controller only calls service, never database directly
 */
public class SectorController {
    
    private final SectorService sectorService;
    private int currentAuthorityId;  // Logged-in authority user ID
    
    // Constructor - Initializes service and stores authority ID
    public SectorController(int authorityId) {
        this.sectorService = new SectorService();
        this.currentAuthorityId = authorityId;
    }
    
    // UC1 STEP 1 - Get all sectors for display
    public List<Sector> getAllSectors() throws SQLException {
        return sectorService.getAllSectors();
    }
    
    // UC1 STEP 2 - Get specific sector details
    public Sector getSectorDetails(int sectorId) throws SQLException {
        return sectorService.getSectorById(sectorId);
    }
    
    /**
     * UC1 STEP 3 - Main method to define/update sector capacity
     * GRASP: CONTROLLER - Delegates to service for business logic
     * Handles basic input validation before passing to service
     */
    public UpdateResult defineSectorCapacity(int sectorId, int capacityLimit) throws SQLException {
        
        // Basic input validation (controller level)
        if (capacityLimit < 0) {
            return new UpdateResult(false, "Capacity cannot be negative", null);
        }
        
        // Delegate to service for business logic
        return sectorService.updateSectorCapacity(sectorId, capacityLimit, currentAuthorityId);
    }
}
