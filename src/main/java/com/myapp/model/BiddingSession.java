package com.myapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL: Represents a bidding session for a property listed for auction.
 * Maps to the 'Bidding_Session' table in the database.
 *
 * Used in:
 *   UC6 – Place Bid on Property (current highest bid comparison)
 *   UC8 – Close Bidding Automatically (deadline-based closure)
 *   UC9 – Declare Winning Bidder (winner determination)
 *
 * Status values: Active | Closed
 */
public class BiddingSession {

    private int sessionId;
    private int propertyId;
    private BigDecimal basePrice;
    private LocalDateTime deadline;
    private String status;                  // Active | Closed
    private Integer winnerId;               // nullable — set when winner declared
    private BigDecimal winningBidAmount;     // nullable — final winning amount
    private BigDecimal currentHighestBid;    // nullable — tracks live highest bid

    // ── Constructors ──────────────────────────────────────────────────

    public BiddingSession() {}

    public BiddingSession(int propertyId, BigDecimal basePrice, LocalDateTime deadline) {
        this.propertyId = propertyId;
        this.basePrice = basePrice;
        this.deadline = deadline;
        this.status = "Active";
    }

    // ── Domain Methods ────────────────────────────────────────────────

    /**
     * Checks if the bidding session is currently accepting bids.
     * Used in UC6 pre-condition: bidding session must be active.
     */
    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    /**
     * Checks if the bidding deadline has passed.
     * Used in UC8 – Close Bidding Automatically.
     */
    public boolean isDeadlinePassed() {
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Validates that a new bid exceeds the current highest bid (or base price if no bids yet).
     * Used in UC6 – Place Bid on Property.
     */
    public boolean isBidHighEnough(BigDecimal newBidAmount) {
        BigDecimal threshold = (currentHighestBid != null) ? currentHighestBid : basePrice;
        return newBidAmount.compareTo(threshold) > 0;
    }

    /**
     * Closes the bidding session and records the winner.
     * Used in UC8 (close) and UC9 (declare winner).
     */
    public void closeBidding(Integer winnerId, BigDecimal winningAmount) {
        this.status = "Closed";
        this.winnerId = winnerId;
        this.winningBidAmount = winningAmount;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getWinnerId() { return winnerId; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

    public BigDecimal getWinningBidAmount() { return winningBidAmount; }
    public void setWinningBidAmount(BigDecimal winningBidAmount) { this.winningBidAmount = winningBidAmount; }

    public BigDecimal getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(BigDecimal currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    @Override
    public String toString() {
        return "BiddingSession{" +
                "sessionId=" + sessionId +
                ", propertyId=" + propertyId +
                ", basePrice=" + basePrice +
                ", deadline=" + deadline +
                ", status='" + status + '\'' +
                ", winnerId=" + winnerId +
                ", currentHighestBid=" + currentHighestBid +
                '}';
    }
}
