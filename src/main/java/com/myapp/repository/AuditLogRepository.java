package com.myapp.repository;

import java.sql.*;

public class AuditLogRepository {
    
    private DatabaseConnection dbConnection;
    
    public AuditLogRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Log an action to the audit_logs table.
     * Uses try-catch to never block the main operation if logging fails.
     * Falls back to Activity_Log table if audit_logs has FK issues.
     */
    public boolean logUpdate(int sectorId, int userId, String action) throws SQLException {
        // First try audit_logs table
        try {
            String query = "INSERT INTO audit_logs (sector_id, authority_id, action, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, sectorId);
                pstmt.setInt(2, userId);
                pstmt.setString(3, action);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            // If audit_logs fails (FK constraint), fall back to Activity_Log
            System.err.println("[AUDIT] audit_logs insert failed, using Activity_Log fallback: " + e.getMessage());
            return logToActivityLog(userId, action, "Sector", sectorId);
        }
    }
    
    /**
     * UC3 SD4 step 7: logListingCreation()
     * Logs when a new property listing is created.
     * Non-blocking — if logging fails, property save still succeeds.
     */
    public boolean logListingCreation(int agentId, int propertyId, int sectorId) throws SQLException {
        String action = "PROPERTY_LISTING_CREATED - Property ID: " + propertyId + " by Agent ID: " + agentId;
        return logToActivityLog(agentId, action, "Property", propertyId);
    }

    /**
     * Fallback: Log to Activity_Log table (always works, no strict FKs)
     */
    private boolean logToActivityLog(int userId, String action, String entityType, int entityId) {
        String query = "INSERT INTO Activity_Log (user_id, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, action.length() > 100 ? action.substring(0, 100) : action);
            pstmt.setString(3, entityType);
            pstmt.setInt(4, entityId);
            pstmt.setString(5, action);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AUDIT FALLBACK] Activity_Log insert also failed: " + e.getMessage());
            return false;  // Never block the main operation
        }
    }
}
