package com.myapp.repository;

import com.myapp.model.Sector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SectorRepository handles all database operations for the Sector table.
 *
 * Used by:
 *   UC1 – updateCapacityLimit()
 *   UC2 – updateStatus() (freeze/unfreeze)
 */
public class SectorRepository {

    public List<Sector> findAll() {
        List<Sector> sectors = new ArrayList<>();
        String sql = "SELECT * FROM Sector ORDER BY sector_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) sectors.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching sectors: " + e.getMessage());
        }
        return sectors;
    }

    public Sector findById(int sectorId) {
        String sql = "SELECT * FROM Sector WHERE sector_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error fetching sector " + sectorId + ": " + e.getMessage());
        }
        return null;
    }

    /** UC1: Set the capacity limit for a sector. */
    public boolean updateCapacityLimit(int sectorId, int newLimit) {
        String sql = "UPDATE Sector SET capacity_limit = ? WHERE sector_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newLimit);
            stmt.setInt(2, sectorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating sector capacity: " + e.getMessage());
            return false;
        }
    }

    /** UC2: Update the sector status (Active / Frozen). */
    public boolean updateStatus(int sectorId, String newStatus) {
        String sql = "UPDATE Sector SET status = ? WHERE sector_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, sectorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating sector status: " + e.getMessage());
            return false;
        }
    }

    private Sector mapRow(ResultSet rs) throws SQLException {
        Sector s = new Sector();
        s.setSectorId(rs.getInt("sector_id"));
        s.setSectorName(rs.getString("sector_name"));
        s.setCapacityLimit(rs.getInt("capacity_limit"));
        s.setCurrentCount(rs.getInt("current_count"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
