package com.myapp.controller;

import com.myapp.model.BiddingSession;
import com.myapp.service.BidService;

import java.math.BigDecimal;



 // BidController handles all requests related to bidding.
 // Use Cases:
 //   UC6 Place Bid on Property
 //   UC8 Close Bidding Automatically
 //   UC9 Declare Winning Bidder
 //
public class BidController {

    private final BidService bidService;

    public BidController() {
        this.bidService = new BidService();
    }

    // ──────────────────────────────────────────────────────
    // UC6 – Place Bid on Property
    // ──────────────────────────────────────────────────────

    /**
     * UC6 Step 1: Buyer opens the bidding page.
     * System displays the current highest bid (or base price if no bids yet).
     */
    public String openBiddingPage(int sessionId) {
        BiddingSession session = bidService.getSession(sessionId);

        if (session == null) {
            return "ERROR: Bidding session not found.";
        }

        // Extension: if session is closed, do not allow entry
        if (!session.isActive()) {
            return "ERROR: This bidding session is closed. No more bids are allowed.";
        }
BigDecimal displayedBid;
         if(session.getCurrentHighestBid() != null)
                displayedBid = session.getCurrentHighestBid();
                else
                    displayedBid = session.getBasePrice();

        return "Current highest bid: " + displayedBid;
    }

    /**
     * UC6 Step 2 & 3: Buyer enters a bid amount and submits it.
     */
    public String placeBid(int sessionId, int buyerId, BigDecimal bidAmount) {
        // System validates the bid amount
        String validation = bidService.validateBid(sessionId, bidAmount);
        if (!validation.equals("VALID")) {
            return validation;
        }

        // System records the bid in the database
        boolean saved = bidService.saveBid(sessionId, buyerId, bidAmount);
        if (!saved) {
            return "ERROR: Could not save your bid. Please try again.";
        }

        // System updates highest bid if this bid is the new highest
        bidService.updateHighestBid(sessionId, bidAmount);

        // System displays confirmation message
        return "SUCCESS: Your bid of " + bidAmount + " was placed successfully.";
    }

    // ──────────────────────────────────────────────────────
    // UC8 – Close Bidding Automatically
    // ──────────────────────────────────────────────────────

    /**
     * UC8: Called by a system timer when the bidding deadline has passed.
     * Stops new bids, records final highest bid, sets status to Closed.
     */
    public String closeBiddingSession(int sessionId) {
        return bidService.closeBiddingSession(sessionId);
    }

    // ──────────────────────────────────────────────────────
    // UC9 – Declare Winning Bidder
    // ──────────────────────────────────────────────────────

    /**
     * UC9: After the session is closed, declare the winner.
     * Retrieves all bids, picks the highest, records winner,
     * updates property status, and sends notification.
     */
    public String declareWinner(int sessionId) {
        return bidService.declareWinner(sessionId);
    }
}