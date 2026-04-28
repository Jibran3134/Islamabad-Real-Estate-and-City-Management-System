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
     */
    public UpdateResult updateSectorCapacity(int sectorId, int newCapacity, int authorityId) throws SQLException {
        
        // STEP 1: Verify sector exists (INFORMATION EXPERT delegates to repository)
        Sector sector = sectorRepository.findSectorById(sectorId);
        if (sector == null) {
            return new UpdateResult(false, "Sector not found", null);
        }
        
        // STEP 2: Validate new capacity (GoF STRATEGY pattern in action)
        ValidationResult validation = validator.validate(newCapacity);
        if (!validation.isValid()) {
            return new UpdateResult(false, validation.getErrorMessage(), null);
        }
        
        // STEP 3: Create and execute command (GRASP CREATOR pattern)
        UpdateCapacityCommand command = new UpdateCapacityCommand(sectorId, newCapacity, sectorRepository);
        boolean success = command.execute();
        
        // STEP 4: Log the update if successful (PURE FABRICATION)
        if (success) {
            String logMessage = "CAPACITY_UPDATED from " + sector.getCapacityLimit() + " to " + newCapacity;
            auditLogRepository.logUpdate(sectorId, authorityId, logMessage);
            return new UpdateResult(true, "Capacity updated successfully", newCapacity);
        } else {
            return new UpdateResult(false, "Database update failed, please try again", null);
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
