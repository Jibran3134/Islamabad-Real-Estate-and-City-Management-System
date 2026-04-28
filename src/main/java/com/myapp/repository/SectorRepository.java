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
        String query = "SELECT sector_id, sector_name, capacity_limit, current_property_count FROM sectors";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Sector sector = new Sector(
                    rs.getInt("sector_id"),
                    rs.getString("sector_name"),
                    rs.getInt("capacity_limit"),
                    rs.getInt("current_property_count")
                );
                sectors.add(sector);
            }
        }
        return sectors;
    }
    
    // Information Expert: Sector ID se ek sector dhundo
    public Sector findSectorById(int sectorId) throws SQLException {
        String query = "SELECT sector_id, sector_name, capacity_limit, current_property_count FROM sectors WHERE sector_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, sectorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Sector(
                    rs.getInt("sector_id"),
                    rs.getString("sector_name"),
                    rs.getInt("capacity_limit"),
                    rs.getInt("current_property_count")
                );
            }
        }
        return null;
    }
    
    /**
     * Capacity update karne ka method (Low Coupling - sirf DB ka kaam)
     * 
     * ALT SCENARIO 3 FIX: Both updates run in a SINGLE TRANSACTION.
     * If updateSectorStatus() fails, the capacity change is rolled back too.
     * This matches SD4 step: beginTransaction() → UPDATE → saveChanges() → commitTransaction()
     * On failure: rollbackTransaction() is called (Alt Scenario 3 from Extended UC1)
     */
    public boolean updateCapacityLimit(int sectorId, int newCapacityLimit) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            
            // ALT SCENARIO 3: Begin transaction - both updates must succeed or both fail
            conn.setAutoCommit(false);
            
            // Step 1: Update capacity limit
            String updateCapacity = "UPDATE sectors SET capacity_limit = ?, last_updated = CURRENT_TIMESTAMP WHERE sector_id = ?";
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
            String updateStatus = "UPDATE sectors SET status = CASE WHEN current_property_count >= capacity_limit THEN 'OVERLOADED' ELSE 'ACTIVE' END WHERE sector_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateStatus)) {
                pstmt.setInt(1, sectorId);
                pstmt.executeUpdate();
            }
            
            // Both succeeded — commit transaction (SD4: commitTransaction())
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            // ALT SCENARIO 3: rollbackTransaction() — undo all changes on failure
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[ROLLBACK] Transaction rolled back due to: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("[CRITICAL] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e; // Re-throw so service layer can handle it
        } finally {
            // Restore auto-commit to default state
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    // Ignore
                }
            }
        }
    }
}