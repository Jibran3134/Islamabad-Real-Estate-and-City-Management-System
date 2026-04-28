package com.myapp.model;

import java.time.LocalDateTime;

/**
 * SECTOR MODEL CLASS
 * OOP Principle: ENCAPSULATION - All fields are private, accessed via getters/setters
 * OOP Principle: ABSTRACTION - Hides internal data representation
 */
public class Sector {

    // Private fields - ENCAPSULATION
    private int sectorId;
    private String sectorName;
    private int capacityLimit;
    private int currentPropertyCount;
    private String status;           // "ACTIVE", "FROZEN", "OVERLOADED"
    private LocalDateTime lastUpdated;

    // CONSTRUCTOR - Creates a new Sector object
    public Sector(int sectorId, String sectorName, int capacityLimit, int currentPropertyCount) {
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.capacityLimit = capacityLimit;
        this.currentPropertyCount = currentPropertyCount;
        this.status = (currentPropertyCount >= capacityLimit) ? "OVERLOADED" : "ACTIVE";
        this.lastUpdated = LocalDateTime.now();
    }

    // GETTERS - Provide controlled access to private fields (ENCAPSULATION)
    public int getSectorId() {
        return sectorId;
    }

    public String getSectorName() {
        return sectorName;
    }

    public int getCapacityLimit() {
        return capacityLimit;
    }

    public int getCurrentPropertyCount() {
        return currentPropertyCount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * SETTER with business logic
     * OOP Principle: ENCAPSULATION - Validation and logic inside setter
     */
    public void setCapacityLimit(int capacityLimit) {
        this.capacityLimit = capacityLimit;
        // Auto-update status based on new capacity
        this.status = (currentPropertyCount >= capacityLimit) ? "OVERLOADED" : "ACTIVE";
        this.lastUpdated = LocalDateTime.now();
    }

    public void setCurrentPropertyCount(int count) {
        this.currentPropertyCount = count;
        // Re-evaluate status when property count changes
        this.status = (currentPropertyCount >= capacityLimit) ? "OVERLOADED" : "ACTIVE";
    }

    // ========== UC2 KE LIYE NAYE METHODS ==========

    /**
     * UC2: Sector ko freeze karne ka method
     * OOP Principle: ENCAPSULATION - Status change logic inside model
     * SD4 step 4.1: updateSectorStatus("FROZEN")
     */
    public void freeze() {
        this.status = "FROZEN";
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * UC2 (future): Sector ko unfreeze karne ka method
     * Re-evaluates status based on current count vs capacity
     */
    public void unfreeze() {
        this.status = (currentPropertyCount >= capacityLimit) ? "OVERLOADED" : "ACTIVE";
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * UC2: Check if sector is frozen
     * INFORMATION EXPERT - Model knows its own state
     */
    public boolean isFrozen() {
        return "FROZEN".equals(this.status);
    }

    /**
     * UC2: Check if sector is overloaded (count >= limit)
     * INFORMATION EXPERT - Model knows its own data
     */
    public boolean isOverloaded() {
        return currentPropertyCount >= capacityLimit;
    }

    @Override
    public String toString() {
        return "Sector{id=" + sectorId + ", name='" + sectorName + "', status=" + status + ", limit=" + capacityLimit + ", count=" + currentPropertyCount + "}";
    }
}