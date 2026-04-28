package com.myapp.repository;

import java.sql.*;

public class AuditLogRepository {
    
    private DatabaseConnection dbConnection;
    
    public AuditLogRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public boolean logUpdate(int sectorId, int authorityId, String action) throws SQLException {
        String query = "INSERT INTO audit_logs (sector_id, authority_id, action, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, sectorId);
            pstmt.setInt(2, authorityId);
            pstmt.setString(3, action);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // ========== UC3 KE LIYE NAYA METHOD ==========
    
    /**
     * UC3 SD4 step 7: logListingCreation()
     * Logs when a new property listing is created
     * GRASP: PURE FABRICATION - AuditLog has no domain equivalent
     */
    public boolean logListingCreation(int agentId, int propertyId, int sectorId) throws SQLException {
        String query = "INSERT INTO audit_logs (sector_id, authority_id, action, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, sectorId);
            pstmt.setInt(2, agentId);
            pstmt.setString(3, "PROPERTY_LISTING_CREATED - Property ID: " + propertyId + " by Agent ID: " + agentId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
}
