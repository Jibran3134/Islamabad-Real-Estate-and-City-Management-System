package com.myapp;

import com.myapp.repository.DatabaseConnection;
import com.myapp.controller.SectorController;
import com.myapp.model.Sector;
import java.sql.SQLException;
import java.util.List;

public class MainApplication {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  Islamabad Real Estate & City Management System          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        
        // Test database connection
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            db.getConnection();
            System.out.println("[SUCCESS] Database connection established!");
            
            // Test sector controller
            SectorController controller = new SectorController(1);
            List<Sector> sectors = controller.getAllSectors();
            System.out.println("[SUCCESS] Loaded " + sectors.size() + " sectors");
            
            for (Sector s : sectors) {
                System.out.println("  - " + s.getSectorName() + ": " + s.getCapacityLimit());
            }
            
        } catch (SQLException e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
