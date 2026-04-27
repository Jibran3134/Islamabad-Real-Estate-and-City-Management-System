package com.myapp.controller;

import com.myapp.model.User;
import com.myapp.service.UserService;

import java.sql.SQLException;
import java.util.List;


/**
 * UserController handles all requests related to user account management.
 *
 * Use Cases:
 *   UC11 – Managing User Accounts
 */
public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    // ──────────────────────────────────────────────────────
    // UC11 – Managing User Accounts
    // ──────────────────────────────────────────────────────

    /**
     * UC11 Step 1: Administrator opens the user management dashboard.
     * System retrieves all user records from the database.
     * System processes the query and returns the user list.
     *
     * @param adminId  The ID of the admin making the request (for permission check)
     */
    public String openUserManagementDashboard(int adminId) {
        // Check if the requesting user has admin permissions
        boolean hasPermission = userService.isAdmin(adminId);
        if (!hasPermission) {
            // Extension: If permission denied, block access
            return "ERROR: Access denied. You do not have admin permissions.";
        }

        // System retrieves user records from the database
        List<User> users = userService.getAllUsers();

        // Extension: If no records found, show empty list message
        if (users == null || users.isEmpty()) {
            return "INFO: No user records found.";
        }

        // System displays user account list
        StringBuilder result = new StringBuilder();
        result.append("User Account List (").append(users.size()).append(" records):\n");
        result.append("------------------------------------------------------------\n");

        for (User user : users) {
            result.append("ID: ").append(user.getUserId())
                  .append(" | Name: ").append(user.getFullName())
                  .append(" | Email: ").append(user.getEmail())
                  .append(" | Role: ").append(user.getRoleId())
                  .append(" | Status: ").append(user.getStatus())
                  .append("\n");
        }

        return result.toString();
    }

    /**
     * UC11: Retrieve a single user by their ID.
     * Used for viewing account details.
     */
    public String getUserById(int adminId, int targetUserId) {
        // Permission check
        if (!userService.isAdmin(adminId)) {
            return "ERROR: Access denied. You do not have admin permissions.";
        }

        User user = userService.getUserById(targetUserId);

        if (user == null) {
            return "ERROR: User with ID " + targetUserId + " not found.";
        }

        return "User Details:\n"
                + "ID: " + user.getUserId() + "\n"
                + "Name: " + user.getFullName() + "\n"
                + "Email: " + user.getEmail() + "\n"
                + "Phone: " + user.getPhoneNumber() + "\n"
                + "Role ID: " + user.getRoleId() + "\n"
                + "Status: " + user.getStatus();
    }

    /**
     * UC11: Update a user's status (e.g., Active → Suspended).
     * Used for account management actions.
     */
    public String updateUserStatus(int adminId, int targetUserId, String newStatus) {
        // Permission check
        if (!userService.isAdmin(adminId)) {
            return "ERROR: Access denied. You do not have admin permissions.";
        }

        boolean updated = userService.updateUserStatus(targetUserId, newStatus);

        if (!updated) {
            return "ERROR: Could not update status. Please try again.";
        }

        return "SUCCESS: User " + targetUserId + " status updated to " + newStatus + ".";
    }
}