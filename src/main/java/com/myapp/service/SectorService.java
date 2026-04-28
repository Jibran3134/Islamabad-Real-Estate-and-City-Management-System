package com.myapp.service;

import com.myapp.model.Sector;
import com.myapp.repository.SectorRepository;
import com.myapp.repository.AuditLogRepository;
import com.myapp.command.UpdateCapacityCommand;
import com.myapp.service.SectorCapacityValidator.ValidationResult;
import java.sql.SQLException;
import java.util.List;

/**
 * BUSINESS LOGIC LAYER (SERVICE)
 * GRASP: HIGH COHESION - Only handles sector-related business logic
 * GRASP: LOW COUPLING - Talks only to repositories, never directly to database
 * OOP Principle: ABSTRACTION - Hides implementation details from controllers
 */
public class SectorService {
    
    private final SectorRepository sectorRepository;
    private final AuditLogRepository auditLogRepository;
    private final SectorCapacityValidator validator;
    
    public SectorService() {
        this.sectorRepository = new SectorRepository();
        this.auditLogRepository = new AuditLogRepository();
        this.validator = new SectorCapacityValidator();  // GoF STRATEGY
    }
    
    // Returns all sectors - Pass-through to repository
    public List<Sector> getAllSectors() throws SQLException {
        return sectorRepository.findAllSectors();
    }
    
    // Returns single sector by ID - With input validation
    public Sector getSectorById(int sectorId) throws SQLException {
        if (sectorId <= 0) {
            throw new IllegalArgumentException("Invalid sector ID");
        }
        return sectorRepository.findSectorById(sectorId);
    }
    
    /**
     * Main business logic for UC1 - Define Sector Capacity Limits
     * GRASP: PROTECTED VARIATIONS - Changes to validation won't affect this method
     * 
     * SD4 FIX: After DB update, sector.setCapacityLimit() is called to keep
     *          the Java object in sync with the database (SD4 step 6-7).
     * ALT SCENARIO 4 FIX: handleSystemCrash() wraps the entire operation
     *          and calls restoreLastSavedData() on unexpected failures.
     */
    public UpdateResult updateSectorCapacity(int sectorId, int newCapacity, int authorityId) throws SQLException {
        
        // STEP 1: Verify sector exists (INFORMATION EXPERT delegates to repository)
        Sector sector = sectorRepository.findSectorById(sectorId);
        if (sector == null) {
            return new UpdateResult(false, "Sector not found", null);
        }
        
        // Save old capacity for rollback/logging (ALT SCENARIO 4: restoreLastSavedData)
        int oldCapacity = sector.getCapacityLimit();
        
        // STEP 2: Validate new capacity (GoF STRATEGY pattern in action)
        ValidationResult validation = validator.validate(newCapacity);
        if (!validation.isValid()) {
            return new UpdateResult(false, validation.getErrorMessage(), null);
        }
        
        // STEP 3: Create and execute command (GRASP CREATOR pattern)
        // Wrapped in crash handler (ALT SCENARIO 4)
        try {
            UpdateCapacityCommand command = new UpdateCapacityCommand(sectorId, newCapacity, sectorRepository);
            boolean success = command.execute();
            
            if (success) {
                // SD4 FIX (STEP 6): Update the in-memory Sector object
                // This matches SD4: SectorRepository → Sector.setCapacityLimit()
                sector.setCapacityLimit(newCapacity);
                
                // STEP 5: Log the update (PURE FABRICATION)
                String logMessage = "CAPACITY_UPDATED from " + oldCapacity + " to " + newCapacity;
                auditLogRepository.logUpdate(sectorId, authorityId, logMessage);
                
                return new UpdateResult(true, "Capacity updated successfully", newCapacity);
            } else {
                return new UpdateResult(false, "Database update failed, please try again", null);
            }
            
        } catch (SQLException e) {
            // ALT SCENARIO 4: handleSystemCrash() — catch unexpected DB failures
            return handleSystemCrash(sectorId, oldCapacity, e);
        } catch (Exception e) {
            // ALT SCENARIO 4: Catch any unexpected runtime crash
            return handleSystemCrash(sectorId, oldCapacity, e);
        }
    }
    
    /**
     * ALT SCENARIO 4: handleSystemCrash()
     * Called when an unexpected error occurs during capacity update.
     * Attempts to restore the last known good state from the database.
     * Maps to Extended UC1 Extension 4: "system shows error and restores last saved data"
     */
    private UpdateResult handleSystemCrash(int sectorId, int previousCapacity, Exception e) {
        System.err.println("[CRASH HANDLER] System error during capacity update: " + e.getMessage());
        
        // Attempt to restore last saved data from database
        Sector restoredSector = restoreLastSavedData(sectorId);
        
        if (restoredSector != null) {
            System.err.println("[CRASH HANDLER] Restored sector data from database. " +
                "Current capacity in DB: " + restoredSector.getCapacityLimit());
            return new UpdateResult(false, 
                "System error occurred. Data restored to last saved state (capacity: " + 
                restoredSector.getCapacityLimit() + "). Please try again.", null);
        } else {
            System.err.println("[CRASH HANDLER] Could not restore sector data. Previous capacity was: " + previousCapacity);
            return new UpdateResult(false, 
                "System error occurred. Could not verify data state. " +
                "Previous capacity was: " + previousCapacity + ". Please contact administrator.", null);
        }
    }
    
    /**
     * ALT SCENARIO 4: restoreLastSavedData()
     * Re-fetches sector from database to verify/restore consistent state.
     * Since SectorRepository uses transaction rollback (Alt Scenario 3),
     * the DB state is always consistent — this method confirms it.
     */
    private Sector restoreLastSavedData(int sectorId) {
        try {
            // Re-read from database — this is the "last saved data"
            // Since transactions roll back on failure, DB is always in a valid state
            return sectorRepository.findSectorById(sectorId);
        } catch (SQLException ex) {
            System.err.println("[RESTORE FAILED] Cannot read from database: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * Result wrapper class
     * HIGH COHESION - Groups success status with message and data
     */
    public static class UpdateResult {
        private final boolean success;
        private final String message;
        private final Integer newCapacity;
        
        public UpdateResult(boolean success, String message, Integer newCapacity) {
            this.success = success;
            this.message = message;
            this.newCapacity = newCapacity;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getNewCapacity() { return newCapacity; }
    }
}
