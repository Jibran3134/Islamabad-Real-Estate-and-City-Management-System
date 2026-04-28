package com.myapp.controller;

import com.myapp.model.Sector;
import com.myapp.service.SectorService;
import com.myapp.service.SectorService.UpdateResult;
import com.myapp.service.SectorService.FreezeResult;
import com.myapp.repository.SectorRepository.SectorStatistics;
import java.sql.SQLException;
import java.util.List;

/**
 * GRASP: CONTROLLER PATTERN
 * Handles all system events for UC1 and UC2
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
    
    // ========== UC1 METHODS ==========
    
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
    
    // ========== UC2 METHODS ==========
    
    /**
     * UC2 SD1: Get sector statistics for dashboard
     * GRASP Controller - handles UC2 system event: openSectorDashboard()
     */
    public List<SectorStatistics> getSectorStatistics() throws SQLException {
        return sectorService.getSectorStatistics();
    }
    
    /**
     * UC2 Main: Freeze overloaded sector
     * GRASP Controller - handles UC2 system event: freezeSector()
     * 
     * @param sectorId - ID of the sector to freeze
     * @param overrideWarning - true to freeze even if sector is not overloaded
     * @return FreezeResult with success/failure status and message
     */
    public FreezeResult freezeSector(int sectorId, boolean overrideWarning) throws SQLException {
        // Basic validation at controller level
        if (sectorId <= 0) {
            return new FreezeResult(false, "Invalid sector ID", false);
        }
        
        // Delegate to service for business logic
        return sectorService.freezeSector(sectorId, currentAuthorityId, overrideWarning);
    }
    
    /**
     * UC2: Check if sector is overloaded (utility method)
     * Used by UI to show overload status before freeze
     */
    public boolean isSectorOverloaded(int sectorId) throws SQLException {
        Sector sector = sectorService.getSectorById(sectorId);
        return sector != null && sector.isOverloaded();
    }
}
