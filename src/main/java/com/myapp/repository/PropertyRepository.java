package com.myapp.repository;

import com.myapp.model.Property;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PropertyRepository handles all database operations for the Property table.
 *
 * Used by:
 *   UC6 – findById() (confirm property is open for bidding)
 *   UC9 – updateListingStatus() (mark as Sold)
 */
public class PropertyRepository {

    private DatabaseConnection dbConnection;  // ← ADD THIS
    
    public PropertyRepository() {  // ← ADD CONSTRUCTOR
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Retrieve a single property by its ID.
     */
    public Property findById(int propertyId) {
        String sql = "SELECT * FROM Property WHERE property_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching property " + propertyId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * UC4: Get all properties with optional filters.
     */
    public List<Property> findAll(String keyword, String sectorName, String propertyType, BigDecimal maxPrice) {
        List<Property> properties = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.* FROM Property p JOIN Sector s ON p.sector_id = s.sector_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(p.title) LIKE ? OR LOWER(p.location) LIKE ?)");
            String kw = "%" + keyword.toLowerCase() + "%";
            params.add(kw); params.add(kw);
        }
        if (sectorName != null && !sectorName.trim().isEmpty()) {
            sql.append(" AND s.sector_name LIKE ?");
            params.add("%" + sectorName + "%");
        }
        if (propertyType != null && !propertyType.trim().isEmpty()) {
            sql.append(" AND p.property_type = ?");
            params.add(propertyType);
        }
        if (maxPrice != null) {
            sql.append(" AND p.price <= ?");
            params.add(maxPrice);
        }
        sql.append(" ORDER BY p.created_at DESC");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) stmt.setString(i + 1, (String) p);
                else if (p instanceof BigDecimal) stmt.setBigDecimal(i + 1, (BigDecimal) p);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) properties.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching properties: " + e.getMessage());
        }
        return properties;
    }

    /**
     * UC3: Insert a new property listing; returns the generated property_id.
     */
    public int insertProperty(Property p) {
        String sql = "INSERT INTO Property (sector_id, agent_id, title, description, price, property_type, " +
                     "location, listing_status, selling_method, created_at) VALUES (?,?,?,?,?,?,?,?,?,GETDATE())";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, p.getSectorId());
            stmt.setInt(2, p.getAgentId());
            stmt.setString(3, p.getTitle());
            stmt.setString(4, p.getDescription());
            stmt.setBigDecimal(5, p.getPrice());
            stmt.setString(6, p.getPropertyType());
            stmt.setString(7, p.getLocation());
            stmt.setString(8, p.getListingStatus() != null ? p.getListingStatus() : "For Sale");
            stmt.setString(9, p.getSellingMethod() != null ? p.getSellingMethod() : "Fixed Price");
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error inserting property: " + e.getMessage());
        }
        return -1;
    }

    /**
     * UC5: Update an existing property listing.
     */
    public boolean updateProperty(Property p) {
        String sql = "UPDATE Property SET title=?, description=?, price=?, property_type=?, " +
                     "location=?, listing_status=?, selling_method=? WHERE property_id=?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getTitle());
            stmt.setString(2, p.getDescription());
            stmt.setBigDecimal(3, p.getPrice());
            stmt.setString(4, p.getPropertyType());
            stmt.setString(5, p.getLocation());
            stmt.setString(6, p.getListingStatus());
            stmt.setString(7, p.getSellingMethod());
            stmt.setInt(8, p.getPropertyId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating property " + p.getPropertyId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * UC6: Get all properties that are listed For Sale with Bidding as selling method.
     */
    public List<Property> findPropertiesOpenForBidding() {
        List<Property> properties = new ArrayList<>();
        String sql = "SELECT * FROM Property "
                   + "WHERE listing_status = 'For Sale' AND selling_method = 'Bidding'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                properties.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bidding properties: " + e.getMessage());
        }

        return properties;
    }

    /**
     * UC9: Update the listing status of a property (e.g., "For Sale" → "Sold").
     */
    public boolean updateListingStatus(int propertyId, String newStatus) {
        String sql = "UPDATE Property SET listing_status = ? WHERE property_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, propertyId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Error updating property status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search properties by sector and type (Supports Main UI Search).
     */
    public List<Property> search(String sectorName, String propertyType) {
        List<Property> properties = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.* FROM Property p JOIN Sector s ON p.sector_id = s.sector_id WHERE 1=1");
        
        if (sectorName != null && !sectorName.isEmpty()) sql.append(" AND s.sector_name LIKE ?");
        if (propertyType != null && !propertyType.isEmpty()) sql.append(" AND p.property_type = ?");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (sectorName != null && !sectorName.isEmpty()) stmt.setString(idx++, "%" + sectorName + "%");
            if (propertyType != null && !propertyType.isEmpty()) stmt.setString(idx++, propertyType);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) properties.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return properties;
    }

    /**
     * Helper: maps a ResultSet row into a Property object.
     * Keeps the code DRY — used by findById() and findPropertiesOpenForBidding().
     */
    private Property mapRow(ResultSet rs) throws SQLException {
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
        return p;
    }
}