package com.myapp.repository;

import com.myapp.model.Sector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectorRepository {
    
    private DatabaseConnection dbConnection;
    
    public SectorRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public List<Sector> findAllSectors() throws SQLException {
        List<Sector> sectors = new ArrayList<>();
        String query = "SELECT sector_id, sector_name, capacity_limit, current_property_count FROM sectors";
        
        // FIX: getInstance() call karo pehle
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
    
    public boolean updateCapacityLimit(int sectorId, int newCapacityLimit) throws SQLException {
        String query = "UPDATE sectors SET capacity_limit = ?, last_updated = CURRENT_TIMESTAMP WHERE sector_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, newCapacityLimit);
            pstmt.setInt(2, sectorId);
            int rowsUpdated = pstmt.executeUpdate();
            
            updateSectorStatus(sectorId);
            
            return rowsUpdated > 0;
        }
    }
    
    private void updateSectorStatus(int sectorId) throws SQLException {
        String query = "UPDATE sectors SET status = CASE WHEN current_property_count >= capacity_limit THEN 'OVERLOADED' ELSE 'ACTIVE' END WHERE sector_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, sectorId);
            pstmt.executeUpdate();
        }
    }
}