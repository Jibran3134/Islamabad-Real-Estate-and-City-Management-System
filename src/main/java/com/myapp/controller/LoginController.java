package com.myapp.controller;

import com.myapp.launcher.JavaFXLauncher;
import com.myapp.model.User;
import com.myapp.service.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Login Screen Controller
 * Authenticates user and navigates to role-based Dashboard
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        roleCombo.setItems(FXCollections.observableArrayList(
            "Buyer", "Agent", "Authority", "Admin"
        ));
        roleCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleCombo.getValue();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        // Authenticate using existing UserService
        // For demo: accept any credentials with valid email format
        if (!email.contains("@")) {
            showError("Please enter a valid email address.");
            return;
        }

        // Extract display name from email
        String name = email.split("@")[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        int userId = 1; // Default user ID for demo

        // Navigate to Dashboard with role
        JavaFXLauncher.showDashboard(userId, role, name);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
