package com.myapp.repository;

import com.myapp.model.Bid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * BidRepository handles all database operations for the Bid table.
 *
 * Used by:
 *   UC6 – saveBid()
 *   UC9 – findBySessionId()
 */
public class BidRepository {

    /**
     * UC6: Insert a new bid record into the Bid table.
     *
     * @return true if the row was inserted, false if something went wrong
     */
    public boolean saveBid(Bid bid) {
        String sql = "INSERT INTO Bid (session_id, buyer_id, bid_amount, bid_time) "+ "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bid.getSessionId());
            stmt.setInt(2, bid.getBuyerId());
            stmt.setBigDecimal(3, bid.getBidAmount());
            stmt.setTimestamp(4, Timestamp.valueOf(bid.getBidTime()));

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("Error saving bid: " + e.getMessage());
            return false;
        }
    }

    /**
     * UC9: Get all bids for a session, sorted by:
     *   1. Highest bid amount first
     *   2. Earliest bid time first (tie-breaker — UC9 extension)
     *
     * The first item in the returned list is the winning bid.
     */
    public List<Bid> findBySessionId(int sessionId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM Bid WHERE session_id = ? "
                   + "ORDER BY bid_amount DESC, bid_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Bid bid = new Bid();
                bid.setBidId(rs.getInt("bid_id"));
                bid.setSessionId(rs.getInt("session_id"));
                bid.setBuyerId(rs.getInt("buyer_id"));
                bid.setBidAmount(rs.getBigDecimal("bid_amount"));
                bid.setBidTime(rs.getTimestamp("bid_time").toLocalDateTime());
                bids.add(bid);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bids for session " + sessionId + ": " + e.getMessage());
        }

        return bids;
    }
}