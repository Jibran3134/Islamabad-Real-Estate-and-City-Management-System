package com.myapp.repository;

import com.myapp.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * UserRepository handles all database operations for the Users table.
 *
 * Used by:
 *   UC11 – findAllUsers(), findById(), updateStatus()
 */
public class UserRepository {

    private DatabaseConnection dbConnection;  // ← ADD THIS
    
    public UserRepository() {  // ← ADD CONSTRUCTOR
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * UC11: Retrieve all users from the database.
     * Called when admin opens the user management dashboard.
     */
    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }

        return users;
    }

    /**
     * UC11: Retrieve a single user by their ID.
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user " + userId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * UC11: Update a user's status (Active / Suspended / Inactive).
     */
    public boolean updateStatus(int userId, String newStatus) {
        String sql = "UPDATE Users SET status = ? WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, userId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user " + userId + " status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Auth: Find a user by email address for login.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Auth: Insert a new user and return the generated user_id.
     */
    public int insertUser(User user) {
        String sql = "INSERT INTO Users (role_id, full_name, email, password_hash, status, phone_number) VALUES (?,?,?,?,?,?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, user.getRoleId());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getStatus() != null ? user.getStatus() : "Active");
            stmt.setString(6, user.getPhoneNumber());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Helper: maps a ResultSet row into a User object.
     * Used by findAllUsers() and findById().
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setRoleId(rs.getInt("role_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setStatus(rs.getString("status"));
        user.setPhoneNumber(rs.getString("phone_number"));
        return user;
    }
}