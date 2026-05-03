package com.myapp.controller;

import com.myapp.launcher.JavaFXLauncher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Dashboard Controller - Role-based menu system
 * Shows different menu options based on user role
 */
public class DashboardController implements Initializable {

    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Label welcomeLabel;
    @FXML private FlowPane menuGrid;

    private int currentUserId;
    private String currentRole;
    private String currentName;

    // Static fields so other controllers (e.g. BiddingDashboard) can access the current user's role
    private static String activeUserRole = "buyer";
    private static int activeUserId = 0;

    public static String getActiveUserRole() { return activeUserRole; }
    public static int getActiveUserId() { return activeUserId; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Will be called after FXML loads
    }

    public void setUserInfo(int userId, String role, String name) {
        this.currentUserId = userId;
        this.currentRole = role;
        this.currentName = name;

        // Update static fields for cross-controller access
        activeUserRole = role.toLowerCase();
        activeUserId = userId;

        userLabel.setText("Welcome, " + name);
        roleLabel.setText(role.toUpperCase());
        welcomeLabel.setText("Hello, " + name + "!");

        loadMenuButtons();
    }

    private void loadMenuButtons() {
        menuGrid.getChildren().clear();

        switch (currentRole.toLowerCase()) {
            case "admin":
                // Admin dashboard is empty — dashboards will be created later
                Label comingSoon = new Label("Admin dashboards coming soon...");
                comingSoon.setStyle("-fx-text-fill: #999999; -fx-font-size: 18px; -fx-font-style: italic;");
                menuGrid.getChildren().add(comingSoon);
                break;
            case "authority":
                addMenuButton("User Management", "Manage all user accounts (UC11)", "/fxml/UserManagement.fxml");
                addMenuButton("Sector Management", "Define capacity limits (UC1)", "/fxml/SectorManagement.fxml");
                addMenuButton("Sector Dashboard", "Freeze overloaded sectors (UC2)", "/fxml/SectorDashboard.fxml");
                addMenuButton("Add Property Listing", "List a new property for sale (UC3)", "/fxml/AddPropertyListing.fxml");
                break;
            case "agent":
                addMenuButton("Bidding Dashboard", "Manage bidding sessions (UC6-9)", "/fxml/BiddingDashboard.fxml");
                break;
            case "buyer":
            default:
                addMenuButton("Property Search", "Find properties with ranking (UC4)", "/fxml/PropertySearch.fxml");
                addMenuButton("Bidding Dashboard", "Participate in auctions (UC6-9)", "/fxml/BiddingDashboard.fxml");
                break;
        }
    }

    private void addMenuButton(String title, String description, String fxmlPath) {
        VBox card = new VBox(12);
        card.setPrefSize(320, 150);
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-border-color: #D4AF37; " +
            "-fx-border-width: 3; -fx-border-radius: 16; -fx-background-radius: 16; " +
            "-fx-padding: 28; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #1A1A1A; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 14px;");
        descLabel.setWrapText(true);

        // Gold accent line
        javafx.scene.layout.Region accent = new javafx.scene.layout.Region();
        accent.setPrefHeight(3);
        accent.setMaxWidth(60);
        accent.setStyle("-fx-background-color: #D4AF37; -fx-background-radius: 2;");

        card.getChildren().addAll(titleLabel, accent, descLabel);

        // Hover effect — gold glow
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #FFF8E1; -fx-border-color: #F5D76E; " +
            "-fx-border-width: 3; -fx-border-radius: 16; -fx-background-radius: 16; " +
            "-fx-padding: 28; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.6), 25, 0, 0, 0);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-border-color: #D4AF37; " +
            "-fx-border-width: 3; -fx-border-radius: 16; -fx-background-radius: 16; " +
            "-fx-padding: 28; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);"
        ));

        card.setOnMouseClicked(e -> openScreen(fxmlPath, title));

        menuGrid.getChildren().add(card);
    }

    private void openScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title + " - IRMS");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot open screen");
            alert.setContentText("Failed to load: " + fxmlPath + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        JavaFXLauncher.showLoginScreen();
    }
}
