package com.myapp.model;

/**
 * MODEL: Represents a sector in Islamabad managed by the City Real Estate Authority.
 * Maps to the 'Sector' table in the database.
 *
 * Used in:
 *   UC1 – Define Sector Capacity Limits
 *   UC2 – Freeze Overloaded Sector
 *   UC3 – Add Property Listing (sector availability check)
 *
 * Status values: Active | Frozen
 */
public class Sector {

    private int sectorId;
    private String sectorName;
    private int capacityLimit;
    private int currentCount;
    private String status;            // Active | Frozen

    // ── Constructors ──────────────────────────────────────────────────

    public Sector() {}

    public Sector(String sectorName, int capacityLimit, int currentCount, String status) {
        this.sectorName = sectorName;
        this.capacityLimit = capacityLimit;
        this.currentCount = currentCount;
        this.status = status;
    }

    // ── Domain Methods ────────────────────────────────────────────────

    /**
     * Checks whether this sector has reached or exceeded its capacity limit.
     * Used in UC2 to determine if freezing is warranted.
     */
    public boolean isOverloaded() {
        return currentCount >= capacityLimit;
    }

    /**
     * Checks whether new property listings can be added to this sector.
     * Used in UC3 to verify sector availability before adding a listing.
     */
    public boolean canAcceptListing() {
        return "Active".equalsIgnoreCase(status) && currentCount < capacityLimit;
    }

    /**
     * Freezes the sector, preventing new property listings.
     * Used in UC2 – Freeze Overloaded Sector.
     */
    public void freeze() {
        this.status = "Frozen";
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getSectorId() { return sectorId; }
    public void setSectorId(int sectorId) { this.sectorId = sectorId; }

    public String getSectorName() { return sectorName; }
    public void setSectorName(String sectorName) { this.sectorName = sectorName; }

    public int getCapacityLimit() { return capacityLimit; }
    public void setCapacityLimit(int capacityLimit) { this.capacityLimit = capacityLimit; }

    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Sector{" +
                "sectorId=" + sectorId +
                ", sectorName='" + sectorName + '\'' +
                ", capacity=" + currentCount + "/" + capacityLimit +
                ", status='" + status + '\'' +
                '}';
    }
}
