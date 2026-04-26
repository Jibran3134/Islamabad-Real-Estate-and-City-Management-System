package com.myapp.controller;

import com.myapp.model.User;
import com.myapp.service.UserService;

import java.sql.SQLException;
import java.util.List;

/**
 * CONTROLLER: Handles user-related requests from the View layer.
 * Coordinates between the frontend (View) and the UserService (Model).
 *
 * In MVC, the Controller:
 *   1. Receives user input from the View
 *   2. Calls the appropriate Service/Model methods
 *   3. Returns the result to the View for display
 */
public class UserController {

    private final UserService userService;

    public UserController() throws SQLException {
        this.userService = new UserService();
    }

    // ── Registration ──────────────────────────────────────────────────

    /**
     * Handles user registration requests from the registration form.
     * @return success message or error message
     */
    public String handleRegister(String fullName, String email, String password,
                                 String phone, String role) {
        try {
            int userId = userService.registerUser(fullName, email, password, phone, role);
            if (userId > 0) {
                return "SUCCESS: Account created successfully! Your user ID is " + userId;
            } else {
                return "ERROR: Registration failed. Please try again.";
            }
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (SQLException e) {
            return "ERROR: Database error — " + e.getMessage();
        }
    }

    // ── Login ─────────────────────────────────────────────────────────

    /**
     * Handles login requests from the login form.
     * @return the authenticated User or null if login fails
     */
    public User handleLogin(String email, String password) {
        try {
            User user = userService.login(email, password);
            if (user != null) {
                System.out.println("Login successful: " + user.getFullName() + " (" + user.getRole() + ")");
            }
            return user;
        } catch (IllegalArgumentException e) {
            System.err.println("Login error: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Determines which dashboard view to show based on the user's role.
     * @return the dashboard view identifier
     */
    public String getDashboardView(User user) {
        if (user == null) return "login";

        return switch (user.getRole()) {
            case "admin"     -> "dashboard-admin";
            case "agent"     -> "dashboard-agent";
            case "buyer"     -> "dashboard-buyer";
            case "authority" -> "dashboard-authority";
            default          -> "login";
        };
    }

    // ── User Management (Admin) ───────────────────────────────────────

    /**
     * Gets all users (for admin panel).
     */
    public List<User> getAllUsers() {
        try {
            return userService.getAllUsers();
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Gets a user by their ID.
     */
    public User getUserById(int userId) {
        try {
            return userService.getUserById(userId);
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deactivates a user account.
     */
    public String handleDeactivateUser(int userId) {
        try {
            boolean success = userService.deactivateUser(userId);
            return success ? "SUCCESS: User deactivated." : "ERROR: User not found.";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
