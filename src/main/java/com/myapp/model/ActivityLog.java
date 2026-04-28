package com.myapp.model;

import java.time.LocalDateTime;

/**
 * MODEL: Represents an audit/activity log entry for administrative actions.
 * Maps to the 'Activity_Log' table in the database.
 *
 * Used in:
 *   UC1 – Define Sector Capacity Limits (logs administrative update)
 *   UC2 – Freeze Overloaded Sector (logs regulatory action)
 *   UC11 – Managing User Accounts (logs admin viewing activity)
 */
public class ActivityLog {

    private int logId;
    private int userId;              // FK → Users.user_id — who performed the action
    private String action;           // e.g., "UPDATE_CAPACITY", "FREEZE_SECTOR", "VIEW_USERS"
    private String entityType;       // e.g., "Sector", "Property", "User"
    private int entityId;            // ID of the affected entity
    private LocalDateTime timestamp;
    private String details;          // additional context or description

    // ── Constructors ──────────────────────────────────────────────────

    public ActivityLog() {}

    public ActivityLog(int userId, String action, String entityType, int entityId, String details) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public int getEntityId() { return entityId; }
    public void setEntityId(int entityId) { this.entityId = entityId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", timestamp=" + timestamp +
                '}';
    }
}
