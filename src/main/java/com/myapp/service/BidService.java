package com.myapp.service;

import com.myapp.model.Bid;
import com.myapp.model.Property;
import com.myapp.repository.BidRepository;
import com.myapp.repository.PropertyRepository;
import com.myapp.model.BiddingSession;
import com.myapp.repository.BiddingSessionRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;





/**
 * BidService contains all the business logic for bidding.
 *
 * Use Cases:
 *   UC6  – Place Bid on Property
 *   UC8  – Close Bidding Automatically
 *   UC9  – Declare Winning Bidder
 */
public class BidService {

    private final BidRepository bidRepository;
    private final BiddingSessionRepository sessionRepository;
    private final PropertyRepository propertyRepository;

    public BidService() {
        this.bidRepository = new BidRepository();
        this.sessionRepository = new BiddingSessionRepository();
        this.propertyRepository = new PropertyRepository();
    }

    // ──────────────────────────────────────────────────────
    // UC6 – Place Bid on Property
    // ──────────────────────────────────────────────────────

    /**
     * UC6: Fetch the bidding session so the controller can display
     * the current highest bid to the buyer.
     */
    public BiddingSession getSession(int sessionId) {
        return sessionRepository.findById(sessionId);
    }

    /**
     * UC6: Validate the submitted bid amount.
     * - Session must exist and be active.
     * - Bid must be higher than the current highest bid (or base price).
     */
    public String validateBid(int sessionId, BigDecimal bidAmount) {
        BiddingSession session = sessionRepository.findById(sessionId);

        if (session == null) {
            return "ERROR: Bidding session not found.";
        }

        // Extension: session is closed — do not allow the bid
        if (!session.isActive()) {
            return "ERROR: Bidding session is closed. You cannot place a bid.";
        }

        // Extension: bid is too low — reject and show message
        if (!session.isBidHighEnough(bidAmount)) {
            BigDecimal minimum = (session.getCurrentHighestBid() != null)
                    ? session.getCurrentHighestBid()
                    : session.getBasePrice();
            return "ERROR: Your bid must be higher than the current highest bid of " + minimum + ".";
        }

        return "VALID";
    }

    /**
     * UC6: Save the new bid to the database.
     */
    public boolean saveBid(int sessionId, int buyerId, BigDecimal bidAmount) {
        Bid bid = new Bid(sessionId, buyerId, bidAmount);
        return bidRepository.saveBid(bid);
    }

    /**
     * UC6: Update the session's current highest bid in the database.
     */
    public void updateHighestBid(int sessionId, BigDecimal bidAmount) {
        sessionRepository.updateHighestBid(sessionId, bidAmount);
    }

    // ──────────────────────────────────────────────────────
    // UC8 – Close Bidding Automatically
    // ──────────────────────────────────────────────────────

    /**
     * UC8: Called when the system timer detects the deadline has passed.
     *
     * Steps:
     *   1. Confirm the session exists and is still active.
     *   2. Check deadline has actually passed.
     *   3. Stop accepting new bids by setting status to Closed.
     *   4. Record the final highest bid.
     *   5. Return closure confirmation.
     */
    public String closeBiddingSession(int sessionId) {
        BiddingSession session = sessionRepository.findById(sessionId);

        if (session == null) {
            return "ERROR: Bidding session not found.";
        }

        if (!session.isActive()) {
            return "INFO: Bidding session is already closed.";
        }

        // Check that the deadline has actually passed
        if (!session.isDeadlinePassed()) {
            return "INFO: Bidding deadline has not passed yet. Session remains open.";
        }

        // Close the session — sets status to "Closed" on the model object
        session.closeBidding(null, session.getCurrentHighestBid());

        // Persist the closed status and final highest bid to the database
        boolean closed = sessionRepository.closeSession(
                sessionId,
                null,
                session.getCurrentHighestBid()
        );

        if (!closed) {
            return "ERROR: Could not close bidding session. Please retry.";
        }

        return "SUCCESS: Bidding session " + sessionId + " has been closed. "
                + "Final highest bid: " + session.getCurrentHighestBid();
    }

    // ──────────────────────────────────────────────────────
    // UC9 – Declare Winning Bidder
    // ──────────────────────────────────────────────────────

    /**
     * UC9: After session is closed, determine and record the winner.
     *
     * Steps:
     *   1. Confirm session is closed.
     *   2. Retrieve all bids for the session.
     *   3. Find the highest valid bid (if tie, pick earliest by time).
     *   4. Record the winning bidder on the session.
     *   5. Update the property listing status to "Sold".
     *   6. Send winner notification (simulated here as a return message).
     */
    public String declareWinner(int sessionId) {
        BiddingSession session = sessionRepository.findById(sessionId);

        if (session == null) {
            return "ERROR: Bidding session not found.";
        }

        if (session.isActive()) {
            return "ERROR: Bidding session is still active. Close it first.";
        }

        // Retrieve all bids, ordered by amount descending then time ascending
        List<Bid> bids = bidRepository.findBySessionId(sessionId);

        if (bids == null || bids.isEmpty()) {
            return "INFO: No bids found for this session. No winner to declare.";
        }

        // The first bid in the list is the winner
        // (BidRepository returns bids sorted by amount DESC, then time ASC for tie-breaking)
        Bid winningBid = bids.get(0);

        // Record the winning bidder on the session
        session.closeBidding(winningBid.getBuyerId(), winningBid.getBidAmount());
        sessionRepository.closeSession(
                sessionId,
                winningBid.getBuyerId(),
                winningBid.getBidAmount()
        );

        // Update the property listing status to "Sold"
        propertyRepository.updateListingStatus(session.getPropertyId(), "Sold");

        // Notify the winner (in a real system this would send an email)
        String notification = sendWinnerNotification(winningBid.getBuyerId(), winningBid.getBidAmount());

        return "SUCCESS: Winner declared. Buyer ID " + winningBid.getBuyerId()
                + " won with a bid of " + winningBid.getBidAmount() + ". " + notification;
    }

    /**
     * Simulates sending a notification to the winning bidder.
     * In a real system, this would send an email or push notification.
     */
    private String sendWinnerNotification(int buyerId, BigDecimal winningAmount) {
        // Placeholder — replace with actual email/notification logic
        System.out.println("Notification sent to buyer " + buyerId
                + ": You won the auction with a bid of " + winningAmount + "!");
        return "Winner notification sent to buyer ID " + buyerId + ".";
    }
}