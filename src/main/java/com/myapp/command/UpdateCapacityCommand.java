package com.myapp.command;

import com.myapp.repository.SectorRepository;
import java.sql.SQLException;

/**
 * GoF DESIGN PATTERN: FACTORY METHOD (used through creator)
 * GRASP: CREATOR - This command encapsulates the update operation
 * OOP Principle: ENCAPSULATION - Bundles sectorId, capacity, and execution logic
 */
public class UpdateCapacityCommand {
    
    private final int sectorId;
    private final int newCapacityLimit;
    private final SectorRepository repository;
    private boolean executed = false;
    private boolean success = false;
    
    // Constructor - Captures all data needed for the command (CREATOR pattern)
    public UpdateCapacityCommand(int sectorId, int newCapacityLimit, SectorRepository repository) {
        this.sectorId = sectorId;
        this.newCapacityLimit = newCapacityLimit;
        this.repository = repository;
    }
    
    /**
     * Execute the command
     * GRASP: LOW COUPLING - Command doesn't know about UI or services
     * Prevents duplicate execution
     */
    public boolean execute() throws SQLException {
        if (executed) {
            return success;  // Already executed, return previous result
        }
        
        success = repository.updateCapacityLimit(sectorId, newCapacityLimit);
        executed = true;
        return success;
    }
    
    // Status check methods
    public boolean wasExecuted() { return executed; }
    public boolean isSuccess() { return success; }
}
