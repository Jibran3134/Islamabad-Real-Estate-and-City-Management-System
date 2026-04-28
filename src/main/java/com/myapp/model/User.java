package com.myapp.model;

/**
 * MODEL: Represents a system user (Administrator, Agent, Buyer, or Authority).
 * Maps to the 'Users' table in the database.
 *
 * Used in:
 *   UC11 – Managing User Accounts (admin views/manages user records)
 *   UC3  – Add Property Listing (agent identity)
 *   UC6  – Place Bid on Property (buyer identity)
 *
 * Status values: Active | Inactive | Suspended
 * Role is determined by roleId FK → Role table
 */
public class User {

    private int userId;
    private int roleId;                // FK → Role.role_id (1=Authority, 2=Agent, 3=Buyer, 4=Admin)
    private String fullName;
    private String email;
    private String passwordHash;
    private String status;             // Active | Inactive | Suspended
    private String phoneNumber;

    // ── Constructors ──────────────────────────────────────────────────

    public User() {}

    public User(int roleId, String fullName, String email, String passwordHash,
                String status, String phoneNumber) {
        this.roleId = roleId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.phoneNumber = phoneNumber;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", roleId=" + roleId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
