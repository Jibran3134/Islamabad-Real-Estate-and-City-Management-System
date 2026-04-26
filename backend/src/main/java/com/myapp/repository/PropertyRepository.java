package com.myapp.repository;

import com.myapp.model.Property;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REPOSITORY (Model Layer): Handles all database operations for Properties.
 * Implements CRUD operations against the 'properties' table.
 */
public class PropertyRepository {

    private final Connection connection;

    public PropertyRepository() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────

    /**
     * Inserts a new property listing into the database.
     * @return the generated property ID, or -1 if insertion failed.
     */
    public int create(Property property) throws SQLException {
        String sql = "INSERT INTO properties (title, description, property_type, sector, address, " +
                     "city, area_sqft, price, bedrooms, bathrooms, status, listed_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, property.getTitle());
            stmt.setString(2, property.getDescription());
            stmt.setString(3, property.getPropertyType());
            stmt.setString(4, property.getSector());
            stmt.setString(5, property.getAddress());
            stmt.setString(6, property.getCity());
            stmt.setDouble(7, property.getAreaSqft());
            stmt.setBigDecimal(8, property.getPrice());
            stmt.setInt(9, property.getBedrooms());
            stmt.setInt(10, property.getBathrooms());
            stmt.setString(11, property.getStatus());
            stmt.setInt(12, property.getListedBy());

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
     * Finds a property by its ID.
     */
    public Property findById(int propertyId) throws SQLException {
        String sql = "SELECT * FROM properties WHERE property_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProperty(rs);
            }
        }
        return null;
    }

    /**
     * Returns all properties in the system.
     */
    public List<Property> findAll() throws SQLException {
        String sql = "SELECT * FROM properties ORDER BY created_at DESC";
        List<Property> properties = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                properties.add(mapResultSetToProperty(rs));
            }
        }
        return properties;
    }

    /**
     * Returns all available properties (for public listing).
     */
    public List<Property> findAvailable() throws SQLException {
        String sql = "SELECT * FROM properties WHERE status = 'available' ORDER BY created_at DESC";
        List<Property> properties = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                properties.add(mapResultSetToProperty(rs));
            }
        }
        return properties;
    }

    /**
     * Returns all properties listed by a specific agent/user.
     */
    public List<Property> findByListedBy(int userId) throws SQLException {
        String sql = "SELECT * FROM properties WHERE listed_by = ? ORDER BY created_at DESC";
        List<Property> properties = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                properties.add(mapResultSetToProperty(rs));
            }
        }
        return properties;
    }

    /**
     * Searches properties by sector or type.
     */
    public List<Property> search(String sector, String propertyType) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM properties WHERE status = 'available'");
        List<Object> params = new ArrayList<>();

        if (sector != null && !sector.isEmpty()) {
            sql.append(" AND sector = ?");
            params.add(sector);
        }
        if (propertyType != null && !propertyType.isEmpty()) {
            sql.append(" AND property_type = ?");
            params.add(propertyType);
        }
        sql.append(" ORDER BY created_at DESC");

        List<Property> properties = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                properties.add(mapResultSetToProperty(rs));
            }
        }
        return properties;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    /**
     * Updates an existing property listing.
     */
    public boolean update(Property property) throws SQLException {
        String sql = "UPDATE properties SET title = ?, description = ?, property_type = ?, " +
                     "sector = ?, address = ?, area_sqft = ?, price = ?, bedrooms = ?, " +
                     "bathrooms = ?, status = ?, updated_at = GETDATE() WHERE property_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, property.getTitle());
            stmt.setString(2, property.getDescription());
            stmt.setString(3, property.getPropertyType());
            stmt.setString(4, property.getSector());
            stmt.setString(5, property.getAddress());
            stmt.setDouble(6, property.getAreaSqft());
            stmt.setBigDecimal(7, property.getPrice());
            stmt.setInt(8, property.getBedrooms());
            stmt.setInt(9, property.getBathrooms());
            stmt.setString(10, property.getStatus());
            stmt.setInt(11, property.getPropertyId());

            return stmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────

    /**
     * Deletes a property by its ID.
     */
    public boolean delete(int propertyId) throws SQLException {
        String sql = "DELETE FROM properties WHERE property_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, propertyId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────────

    /**
     * Maps a ResultSet row to a Property object.
     */
    private Property mapResultSetToProperty(ResultSet rs) throws SQLException {
        Property property = new Property();
        property.setPropertyId(rs.getInt("property_id"));
        property.setTitle(rs.getString("title"));
        property.setDescription(rs.getString("description"));
        property.setPropertyType(rs.getString("property_type"));
        property.setSector(rs.getString("sector"));
        property.setAddress(rs.getString("address"));
        property.setCity(rs.getString("city"));
        property.setAreaSqft(rs.getDouble("area_sqft"));
        property.setPrice(rs.getBigDecimal("price"));
        property.setBedrooms(rs.getInt("bedrooms"));
        property.setBathrooms(rs.getInt("bathrooms"));
        property.setStatus(rs.getString("status"));
        property.setListedBy(rs.getInt("listed_by"));
        property.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        property.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return property;
    }
}
