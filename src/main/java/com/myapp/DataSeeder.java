package com.myapp;

import com.myapp.model.Property;
import com.myapp.model.Sector;
import com.myapp.repository.DatabaseConnection;
import com.myapp.repository.PropertyRepository;
import com.myapp.controller.SectorController;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class DataSeeder {
    public static void main(String[] args) {
        try {
            // First, clear existing properties to avoid clutter and ensure unique test data
            clearExistingProperties();

            SectorController sc = new SectorController(0);
            List<Sector> allSectors = sc.getAllSectors();
            List<Sector> isbSectors = new java.util.ArrayList<>();
            for (Sector s : allSectors) {
                String name = s.getSectorName().toUpperCase();
                if (!name.contains("DHA") && !name.contains("BAHRIA") && !name.contains("TOWN") && !name.contains("ENCLAVE")) {
                    isbSectors.add(s);
                }
            }

            if (isbSectors.isEmpty()) {
                System.out.println("No Islamabad sectors found.");
                return;
            }

            PropertyRepository repo = new PropertyRepository();
            
            String[] residentialTitles = {
                "A beautiful new house of two bedrooms with modern kitchen",
                "Luxury 5-bedroom villa with swimming pool and garden",
                "Cozy 1-bedroom apartment near main market",
                "Newly renovated 3-bedroom family home",
                "Spacious penthouse with stunning city views"
            };
            
            String[] commercialTitles = {
                "Spacious commercial plaza for rent with basement parking",
                "Prime location shop in main markaz",
                "Modern office space with glass facade",
                "Corner commercial plot ready for construction",
                "Fully furnished co-working space"
            };
            
            String[] industrialTitles = {
                "Large industrial warehouse with heavy machinery access",
                "Factory building with high-power electrical setup",
                "Storage facility with loading docks",
                "Industrial plot in designated economic zone",
                "Cold storage warehouse for perishable goods"
            };

            System.out.println("Inserting 50 unique properties...");
            for (int i = 1; i <= 50; i++) {
                Sector sec = isbSectors.get(i % isbSectors.size());
                
                String type;
                String title;
                if (i <= 25) {
                    type = "Residential";
                    title = residentialTitles[i % residentialTitles.length] + " (Unit " + i + ")";
                } else if (i <= 40) {
                    type = "Commercial";
                    title = commercialTitles[i % commercialTitles.length] + " (Unit " + i + ")";
                } else {
                    type = "Industrial";
                    title = industrialTitles[i % industrialTitles.length] + " (Unit " + i + ")";
                }
                
                String street = "Street " + ((i * 3) % 50 + 1) + ", House " + (i * 10) + ", Main Boulevard";
                
                Property p = new Property();
                p.setSectorId(sec.getSectorId());
                p.setAgentId(1); // Assuming agent 1 exists
                p.setTitle(title);
                p.setDescription("Detailed property description for " + title + ". This property offers exceptional value in " + sec.getSectorName() + ".");
                // Price varies significantly
                p.setPrice(new BigDecimal(10000000 + (i * 1500000)));
                p.setPropertyType(type);
                p.setLocation(street + ", " + sec.getSectorName() + ", Islamabad");
                p.setListingStatus("For Sale");
                p.setSellingMethod(i % 2 == 0 ? "Fixed Price" : "Bidding");
                
                repo.insertProperty(p);
            }
            System.out.println("Successfully inserted 50 unique properties.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void clearExistingProperties() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Property")) {
            int deleted = stmt.executeUpdate();
            System.out.println("Cleared " + deleted + " existing properties.");
        } catch (SQLException e) {
            System.err.println("Could not clear properties (might be foreign key constraints): " + e.getMessage());
        }
    }
}
