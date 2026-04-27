package com.myapp;

import com.myapp.controller.UserController;
import com.myapp.controller.PropertyController;
import com.myapp.controller.BidController;
import com.myapp.model.User;
import com.myapp.repository.DatabaseConnection;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Main entry point for the Islamabad Real Estate and City Management System.
 * Initializes MVC components and handles the main application loop.
 *
 * Architecture: MVC (Model-View-Controller)
 * ──────────────────────────────────────────
 * View       → frontend/src/ (HTML/CSS/JS pages)
 * Controller → com.myapp.controller (UserController, PropertyController, BidController)
 * Model      → com.myapp.model + com.myapp.service + com.myapp.repository
 */
public class MainApplication {

    // ── MVC Controllers ──────────────────────────────────────────────
    private static UserController userController;
    private static PropertyController propertyController;
    private static BidController bidController;

    // ── Current session ──────────────────────────────────────────────
    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  Islamabad Real Estate & City Management System         ║");
        System.out.println("║  Architecture: MVC (Model-View-Controller)              ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            // ── Initialize MVC Controllers ───────────────────────────
            System.out.println("Initializing application...");
            userController = new UserController();
            propertyController = new PropertyController();
            bidController = new BidController();
            System.out.println(" All controllers initialized.\n");

            // ── Start the application loop ───────────────────────────
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                if (currentUser == null) {
            //        showMainMenu();
                } else {
             //       showDashboardMenu();
                }

                System.out.print("\nEnter choice: ");
                String choice = scanner.nextLine().trim();

                if (currentUser == null) {
              //      running = handleMainMenu(choice, scanner);
                } else {
              //      handleDashboardMenu(choice, scanner);
                }
            }

            scanner.close();

        } catch (SQLException e) {
            System.err.println("❌ Failed to start application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
            System.out.println("\nApplication shut down.");
        }
    }

    // ── Main Menu (Not Logged In) ─────────────────────────────────────

    private static void showMainMenu() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  MAIN MENU");
        System.out.println("═══════════════════════════════════════");
        System.out.println("  1. Login");
        System.out.println("  2. Register");
        System.out.println("  3. View Properties");
        System.out.println("  0. Exit");
    }

    private static boolean handleMainMenu(String choice, Scanner scanner) {
        switch (choice) {
            case "1" -> handleLogin(scanner);
            case "2" -> handleRegister(scanner);
            case "3" -> viewProperties();
            case "0" -> { return false; }
            default  -> System.out.println("Invalid choice.");
        }
        return true;
    }

    // ── Dashboard Menu (Logged In) ────────────────────────────────────

    private static void showDashboardMenu() {
        String dashboard = userController.getDashboardView(currentUser);
        System.out.println("═══════════════════════════════════════");
        System.out.println("  " + dashboard.toUpperCase() + "  |  Welcome, " + currentUser.getFullName());
        System.out.println("═══════════════════════════════════════");
        System.out.println("  1. View Properties");
        System.out.println("  2. Search Properties");

        switch (currentUser.getRole()) {
            case "agent" -> {
                System.out.println("  3. Add New Property");
                System.out.println("  4. My Listings");
            }
            case "buyer" -> {
                System.out.println("  3. Place a Bid");
                System.out.println("  4. My Bids");
            }
            case "admin" -> {
                System.out.println("  3. Manage Users");
                System.out.println("  4. All Listings");
            }
            case "authority" -> {
                System.out.println("  3. Manage Zones");
                System.out.println("  4. View All Transactions");
            }
        }

        System.out.println("  0. Logout");
    }

    private static void handleDashboardMenu(String choice, Scanner scanner) {
        switch (choice) {
            case "1" -> viewProperties();
            case "2" -> searchProperties(scanner);
            case "0" -> {
                currentUser = null;
                System.out.println("Logged out successfully.\n");
            }
            default -> System.out.println("Feature coming soon...");
        }
    }

    // ── Actions ───────────────────────────────────────────────────────

    private static void handleLogin(Scanner scanner) {
        System.out.println("\n── LOGIN ──────────────────────────────");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = userController.handleLogin(email, password);
        if (user != null) {
            currentUser = user;
            System.out.println("✅ Welcome, " + user.getFullName() + "! Role: " + user.getRole());
        } else {
            System.out.println("❌ Invalid email or password.");
        }
    }

    private static void handleRegister(Scanner scanner) {
        System.out.println("\n── REGISTER ───────────────────────────");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Role (buyer/agent): ");
        String role = scanner.nextLine().trim();

        String result = userController.handleRegister(name, email, password, phone, role);
        System.out.println(result);
    }

    private static void viewProperties() {
        System.out.println("\n── AVAILABLE PROPERTIES ────────────────");
        var properties = propertyController.getAvailableProperties();
        if (properties.isEmpty()) {
            System.out.println("No properties available at the moment.");
        } else {
            for (var p : properties) {
                System.out.printf("  [%d] %s | %s | %s | Rs. %s%n",
                        p.getPropertyId(), p.getTitle(), p.getPropertyType(),
                        p.getSector(), p.getPrice());
            }
        }
    }

    private static void searchProperties(Scanner scanner) {
        System.out.println("\n── SEARCH PROPERTIES ──────────────────");
        System.out.print("Sector (or leave blank): ");
        String sector = scanner.nextLine().trim();
        System.out.print("Type (house/apartment/plot/commercial/farmhouse or blank): ");
        String type = scanner.nextLine().trim();

        var results = propertyController.handleSearch(
                sector.isEmpty() ? null : sector,
                type.isEmpty() ? null : type
        );

        if (results.isEmpty()) {
            System.out.println("No properties found matching your criteria.");
        } else {
            System.out.println("Found " + results.size() + " properties:");
            for (var p : results) {
                System.out.printf("  [%d] %s | %s | %s | Rs. %s%n",
                        p.getPropertyId(), p.getTitle(), p.getPropertyType(),
                        p.getSector(), p.getPrice());
            }
        }
    }
}
