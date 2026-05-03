package com.myapp.adapter;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


 /** GoF DESIGN PATTERN: ADAPTER
 / GRASP: PURE FABRICATION
 SD2 - DatabaseAdapter adapts raw SQL/ResultSet operations into clean Java method calls
 */
public class DatabaseAdapter {
    
    private Connection connection;
    
    public DatabaseAdapter(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * SD2 step 3: executeQuery(sqlFilters)
     * Builds and executes search query, returns Property list
     */
    public List<Property> executePropertySearch(SearchCriteria criteria) throws SQLException {
        StringBuilder query = new StringBuilder(
            "SELECT p.*, s.sector_name FROM Property p " +
            "LEFT JOIN Sector s ON p.sector_id = s.sector_id " +
            "WHERE p.listing_status = 'For Sale'"
        );
        
        List<Object> params = new ArrayList<>();
        
        // Add filters dynamically
        if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
            query.append(" AND p.location LIKE ?");
            params.add("%" + criteria.getLocation() + "%");
        }
        
        if (criteria.getMinPrice() != null) {
            query.append(" AND p.price >= ?");
            params.add(criteria.getMinPrice());
        }
        
        if (criteria.getMaxPrice() != null) {
            query.append(" AND p.price <= ?");
            params.add(criteria.getMaxPrice());
        }
        
        if (criteria.getPropertyType() != null && !criteria.getPropertyType().trim().isEmpty()) {
            query.append(" AND p.property_type = ?");
            params.add(criteria.getPropertyType());
        }
        
        if (criteria.getSectorId() != null) {
            query.append(" AND p.sector_id = ?");
            params.add(criteria.getSectorId());
        }
        
        query.append(" ORDER BY p.created_at DESC");
        
        // Execute query and convert results
        try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) pstmt.setString(i + 1, (String) param);
                else if (param instanceof BigDecimal) pstmt.setBigDecimal(i + 1, (BigDecimal) param);
                else if (param instanceof Integer) pstmt.setInt(i + 1, (Integer) param);
            }
            
            ResultSet rs = pstmt.executeQuery();
            return convertResultSetToProperties(rs);
        }
    }
    
    /**
     * SD2 step 5: Converts ResultSet to List of Property objects
     * Uses existing Property model setters (no custom constructor needed)
     */
    private List<Property> convertResultSetToProperties(ResultSet rs) throws SQLException {
        List<Property> properties = new ArrayList<>();
        
        while (rs.next()) {
            Property p = new Property();
            p.setPropertyId(rs.getInt("property_id"));
            p.setSectorId(rs.getInt("sector_id"));
            p.setAgentId(rs.getInt("agent_id"));
            p.setTitle(rs.getString("title"));
            p.setDescription(rs.getString("description"));
            p.setPrice(rs.getBigDecimal("price"));
            p.setPropertyType(rs.getString("property_type"));
            p.setLocation(rs.getString("location"));
            p.setListingStatus(rs.getString("listing_status"));
            p.setSellingMethod(rs.getString("selling_method"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
            properties.add(p);
        }
        
        return properties;
    }
}
