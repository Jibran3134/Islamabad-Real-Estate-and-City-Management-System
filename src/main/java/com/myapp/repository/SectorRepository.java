package com.myapp.repository;

import com.myapp.model.Sector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// GRASP: Information Expert - Iske paas sector data hai, to yehi DB se fetch karega
public class SectorRepository {
    
    private DatabaseConnection dbConnection;
    
    // Constructor
    public SectorRepository() {
        this.dbConnection = DatabaseConnection.getInstance(); // Singleton use kiya
    }
    
    // Saare sectors fetch karne ke liye
    public List<Sector> findAllSectors() throws SQLException {
        List<Sector> sectors = new ArrayList<>();
        String query = "SELECT sector_id, sector_name, capacity_limit, current_count FROM Sector";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Sector sector = new Sector(
                    rs.getInt("sector_id"),
                    rs.getString("sector_name"),
                    rs.getInt("capacity_limit"),
                    rs.getInt("current_count")
                );
                sectors.add(sector);
            }
        }
        return sectors;
    }
    
    // Information Expert: Sector ID se ek sector dhundo
    public Sector findSectorById(int sectorId) throws SQLException {
        String query = "SELECT sector_id, sector_name, capacity_limit, current_count FROM Sector WHERE sector_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, sectorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Sector(
                    rs.getInt("sector_id"),
                    rs.getString("sector_name"),
                    rs.getInt("capacity_limit"),
                    rs.getInt("current_count")
                );
            }
        }
        return null;
    }
    
    /**
     * UC1: Capacity update with TRANSACTION SUPPORT
     * ALT SCENARIO 3 FIX: Both updates in a SINGLE TRANSACTION
     */
    public boolean updateCapacityLimit(int sectorId, int newCapacityLimit) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Update capacity limit
            String updateCapacity = "UPDATE Sector SET capacity_limit = ? WHERE sector_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateCapacity)) {
                pstmt.setInt(1, newCapacityLimit);
                pstmt.setInt(2, sectorId);
                int rowsUpdated = pstmt.executeUpdate();
                
                if (rowsUpdated == 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            // Step 2: Update sector status (same transaction)
            String updateStatus = "UPDATE Sector SET status = CASE WHEN current_count >= capacity_limit THEN 'OVERLOADED' ELSE 'ACTIVE' END WHERE sector_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateStatus)) {
                pstmt.setInt(1, sectorId);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[ROLLBACK] Transaction rolled back due to: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("[CRITICAL] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) {}
            }
        }
    }
    
    // ========== UC2 KE LIYE METHODS ==========
    
    /**
     * UC2 - Freeze sector with TRANSACTION SUPPORT
     */
    public boolean freezeSector(int sectorId) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Step 1: Update sector status to FROZEN
            String updateStatusQuery = "UPDATE Sector SET status = 'FROZEN' WHERE sector_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateStatusQuery)) {
                pstmt.setInt(1, sectorId);
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            // Step 2: Block property listings in this sector
            String blockListingsQuery = "UPDATE Property SET listing_status = 'Blocked' WHERE sector_id = ? AND listing_status = 'For Sale'";
            try (PreparedStatement pstmt = conn.prepareStatement(blockListingsQuery)) {
                pstmt.setInt(1, sectorId);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[ROLLBACK] Freeze transaction rolled back: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("[CRITICAL] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) {}
            }
        }
    }
    
    /**
     * UC2 - Check if sector is overloaded from DB
     */
    public boolean isSectorOverloaded(int sectorId) throws SQLException {
        String query = "SELECT CASE WHEN current_count >= capacity_limit THEN 1 ELSE 0 END AS is_overloaded FROM Sector WHERE sector_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, sectorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_overloaded");
            }
        }
        return false;
    }
    
    /**
     * UC2 SD1: getAllSectorsWithUsage()
     */
    public List<SectorStatistics> getAllSectorsWithUsage() throws SQLException {
        List<SectorStatistics> stats = new ArrayList<>();
        String query = "SELECT sector_id, sector_name, capacity_limit, current_count, " +
                       "status, " +
                       "CAST(current_count AS FLOAT) / capacity_limit * 100 AS usage_percentage " +
                       "FROM Sector";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                SectorStatistics stat = new SectorStatistics();
                stat.sectorId = rs.getInt("sector_id");
                stat.sectorName = rs.getString("sector_name");
                stat.capacityLimit = rs.getInt("capacity_limit");
                stat.currentCount = rs.getInt("current_count");
                stat.status = rs.getString("status");
                stat.usagePercentage = rs.getDouble("usage_percentage");
                stats.add(stat);
            }
        }
        return stats;
    }
    
    // ========== UC3 KE LIYE METHOD ==========
    
    /**
     * UC3 SD4 step 3: verifySectorAvailability()
     * Checks if sector exists and is not frozen
     */
    public boolean isSectorAvailableForListing(int sectorId) throws SQLException {
        String query = "SELECT status FROM Sector WHERE sector_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, sectorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                return !"FROZEN".equals(status) && !"Frozen".equals(status);
            }
            return false;
        }
    }
    
    /**
     * Inner class for sector statistics (UC2 SD1 required)
     */
    public static class SectorStatistics {
        public int sectorId;
        public String sectorName;
        public int capacityLimit;
        public int currentCount;
        public String status;
        public double usagePercentage;
        
        public boolean isOverloaded() {
            return currentCount >= capacityLimit;
        }
        
        public boolean isFrozen() {
            return "FROZEN".equals(status) || "Frozen".equals(status);
        }
        
        @Override
        public String toString() {
            return sectorName + " - " + currentCount + "/" + capacityLimit + 
                   " (" + String.format("%.1f", usagePercentage) + "%) - " + status;
        }
    }
}