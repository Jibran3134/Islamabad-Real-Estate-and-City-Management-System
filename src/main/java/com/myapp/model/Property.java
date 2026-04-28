package com.myapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL: Represents a property listing in the system.
 * Maps to the 'Property' table in the database.
 *
 * Used in:
 *   UC3 – Add Property Listing
 *   UC4 – Search Property Based on Filters
 *   UC5 – Modify Property Details
 *
 * Property types: Residential | Commercial | Industrial
 * Listing status: For Sale | Sold | Pending
 * Selling method: Fixed Price | Bidding
 */
public class Property {

    private int propertyId;
    private int sectorId;              // FK → Sector.sector_id
    private int agentId;               // FK → Users.user_id (agent role)
    private String title;
    private String description;
    private BigDecimal price;
    private String propertyType;       // Residential | Commercial | Industrial
    private String location;           // street/area location within the sector
    private String listingStatus;      // For Sale | Sold | Pending
    private String sellingMethod;      // Fixed Price | Bidding
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────

    public Property() {}

    public Property(int sectorId, int agentId, String title, String description,
                    BigDecimal price, String propertyType, String location,
                    String listingStatus, String sellingMethod) {
        this.sectorId = sectorId;
        this.agentId = agentId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.propertyType = propertyType;
        this.location = location;
        this.listingStatus = listingStatus;
        this.sellingMethod = sellingMethod;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public int getSectorId() { return sectorId; }
    public void setSectorId(int sectorId) { this.sectorId = sectorId; }

    public int getAgentId() { return agentId; }
    public void setAgentId(int agentId) { this.agentId = agentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getListingStatus() { return listingStatus; }
    public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }

    public String getSellingMethod() { return sellingMethod; }
    public void setSellingMethod(String sellingMethod) { this.sellingMethod = sellingMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Property{" +
                "propertyId=" + propertyId +
                ", sectorId=" + sectorId +
                ", title='" + title + '\'' +
                ", type='" + propertyType + '\'' +
                ", price=" + price +
                ", listingStatus='" + listingStatus + '\'' +
                ", sellingMethod='" + sellingMethod + '\'' +
                '}';
    }
}
