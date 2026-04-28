package com.myapp.repository;

import com.myapp.model.BiddingSession;

import java.math.BigDecimal;
import java.sql.*;

/**
 * BiddingSessionRepository handles all database operations for the Bidding_Session table.
 *
 * Used by:
 *   UC6 – findById(), updateHighestBid()
 *   UC8 – findById(), closeSession()
 *   UC9 – findById(), closeSession()
 */
public class BiddingSessionRepository {

    private DatabaseConnection dbConnection;  // ← ADD THIS
    
    public BiddingSessionRepository() {  // ← ADD CONSTRUCTOR
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * UC6, UC8, UC9: Retrieve a bidding session by its ID.
     *
     * @return the BiddingSession object, or null if not found
     */
    public BiddingSession findById(int sessionId) {
        String sql = "SELECT * FROM Bidding_Session WHERE session_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BiddingSession session = new BiddingSession();
                session.setSessionId(rs.getInt("session_id"));
                session.setPropertyId(rs.getInt("property_id"));
                session.setBasePrice(rs.getBigDecimal("base_price"));
                session.setDeadline(rs.getTimestamp("deadline").toLocalDateTime());
                session.setStatus(rs.getString("status"));

                // winner_id can be NULL in the database
                int winnerId = rs.getInt("winner_id");
                if (!rs.wasNull()) {
                    session.setWinnerId(winnerId);
                }

                // These can also be NULL if no bids have been placed yet
                BigDecimal winningBid = rs.getBigDecimal("winning_bid_amount");
                if (winningBid != null) session.setWinningBidAmount(winningBid);

                BigDecimal highestBid = rs.getBigDecimal("current_highest_bid");
                if (highestBid != null) session.setCurrentHighestBid(highestBid);

                return session;
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bidding session " + sessionId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all active bidding sessions for dashboard display.
     * Reuses same column mapping as findById.
     */
    public java.util.List<BiddingSession> findAllActive() {
        java.util.List<BiddingSession> sessions = new java.util.ArrayList<>();
        String sql = "SELECT * FROM Bidding_Session ORDER BY status ASC, session_id DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                BiddingSession session = new BiddingSession();
                session.setSessionId(rs.getInt("session_id"));
                session.setPropertyId(rs.getInt("property_id"));
                session.setBasePrice(rs.getBigDecimal("base_price"));

                Timestamp deadline = rs.getTimestamp("deadline");
                if (deadline != null) session.setDeadline(deadline.toLocalDateTime());

                session.setStatus(rs.getString("status"));

                int winnerId = rs.getInt("winner_id");
                if (!rs.wasNull()) session.setWinnerId(winnerId);

                BigDecimal winningBid = rs.getBigDecimal("winning_bid_amount");
                if (winningBid != null) session.setWinningBidAmount(winningBid);

                BigDecimal highestBid = rs.getBigDecimal("current_highest_bid");
                if (highestBid != null) session.setCurrentHighestBid(highestBid);

                sessions.add(session);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active sessions: " + e.getMessage());
        }

        return sessions;
    }

    /**
     * UC6: Update the current highest bid after a new bid is placed.
     */
    public boolean updateHighestBid(int sessionId, BigDecimal newHighestBid) {
        String sql = "UPDATE Bidding_Session SET current_highest_bid = ? WHERE session_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, newHighestBid);
            stmt.setInt(2, sessionId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Error updating highest bid for session " + sessionId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * UC8 & UC9: Close the session by setting its status to "Closed".
     * Also records the winner ID and winning bid amount.
     * winnerId can be null at UC8 close time; it gets set in UC9.
     */
    public boolean closeSession(int sessionId, Integer winnerId, BigDecimal winningAmount) {
        String sql = "UPDATE Bidding_Session "
                   + "SET status = 'Closed', winner_id = ?, winning_bid_amount = ? "
                   + "WHERE session_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // winner_id is nullable — set to NULL if not yet known
            if (winnerId != null) {
                stmt.setInt(1, winnerId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            stmt.setBigDecimal(2, winningAmount);
            stmt.setInt(3, sessionId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Error closing session " + sessionId + ": " + e.getMessage());
            return false;
        }
    }
}