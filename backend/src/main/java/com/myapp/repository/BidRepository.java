package com.myapp.repository;

import com.myapp.model.Bid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REPOSITORY (Model Layer): Handles all database operations for Bids.
 * Implements CRUD operations against the 'bids' table.
 */
public class BidRepository {

    private final Connection connection;

    public BidRepository() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    /**
     * Places a new bid on a property.
     * @return the generated bid ID, or -1 if insertion failed.
     */
    public int create(Bid bid) throws SQLException {
        String sql = "INSERT INTO bids (property_id, bidder_id, bid_amount, bid_status) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, bid.getPropertyId());
            stmt.setInt(2, bid.getBidderId());
            stmt.setBigDecimal(3, bid.getBidAmount());
            stmt.setString(4, bid.getBidStatus());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    // ── READ ──────────────────────────────────────────────────────────

    /**
     * Finds a bid by its ID.
     */
    public Bid findById(int bidId) throws SQLException {
        String sql = "SELECT * FROM bids WHERE bid_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bidId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBid(rs);
            }
        }
        return null;
    }

    /**
     * Returns all bids for a specific property.
     */
    public List<Bid> findByPropertyId(int propertyId) throws SQLException {
        String sql = "SELECT * FROM bids WHERE property_id = ? ORDER BY bid_amount DESC";
        List<Bid> bids = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bids.add(mapResultSetToBid(rs));
            }
        }
        return bids;
    }

    /**
     * Returns all bids placed by a specific user.
     */
    public List<Bid> findByBidderId(int bidderId) throws SQLException {
        String sql = "SELECT * FROM bids WHERE bidder_id = ? ORDER BY created_at DESC";
        List<Bid> bids = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bidderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bids.add(mapResultSetToBid(rs));
            }
        }
        return bids;
    }

    /**
     * Returns the highest active bid for a property.
     */
    public Bid findHighestBid(int propertyId) throws SQLException {
        String sql = "SELECT TOP 1 * FROM bids WHERE property_id = ? AND bid_status = 'active' " +
                     "ORDER BY bid_amount DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBid(rs);
            }
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    /**
     * Updates the status of a bid (accept, reject, withdraw).
     */
    public boolean updateStatus(int bidId, String status) throws SQLException {
        String sql = "UPDATE bids SET bid_status = ? WHERE bid_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, bidId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────

    /**
     * Deletes a bid by its ID.
     */
    public boolean delete(int bidId) throws SQLException {
        String sql = "DELETE FROM bids WHERE bid_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bidId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────────

    private Bid mapResultSetToBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setBidId(rs.getInt("bid_id"));
        bid.setPropertyId(rs.getInt("property_id"));
        bid.setBidderId(rs.getInt("bidder_id"));
        bid.setBidAmount(rs.getBigDecimal("bid_amount"));
        bid.setBidStatus(rs.getString("bid_status"));
        bid.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return bid;
    }
}
