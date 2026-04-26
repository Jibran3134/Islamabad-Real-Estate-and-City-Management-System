package com.myapp.service;

import com.myapp.model.User;
import com.myapp.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * SERVICE (Model Layer - Business Logic): Handles user-related business rules.
 * Sits between the Controller and Repository, enforcing validation and logic.
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService() throws SQLException {
        this.userRepository = new UserRepository();
    }

    // ── Registration ──────────────────────────────────────────────────

    /**
     * Registers a new user after validation.
     * @return the new user's ID
     * @throws IllegalArgumentException if validation fails
     */
    public int registerUser(String fullName, String email, String password,
                            String phone, String role) throws SQLException {

        // Validate required fields
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        // Check for duplicate email
        User existing = userRepository.findByEmail(email);
        if (existing != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // Validate role
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid role. Must be: admin, agent, buyer, or authority.");
        }

        // Hash password (simple hash for now — replace with BCrypt in production)
        String passwordHash = hashPassword(password);

        User user = new User(fullName, email, passwordHash, phone, role);
        return userRepository.create(user);
    }

    // ── Authentication ────────────────────────────────────────────────

    /**
     * Authenticates a user by email and password.
     * @return the authenticated User, or null if credentials are invalid.
     */
    public User login(String email, String password) throws SQLException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return null; // User not found
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("This account has been deactivated.");
        }

        // Verify password
        String passwordHash = hashPassword(password);
        if (!user.getPasswordHash().equals(passwordHash)) {
            return null; // Wrong password
        }

        return user;
    }

    // ── User Management ───────────────────────────────────────────────

    /**
     * Gets a user by their ID.
     */
    public User getUserById(int userId) throws SQLException {
        return userRepository.findById(userId);
    }

    /**
     * Gets all users in the system (admin only).
     */
    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }

    /**
     * Gets all users with a specific role.
     */
    public List<User> getUsersByRole(String role) throws SQLException {
        return userRepository.findByRole(role);
    }

    /**
     * Updates a user's profile.
     */
    public boolean updateUser(User user) throws SQLException {
        return userRepository.update(user);
    }

    /**
     * Deactivates a user account (soft delete).
     */
    public boolean deactivateUser(int userId) throws SQLException {
        User user = userRepository.findById(userId);
        if (user != null) {
            user.setActive(false);
            return userRepository.update(user);
        }
        return false;
    }

    /**
     * Deletes a user permanently.
     */
    public boolean deleteUser(int userId) throws SQLException {
        return userRepository.delete(userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private boolean isValidRole(String role) {
        return role != null && (role.equals("admin") || role.equals("agent") ||
                role.equals("buyer") || role.equals("authority"));
    }

    /**
     * Simple password hashing. In a real production app, use BCrypt or Argon2.
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
