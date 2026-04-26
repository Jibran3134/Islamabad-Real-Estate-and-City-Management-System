package com.myapp.model;

import java.time.LocalDateTime;

/**
 * MODEL: Represents a city zone/sector managed by the authority.
 * Maps to the 'city_zones' table in the database.
 *
 * Zone types: residential, commercial, industrial, green, mixed
 */
public class CityZone {

    private int zoneId;
    private String zoneName;
    private String zoneType;       // residential | commercial | industrial | green | mixed
    private String description;
    private int managedBy;         // FK → users.user_id (authority role)
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────

    public CityZone() {}

    public CityZone(String zoneName, String zoneType, String description, int managedBy) {
        this.zoneName = zoneName;
        this.zoneType = zoneType;
        this.description = description;
        this.managedBy = managedBy;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getZoneId() { return zoneId; }
    public void setZoneId(int zoneId) { this.zoneId = zoneId; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public String getZoneType() { return zoneType; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getManagedBy() { return managedBy; }
    public void setManagedBy(int managedBy) { this.managedBy = managedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "CityZone{" +
                "zoneId=" + zoneId +
                ", zoneName='" + zoneName + '\'' +
                ", zoneType='" + zoneType + '\'' +
                '}';
    }
}
