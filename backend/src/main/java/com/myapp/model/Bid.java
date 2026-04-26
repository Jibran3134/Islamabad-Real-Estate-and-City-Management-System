package com.myapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL: Represents a bid placed on a property.
 * Maps to the 'bids' table in the database.
 *
 * Status: active, accepted, rejected, withdrawn
 */
public class Bid {

    private int bidId;
    private int propertyId;
    private int bidderId;
    private BigDecimal bidAmount;
    private String bidStatus;      // active | accepted | rejected | withdrawn
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────

    public Bid() {}

    public Bid(int propertyId, int bidderId, BigDecimal bidAmount) {
        this.propertyId = propertyId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidStatus = "active";
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public int getBidderId() { return bidderId; }
    public void setBidderId(int bidderId) { this.bidderId = bidderId; }

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }

    public String getBidStatus() { return bidStatus; }
    public void setBidStatus(String bidStatus) { this.bidStatus = bidStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Bid{" +
                "bidId=" + bidId +
                ", propertyId=" + propertyId +
                ", bidderId=" + bidderId +
                ", amount=" + bidAmount +
                ", status='" + bidStatus + '\'' +
                '}';
    }
}
