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
}
