package com.myapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL: Represents a single bid placed by a buyer during a bidding session.
 * Maps to the 'Bid' table in the database.
 *
 * Used in:
 *   UC6 – Place Bid on Property
 *   UC9 – Declare Winning Bidder
 */
public class Bid {

    private int bidId;
    private int sessionId;             // FK → Bidding_Session.session_id
    private int buyerId;               // FK → Users.user_id (buyer role)
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;

    // ── Constructors ──────────────────────────────────────────────────

    public Bid() {}

    public Bid(int sessionId, int buyerId, BigDecimal bidAmount) {
        this.sessionId = sessionId;
        this.buyerId = buyerId;
        this.bidAmount = bidAmount;
        this.bidTime = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    @Override
    public String toString() {
        return "Bid{" +
                "bidId=" + bidId +
                ", sessionId=" + sessionId +
                ", buyerId=" + buyerId +
                ", amount=" + bidAmount +
                ", bidTime=" + bidTime +
                '}';
    }
}
