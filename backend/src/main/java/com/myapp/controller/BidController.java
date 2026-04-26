package com.myapp.controller;

import com.myapp.model.Bid;
import com.myapp.service.BidService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * CONTROLLER: Handles bid-related requests from the View layer.
 * Coordinates between the frontend (View) and the BidService (Model).
 *
 * In MVC, the Controller:
 *   1. Receives user input from the View (e.g., bid amount from auction page)
 *   2. Calls the appropriate Service/Model methods
 *   3. Returns the result to the View for display
 */
public class BidController {

    private final BidService bidService;

    public BidController() throws SQLException {
        this.bidService = new BidService();
    }

    // ── Place Bid ─────────────────────────────────────────────────────

    /**
     * Handles bid placement from the property details View.
     * @return success or error message
     */
    public String handlePlaceBid(int propertyId, int bidderId, String bidAmountStr) {
        try {
            BigDecimal bidAmount = new BigDecimal(bidAmountStr);
            int bidId = bidService.placeBid(propertyId, bidderId, bidAmount);

            if (bidId > 0) {
                return "SUCCESS: Bid placed successfully! Bid ID: " + bidId;
            } else {
                return "ERROR: Failed to place bid.";
            }
        } catch (NumberFormatException e) {
            return "ERROR: Invalid bid amount format.";
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }

    // ── View Bids ─────────────────────────────────────────────────────

    /**
     * Gets all bids for a property (for agent/admin to review).
     */
    public List<Bid> getBidsForProperty(int propertyId) {
        try {
            return bidService.getBidsForProperty(propertyId);
        } catch (SQLException e) {
            System.err.println("Error fetching bids: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Gets all bids placed by a specific user (for buyer dashboard).
     */
    public List<Bid> getMyBids(int bidderId) {
        try {
            return bidService.getBidsByUser(bidderId);
        } catch (SQLException e) {
            System.err.println("Error fetching user bids: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Gets the current highest bid for a property.
     */
    public Bid getHighestBid(int propertyId) {
        try {
            return bidService.getHighestBid(propertyId);
        } catch (SQLException e) {
            System.err.println("Error fetching highest bid: " + e.getMessage());
            return null;
        }
    }

    // ── Manage Bids ───────────────────────────────────────────────────

    /**
     * Accepts a bid (agent/admin action).
     */
    public String handleAcceptBid(int bidId) {
        try {
            boolean success = bidService.acceptBid(bidId);
            return success ? "SUCCESS: Bid accepted. Other bids have been rejected." :
                           "ERROR: Failed to accept bid.";
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }

    /**
     * Rejects a bid (agent/admin action).
     */
    public String handleRejectBid(int bidId) {
        try {
            boolean success = bidService.rejectBid(bidId);
            return success ? "SUCCESS: Bid rejected." : "ERROR: Bid not found.";
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }

    /**
     * Withdraws a bid (buyer action).
     */
    public String handleWithdrawBid(int bidId, int bidderId) {
        try {
            boolean success = bidService.withdrawBid(bidId, bidderId);
            return success ? "SUCCESS: Bid withdrawn." : "ERROR: Failed to withdraw bid.";
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }
}
