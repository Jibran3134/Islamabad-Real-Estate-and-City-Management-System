package com.myapp.service;

import com.myapp.model.User;
import com.myapp.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;


/**
 * UserService contains all business logic for user account management.
 *
 * Use Cases:
 *   UC11 – Managing User Accounts
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    // ──────────────────────────────────────────────────────
    // UC11 – Managing User Accounts
    // ──────────────────────────────────────────────────────

    /**
     * UC11: Check whether a user has the Admin role.
     * Role ID 4 = Admin (from the database Role table).
     */
    public boolean isAdmin(int userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            return false;
        }

        // Role ID 4 is Admin as defined in the Role table
        return user.getRoleId() == 4;
    }

    /**
     * UC11: Retrieve all user records from the database.
     * Called when admin opens the user management dashboard.
     */
    public List<User> getAllUsers() {
        return userRepository.findAllUsers();
    }

    /**
     * UC11: Retrieve a single user by their ID.
     * Used when admin wants to view one account's full details.
     */
    public User getUserById(int userId) {
        return userRepository.findById(userId);
    }

    /**
     * UC11: Update a user's account status (e.g., Active, Suspended, Inactive).
     */
    public boolean updateUserStatus(int userId, String newStatus) {
        return userRepository.updateStatus(userId, newStatus);
    }
}