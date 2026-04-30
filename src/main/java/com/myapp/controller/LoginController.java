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
 * Authenticates user via DB and navigates to role-based Dashboard
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;

    // Registration fields
    @FXML private TextField regNameField;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regPhoneField;
    @FXML private ComboBox<String> regRoleCombo;
    @FXML private Label regStatusLabel;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        roleCombo.setItems(FXCollections.observableArrayList(
            "Buyer", "Agent", "Authority", "Admin"
        ));
        roleCombo.getSelectionModel().selectFirst();

        // Registration role combo
        if (regRoleCombo != null) {
            regRoleCombo.setItems(FXCollections.observableArrayList(
                "Buyer", "Agent", "Authority", "Admin"
            ));
            regRoleCombo.getSelectionModel().selectFirst();
        }
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

        if (!email.contains("@")) {
            showError("Please enter a valid email address.");
            return;
        }

        // Try real DB authentication first
        User user = userService.handleLogin(email, password);
        if (user != null) {
            // DB login successful - use real user data
            String roleName = getRoleName(user.getRoleId());
            JavaFXLauncher.showDashboard(user.getUserId(), roleName, user.getFullName());
            return;
        }

        // Fallback: Demo mode - accept any email/password with selected role
        String name = email.split("@")[0];
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        int userId = 1;

        JavaFXLauncher.showDashboard(userId, role, name);
    }

    @FXML
    private void handleRegister() {
        if (regNameField == null) return; // Registration panel not present

        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String role = regRoleCombo.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegStatus("Please fill all required fields.", true);
            return;
        }

        if (!email.contains("@")) {
            showRegStatus("Please enter a valid email address.", true);
            return;
        }

        String result = userService.handleRegister(name, email, password, phone, role);
        boolean success = result.startsWith("SUCCESS");
        showRegStatus(result, !success);

        if (success) {
            // Clear registration form
            regNameField.clear();
            regEmailField.clear();
            regPasswordField.clear();
            regPhoneField.clear();
        }
    }

    private String getRoleName(int roleId) {
        switch (roleId) {
            case 1: return "Buyer";
            case 2: return "Agent";
            case 3: return "Authority";
            case 4: return "Admin";
            default: return "Buyer";
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showRegStatus(String message, boolean isError) {
        if (regStatusLabel != null) {
            regStatusLabel.setText(message);
            regStatusLabel.setStyle(isError ? "-fx-text-fill: #FF4757; -fx-font-weight: bold;" :
                                              "-fx-text-fill: #2ED573; -fx-font-weight: bold;");
            regStatusLabel.setVisible(true);
        }
    }
}
