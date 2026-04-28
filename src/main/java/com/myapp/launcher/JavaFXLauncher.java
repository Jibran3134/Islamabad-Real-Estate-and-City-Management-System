package com.myapp.launcher;

import com.myapp.controller.DashboardController;
import com.myapp.repository.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Main JavaFX Application Launcher
 * Pure JavaFX - No HTML/CSS/JS framework
 * Theme: Black + White + Gold
 */
public class JavaFXLauncher extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Test database connection
        try {
            DatabaseConnection.getInstance().getConnection();
            System.out.println("[INFO] Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("[ERROR] Database connection failed: " + e.getMessage());
            showErrorAndExit("Cannot connect to database.\n" + e.getMessage());
            return;
        }

        // Load Login screen
        showLoginScreen();

        // Configure stage
        stage.setTitle("Islamabad Real Estate Management System");
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    public static void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                JavaFXLauncher.class.getResource("/fxml/Login.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(
                JavaFXLauncher.class.getResource("/css/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.setTitle("Login - IRMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDashboard(int userId, String role, String name) {
        try {
            FXMLLoader loader = new FXMLLoader(
                JavaFXLauncher.class.getResource("/fxml/Dashboard.fxml")
            );
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserInfo(userId, role, name);

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(
                JavaFXLauncher.class.getResource("/css/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.setTitle("Dashboard - " + name + " (" + role + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private void showErrorAndExit(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Cannot connect to database");
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
