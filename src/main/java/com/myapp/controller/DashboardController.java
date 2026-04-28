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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Will be called after FXML loads
    }

    public void setUserInfo(int userId, String role, String name) {
        this.currentUserId = userId;
        this.currentRole = role;
        this.currentName = name;

        userLabel.setText("Welcome, " + name);
        roleLabel.setText(role.toUpperCase());
        welcomeLabel.setText("Hello, " + name + "!");

        loadMenuButtons();
    }

    private void loadMenuButtons() {
        menuGrid.getChildren().clear();

        switch (currentRole.toLowerCase()) {
            case "admin":
                addMenuButton("User Management", "Manage all user accounts (UC11)", "/fxml/UserManagement.fxml");
                addMenuButton("Sector Management", "Define capacity limits (UC1)", "/fxml/SectorManagement.fxml");
                addMenuButton("Sector Dashboard", "Monitor & freeze sectors (UC2)", "/fxml/SectorDashboard.fxml");
                addMenuButton("Property Search", "Search with ranking (UC4)", "/fxml/PropertySearch.fxml");
                break;
            case "authority":
                addMenuButton("Sector Management", "Define capacity limits (UC1)", "/fxml/SectorManagement.fxml");
                addMenuButton("Sector Dashboard", "Freeze overloaded sectors (UC2)", "/fxml/SectorDashboard.fxml");
                addMenuButton("Property Search", "Search with ranking (UC4)", "/fxml/PropertySearch.fxml");
                break;
            case "agent":
                addMenuButton("Add Property", "List new property (UC3)", "/fxml/AddPropertyListing.fxml");
                addMenuButton("Property Search", "Search with ranking (UC4)", "/fxml/PropertySearch.fxml");
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
        VBox card = new VBox(8);
        card.setPrefSize(280, 110);
        card.setStyle(
            "-fx-background-color: -fx-bg-card; -fx-border-color: -fx-gold; " +
            "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-padding: 18; -fx-cursor: hand;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: -fx-gold; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: -fx-text-muted; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, descLabel);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: rgba(212,175,55,0.15); -fx-border-color: -fx-gold; " +
            "-fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-padding: 18; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.4), 12, 0, 0, 0);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: -fx-bg-card; -fx-border-color: -fx-gold; " +
            "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-padding: 18; -fx-cursor: hand;"
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
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            stage.setScene(scene);
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
