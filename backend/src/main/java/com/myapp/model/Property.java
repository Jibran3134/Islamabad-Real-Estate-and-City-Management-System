package com.myapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL: Represents a property listing in the system.
 * Maps to the 'properties' table in the database.
 *
 * Types: house, apartment, plot, commercial, farmhouse
 * Status: available, sold, pending, auction
 */
public class Property {

    private int propertyId;
    private String title;
    private String description;
    private String propertyType;   // house | apartment | plot | commercial | farmhouse
    private String sector;
    private String address;
    private String city;
    private double areaSqft;
    private BigDecimal price;
    private int bedrooms;
    private int bathrooms;
    private String status;         // available | sold | pending | auction
    private int listedBy;          // FK → users.user_id
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    public Property() {
        this.city = "Islamabad";
        this.status = "available";
    }

    public Property(String title, String description, String propertyType, String sector,
                    String address, double areaSqft, BigDecimal price, int bedrooms,
                    int bathrooms, int listedBy) {
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.sector = sector;
        this.address = address;
        this.city = "Islamabad";
        this.areaSqft = areaSqft;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.status = "available";
        this.listedBy = listedBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getAreaSqft() { return areaSqft; }
    public void setAreaSqft(double areaSqft) { this.areaSqft = areaSqft; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getListedBy() { return listedBy; }
    public void setListedBy(int listedBy) { this.listedBy = listedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Property{" +
                "propertyId=" + propertyId +
                ", title='" + title + '\'' +
                ", type='" + propertyType + '\'' +
                ", sector='" + sector + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                '}';
    }
}
