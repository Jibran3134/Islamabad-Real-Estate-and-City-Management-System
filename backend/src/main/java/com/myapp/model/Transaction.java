package com.myapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL: Represents a completed property transaction.
 * Maps to the 'transactions' table in the database.
 *
 * Status: pending, completed, cancelled, refunded
 */
public class Transaction {

    private int transactionId;
    private int propertyId;
    private int buyerId;
    private int sellerId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String paymentMethod;
    private String status;         // pending | completed | cancelled | refunded

    // ── Constructors ──────────────────────────────────────────────────

    public Transaction() {}

    public Transaction(int propertyId, int buyerId, int sellerId, BigDecimal amount, String paymentMethod) {
        this.propertyId = propertyId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = "pending";
        this.transactionDate = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", propertyId=" + propertyId +
                ", buyerId=" + buyerId +
                ", sellerId=" + sellerId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}
