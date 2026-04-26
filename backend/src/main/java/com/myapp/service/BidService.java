package com.myapp.service;

import com.myapp.model.Bid;
import com.myapp.model.Property;
import com.myapp.repository.BidRepository;
import com.myapp.repository.PropertyRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * SERVICE (Model Layer - Business Logic): Handles bidding business rules.
 * Validates bid amounts, enforces auction rules, and manages bid lifecycle.
 */
public class BidService {

    private final BidRepository bidRepository;
    private final PropertyRepository propertyRepository;

    public BidService() throws SQLException {
        this.bidRepository = new BidRepository();
        this.propertyRepository = new PropertyRepository();
    }

    // ── Place a Bid ───────────────────────────────────────────────────

    /**
     * Places a new bid on a property after validation.
     * @return the new bid ID
     * @throws IllegalArgumentException if the bid is invalid
     */
    public int placeBid(int propertyId, int bidderId, BigDecimal bidAmount) throws SQLException {

        // Validate bid amount
        if (bidAmount == null || bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bid amount must be greater than zero.");
        }

        // Verify property exists and is on auction
        Property property = propertyRepository.findById(propertyId);
        if (property == null) {
            throw new IllegalArgumentException("Property not found.");
        }
        if (!"auction".equals(property.getStatus())) {
            throw new IllegalArgumentException("This property is not currently on auction.");
        }

        // Ensure bid is higher than the current highest bid
        Bid highestBid = bidRepository.findHighestBid(propertyId);
        if (highestBid != null && bidAmount.compareTo(highestBid.getBidAmount()) <= 0) {
            throw new IllegalArgumentException("Your bid must be higher than the current highest bid of " +
                    highestBid.getBidAmount());
        }

        // Ensure bid is at least equal to the property's base price
        if (bidAmount.compareTo(property.getPrice()) < 0) {
            throw new IllegalArgumentException("Bid must be at least the base price of " + property.getPrice());
        }

        Bid bid = new Bid(propertyId, bidderId, bidAmount);
        return bidRepository.create(bid);
    }

    // ── Read Bids ─────────────────────────────────────────────────────

    /**
     * Gets all bids for a specific property (sorted by amount desc).
     */
    public List<Bid> getBidsForProperty(int propertyId) throws SQLException {
        return bidRepository.findByPropertyId(propertyId);
    }

    /**
     * Gets all bids placed by a specific user.
     */
    public List<Bid> getBidsByUser(int bidderId) throws SQLException {
        return bidRepository.findByBidderId(bidderId);
    }

    /**
     * Gets the current highest bid for a property.
     */
    public Bid getHighestBid(int propertyId) throws SQLException {
        return bidRepository.findHighestBid(propertyId);
    }

    // ── Manage Bids ───────────────────────────────────────────────────

    /**
     * Accepts a bid (and rejects all other bids for the same property).
     */
    public boolean acceptBid(int bidId) throws SQLException {
        Bid bid = bidRepository.findById(bidId);
        if (bid == null) {
            throw new IllegalArgumentException("Bid not found.");
        }

        // Accept this bid
        bidRepository.updateStatus(bidId, "accepted");

        // Reject all other active bids for the same property
        List<Bid> otherBids = bidRepository.findByPropertyId(bid.getPropertyId());
        for (Bid other : otherBids) {
            if (other.getBidId() != bidId && "active".equals(other.getBidStatus())) {
                bidRepository.updateStatus(other.getBidId(), "rejected");
            }
        }

        // Mark the property as pending (sale in progress)
        Property property = propertyRepository.findById(bid.getPropertyId());
        if (property != null) {
            property.setStatus("pending");
            propertyRepository.update(property);
        }

        return true;
    }

    /**
     * Rejects a specific bid.
     */
    public boolean rejectBid(int bidId) throws SQLException {
        return bidRepository.updateStatus(bidId, "rejected");
    }

    /**
     * Withdraws a bid (by the bidder).
     */
    public boolean withdrawBid(int bidId, int bidderId) throws SQLException {
        Bid bid = bidRepository.findById(bidId);
        if (bid == null) {
            throw new IllegalArgumentException("Bid not found.");
        }
        if (bid.getBidderId() != bidderId) {
            throw new IllegalArgumentException("You can only withdraw your own bids.");
        }
        return bidRepository.updateStatus(bidId, "withdrawn");
    }
}
