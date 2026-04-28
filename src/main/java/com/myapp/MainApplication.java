package com.myapp;

import com.myapp.repository.DatabaseConnection;
import com.myapp.controller.SectorController;
import com.myapp.controller.PropertyController;
import com.myapp.controller.PropertySearchController;
import com.myapp.model.Property;
import com.myapp.model.Sector;
import com.myapp.model.SearchCriteria;
import com.myapp.service.SectorService.UpdateResult;
import com.myapp.service.SectorService.FreezeResult;
import com.myapp.service.PropertyService.AddPropertyResult;
import com.myapp.service.PropertySearchService.SearchResult;
import com.myapp.service.FilterValidator.ValidationResult;
import com.myapp.repository.SectorRepository.SectorStatistics;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application - Console-based interface for IRMS
 * Provides interactive menu for UC1, UC2, and UC3
 */
public class MainApplication {
    
    private static SectorController sectorController;
    private static PropertyController propertyController;
    private static PropertySearchController searchController;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  Islamabad Real Estate & City Management System          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        
        // Test database connection
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            db.getConnection();
            System.out.println("[SUCCESS] Database connection established!\n");
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Database connection failed: " + e.getMessage());
            System.err.println("Please check your SQL Server and try again.");
            return;
        }
        
        // Initialize controllers
        sectorController = new SectorController(1);     // Authority ID = 1
        propertyController = new PropertyController(1); // Agent ID = 1
        searchController = new PropertySearchController(); // UC4 search
        scanner = new Scanner(System.in);
        
        // Main menu loop
        boolean running = true;
        while (running) {
            printMainMenu();
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleViewAllSectors();
                    break;
                case "2":
                    handleDefineSectorCapacity();  // UC1
                    break;
                case "3":
                    handleViewSectorDashboard();   // UC2 - SD1
                    break;
                case "4":
                    handleFreezeSector();           // UC2 - Main flow
                    break;
                case "5":
                    handleAddPropertyListing();     // UC3 - Main flow
                    break;
                case "6":
                    handleSearchProperties();        // UC4 - Search
                    break;
                case "0":
                    running = false;
                    System.out.println("\n[SYSTEM] Shutting down... Goodbye!");
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
    }
    
    // ==================== MENU ====================
    
    private static void printMainMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║          MAIN MENU                    ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  1. View All Sectors                  ║");
        System.out.println("║  2. Define Sector Capacity (UC1)      ║");
        System.out.println("║  3. Sector Dashboard (UC2)            ║");
        System.out.println("║  4. Freeze Overloaded Sector (UC2)    ║");
        System.out.println("║  5. Add Property Listing (UC3)        ║");
        System.out.println("║  6. Search Properties (UC4)           ║");
        System.out.println("║  0. Exit                              ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Enter choice: ");
    }
    
    // ==================== UC1: DEFINE SECTOR CAPACITY ====================
    
    /**
     * View all sectors — UC1 Step 1 (SD1: displaySectorList)
     */
    private static void handleViewAllSectors() {
        try {
            List<Sector> sectors = sectorController.getAllSectors();
            System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
            System.out.println("║                     ALL SECTORS                                   ║");
            System.out.println("╠══════╦═══════════════════════╦══════════╦═══════════╦══════════════╣");
            System.out.printf("║ %-4s ║ %-21s ║ %-8s ║ %-9s ║ %-12s ║%n", "ID", "Name", "Limit", "Count", "Status");
            System.out.println("╠══════╬═══════════════════════╬══════════╬═══════════╬══════════════╣");
            
            for (Sector s : sectors) {
                System.out.printf("║ %-4d ║ %-21s ║ %-8d ║ %-9d ║ %-12s ║%n",
                    s.getSectorId(), s.getSectorName(), s.getCapacityLimit(),
                    s.getCurrentPropertyCount(), s.getStatus());
            }
            System.out.println("╚══════╩═══════════════════════╩══════════╩═══════════╩══════════════╝");
            System.out.println("Total sectors: " + sectors.size());
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load sectors: " + e.getMessage());
        }
    }
    
    /**
     * UC1 Main Flow - Define Sector Capacity
     */
    private static void handleDefineSectorCapacity() {
        System.out.println("\n═══ UC1: DEFINE SECTOR CAPACITY LIMITS ═══");
        handleViewAllSectors();
        
        System.out.print("\nEnter Sector ID: ");
        int sectorId;
        try {
            sectorId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid input. Please enter a number.");
            return;
        }
        
        try {
            Sector sector = sectorController.getSectorDetails(sectorId);
            if (sector == null) {
                System.out.println("[ERROR] Sector not found with ID: " + sectorId);
                return;
            }
            System.out.println("\nSelected: " + sector.getSectorName());
            System.out.println("Current Capacity Limit: " + sector.getCapacityLimit());
            System.out.println("Current Property Count: " + sector.getCurrentPropertyCount());
            System.out.println("Status: " + sector.getStatus());
        } catch (SQLException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return;
        }
        
        System.out.print("\nEnter New Capacity Limit: ");
        int newCapacity;
        try {
            newCapacity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid input. Please enter a number.");
            return;
        }
        
        System.out.print("Confirm update? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("[CANCELLED] Operation cancelled by user.");
            return;
        }
        
        try {
            UpdateResult result = sectorController.defineSectorCapacity(sectorId, newCapacity);
            if (result.isSuccess()) {
                System.out.println("[SUCCESS] " + result.getMessage());
            } else {
                System.out.println("[FAILED] " + result.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }
    
    // ==================== UC2: FREEZE OVERLOADED SECTOR ====================
    
    private static void handleViewSectorDashboard() {
        try {
            List<SectorStatistics> stats = sectorController.getSectorStatistics();
            
            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                    SECTOR DASHBOARD (UC2)                                 ║");
            System.out.println("╠══════╦═══════════════════════╦══════════╦═══════════╦══════════╦═══════════╣");
            System.out.printf("║ %-4s ║ %-21s ║ %-8s ║ %-9s ║ %-8s ║ %-9s ║%n", 
                "ID", "Name", "Limit", "Count", "Usage %", "Status");
            System.out.println("╠══════╬═══════════════════════╬══════════╬═══════════╬══════════╬═══════════╣");
            
            int overloadedCount = 0;
            int frozenCount = 0;
            
            for (SectorStatistics s : stats) {
                if (s.isFrozen()) frozenCount++;
                else if (s.isOverloaded()) overloadedCount++;
                
                System.out.printf("║ %-4d ║ %-21s ║ %-8d ║ %-9d ║ %6.1f%% ║ %-9s ║%n",
                    s.sectorId, s.sectorName, s.capacityLimit, s.currentCount,
                    s.usagePercentage, s.status);
            }
            
            System.out.println("╚══════╩═══════════════════════╩══════════╩═══════════╩══════════╩═══════════╝");
            System.out.println("Total: " + stats.size() + " sectors | Overloaded: " + overloadedCount + " | Frozen: " + frozenCount);
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load dashboard: " + e.getMessage());
        }
    }
    
    private static void handleFreezeSector() {
        System.out.println("\n═══ UC2: FREEZE OVERLOADED SECTOR ═══");
        handleViewSectorDashboard();
        
        System.out.print("\nEnter Sector ID to freeze: ");
        int sectorId;
        try {
            sectorId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid input. Please enter a number.");
            return;
        }
        
        try {
            Sector sector = sectorController.getSectorDetails(sectorId);
            if (sector == null) {
                System.out.println("[ERROR] Sector not found with ID: " + sectorId);
                return;
            }
            
            System.out.println("\nSelected: " + sector.getSectorName());
            System.out.println("Status: " + sector.getStatus());
            System.out.println("Usage: " + sector.getCurrentPropertyCount() + "/" + sector.getCapacityLimit());
            System.out.println("Overloaded: " + (sector.isOverloaded() ? "YES" : "NO"));
        } catch (SQLException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return;
        }
        
        try {
            FreezeResult result = sectorController.freezeSector(sectorId, false);
            
            if (result.isSuccess()) {
                System.out.println("[SUCCESS] " + result.getMessage());
                return;
            }
            
            if (result.needsOverride()) {
                System.out.println("[WARNING] " + result.getMessage());
                System.out.print("Override and freeze anyway? (yes/no): ");
                String override = scanner.nextLine().trim().toLowerCase();
                
                if (override.equals("yes") || override.equals("y")) {
                    System.out.print("Final confirmation - Freeze? (yes/no): ");
                    String finalConfirm = scanner.nextLine().trim().toLowerCase();
                    if (!finalConfirm.equals("yes") && !finalConfirm.equals("y")) {
                        System.out.println("[CANCELLED] Freeze cancelled.");
                        return;
                    }
                    
                    FreezeResult overrideResult = sectorController.freezeSector(sectorId, true);
                    System.out.println(overrideResult.isSuccess() ? 
                        "[SUCCESS] " + overrideResult.getMessage() : 
                        "[FAILED] " + overrideResult.getMessage());
                } else {
                    System.out.println("[CANCELLED] Freeze cancelled.");
                }
            } else {
                System.out.println("[FAILED] " + result.getMessage());
            }
            
        } catch (SQLException e) {
            System.err.println("[SYSTEM ERROR] " + e.getMessage());
        }
    }
    
    // ==================== UC3: ADD PROPERTY LISTING ====================
    
    /**
     * UC3 Main Flow - Add Property Listing
     * Maps to Extended UC3 Main Success Scenario:
     *   1. Agent opens add listing form → this menu
     *   2. Agent enters property details → title, description, location, price, type
     *   3. Agent selects sector → enter sector ID
     *   4. Agent selects selling method → Fixed Price / Bidding
     *   5. System validates data → PropertyValidator.validate()
     *   6. System checks sector availability → isSectorAvailableForListing()
     *   7. System saves listing → PropertyRepository.savePropertyWithTransaction()
     *   8. System logs action → AuditLogRepository
     *   9. System displays confirmation → success message
     */
    private static void handleAddPropertyListing() {
        System.out.println("\n═══ UC3: ADD PROPERTY LISTING ═══");
        
        // Step 1: Show available sectors (not frozen)
        handleViewAllSectors();
        
        // Step 2: Enter property details
        System.out.println("\n--- Enter Property Details ---");
        
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("[ERROR] Title cannot be empty.");
            return;
        }
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        System.out.print("Location (street/area): ");
        String location = scanner.nextLine().trim();
        if (location.isEmpty()) {
            System.out.println("[ERROR] Location cannot be empty.");
            return;
        }
        
        System.out.print("Price (PKR): ");
        BigDecimal price;
        try {
            price = new BigDecimal(scanner.nextLine().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("[ERROR] Price must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid price. Please enter a valid number.");
            return;
        }
        
        System.out.print("Property Type (Residential/Commercial/Industrial): ");
        String propertyType = scanner.nextLine().trim();
        
        // Step 3: Select sector
        System.out.print("Sector ID: ");
        int sectorId;
        try {
            sectorId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid sector ID.");
            return;
        }
        
        // Step 4: Select selling method
        System.out.print("Selling Method (Fixed Price / Bidding): ");
        String sellingMethod = scanner.nextLine().trim();
        if (sellingMethod.isEmpty()) sellingMethod = "Fixed Price";
        
        // Step 5: Confirm
        System.out.println("\n--- Review Your Listing ---");
        System.out.println("Title:          " + title);
        System.out.println("Description:    " + description);
        System.out.println("Location:       " + location);
        System.out.println("Price:          PKR " + price);
        System.out.println("Type:           " + propertyType);
        System.out.println("Sector ID:      " + sectorId);
        System.out.println("Selling Method: " + sellingMethod);
        
        System.out.print("\nSubmit this listing? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("[CANCELLED] Listing cancelled by user.");
            return;
        }
        
        // Step 6: Execute (no images in console mode)
        try {
            AddPropertyResult result = propertyController.addPropertyListing(
                title, description, location, price, propertyType, sellingMethod,
                sectorId, new ArrayList<>(), new ArrayList<>());
            
            if (result.isSuccess()) {
                System.out.println("[SUCCESS] " + result.getMessage());
            } else {
                System.out.println("[FAILED] " + result.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("[SYSTEM ERROR] " + e.getMessage());
            System.err.println("Transaction has been rolled back. Please try again.");
        }
    }
    
    // ==================== UC4: SEARCH PROPERTIES (NON-CRUD with RANKING) ====================
    
    /**
     * UC4 Main Flow - Search Properties with Simple Ranking (NON-CRUD)
     * Maps to Extended UC4 Main Success Scenario:
     *   1. Buyer opens search screen -> this menu
     *   2. Buyer enters filter values -> location, price range, type, sector
     *   3. System validates filters -> FilterValidator.validate()
     *   4. System applies Strategy filters -> PriceRange/Location/PropertyType/Sector
     *   5. System calculates relevance scores -> SimpleRankingService (NON-CRUD)
     *   6. System ranks results by score -> sorted highest to lowest
     *   7. System displays ranked results -> formatted table with rank + score
     */
    private static void handleSearchProperties() {
        System.out.println("\n=== UC4: SEARCH PROPERTIES (with RANKING - NON-CRUD) ===");
        
        SearchCriteria criteria = new SearchCriteria();
        
        // Step 1: Enter filters (all optional - press Enter to skip)
        System.out.println("\n--- Enter Search Filters (press Enter to skip) ---");
        
        // Location filter
        System.out.print("Location (e.g., F-7, G-10): ");
        String location = scanner.nextLine().trim();
        if (!location.isEmpty()) {
            criteria.setLocation(location);
        }
        
        // Min price filter
        System.out.print("Min Price (PKR): ");
        String minPriceStr = scanner.nextLine().trim();
        if (!minPriceStr.isEmpty()) {
            try {
                criteria.setMinPrice(new BigDecimal(minPriceStr));
            } catch (NumberFormatException e) {
                System.out.println("[WARNING] Invalid min price, skipping.");
            }
        }
        
        // Max price filter
        System.out.print("Max Price (PKR): ");
        String maxPriceStr = scanner.nextLine().trim();
        if (!maxPriceStr.isEmpty()) {
            try {
                criteria.setMaxPrice(new BigDecimal(maxPriceStr));
            } catch (NumberFormatException e) {
                System.out.println("[WARNING] Invalid max price, skipping.");
            }
        }
        
        // Property type filter
        System.out.print("Property Type (Residential/Commercial/Industrial): ");
        String propertyType = scanner.nextLine().trim();
        if (!propertyType.isEmpty()) {
            criteria.setPropertyType(propertyType);
        }
        
        // Sector filter
        System.out.print("Sector ID (or press Enter for all): ");
        String sectorStr = scanner.nextLine().trim();
        if (!sectorStr.isEmpty()) {
            try {
                criteria.setSectorId(Integer.parseInt(sectorStr));
            } catch (NumberFormatException e) {
                System.out.println("[WARNING] Invalid sector ID, skipping.");
            }
        }
        
        // Show applied filters
        System.out.println("\nApplied Filters: " + criteria);
        
        // Step 2: Validate filters (SD1 - FilterValidator)
        ValidationResult validation = searchController.validateFilters(criteria);
        if (!validation.isValid()) {
            System.out.println("[VALIDATION ERROR] " + validation.getMessage());
            return;
        }
        
        // Step 3: Execute search with ranking (NON-CRUD)
        System.out.println("Searching and ranking properties...");
        SearchResult result = searchController.submitSearchRequest(criteria);
        
        if (!result.isSuccess()) {
            System.out.println("[FAILED] " + result.getMessage());
            return;
        }
        
        // Step 4: Display ranked results
        List<Property> properties = result.getProperties();
        
        if (properties.isEmpty()) {
            System.out.println("[INFO] " + result.getMessage());
            return;
        }
        
        System.out.println("\n=============================== RANKED SEARCH RESULTS (NON-CRUD) ===============================");
        System.out.printf("%-6s %-8s %-26s %-15s %-15s %-10s%n", "RANK", "SCORE", "TITLE", "PRICE (PKR)", "TYPE", "SECTOR");
        System.out.println("------------------------------------------------------------------------------------------------");
        
        for (Property p : properties) {
            String title = p.getTitle();
            if (title != null && title.length() > 24) title = title.substring(0, 21) + "...";
            
            String rankIcon = "";
            if (p.getRankPosition() == 1) rankIcon = " #1";
            else if (p.getRankPosition() == 2) rankIcon = " #2";
            else if (p.getRankPosition() == 3) rankIcon = " #3";
            else rankIcon = " #" + p.getRankPosition();
            
            System.out.printf("%-6s %-8s %-26s %-15s %-15s %-10d%n",
                rankIcon,
                p.getRelevanceScore() + "/40",
                title != null ? title : "N/A",
                p.getPrice() != null ? p.getPrice().toString() : "N/A",
                p.getPropertyType() != null ? p.getPropertyType() : "N/A",
                p.getSectorId());
        }
        
        System.out.println("================================================================================================");
        System.out.println(result.getMessage() + " (Search took: " + result.getSearchTimeMs() + "ms)");
        
        // Show ranking explanation for top result
        if (result.getRankingInfo() != null) {
            System.out.println("\n--- Top Result Ranking Explanation ---");
            System.out.println(result.getRankingInfo());
        }
        
        // Show ranking formula
        System.out.println("\n--- Ranking Formula (NON-CRUD) ---");
        System.out.println("Score = (Matched Filters) x 10");
        System.out.println("Location match: +10 | Price match: +10 | Type match: +10 | Sector match: +10");
        System.out.println("Max possible score: 40/40");
        
        // Special Requirement: Search within 3 seconds check
        if (result.getSearchTimeMs() > 3000) {
            System.out.println("[WARNING] Search took " + result.getSearchTimeMs() + "ms (exceeds 3 second requirement)");
        }
    }
}
