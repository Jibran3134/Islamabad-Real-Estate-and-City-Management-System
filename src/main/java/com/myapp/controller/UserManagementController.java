package com.myapp.controller;

import com.myapp.model.User;
import com.myapp.service.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC11: User Management - JavaFX Controller
 * Uses existing UserController -> UserService -> UserRepository
 */
public class UserManagementController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colUserStatus;
    @FXML private TextField targetUserIdField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;

    private UserController userController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userController = new UserController();

        // Setup columns
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status combo
        statusCombo.setItems(FXCollections.observableArrayList(
            "Active", "Suspended", "Inactive", "Banned"
        ));
        statusCombo.getSelectionModel().selectFirst();

        // Table click
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                targetUserIdField.setText(String.valueOf(newVal.getUserId()));
            }
        });

        loadUsers();
        addLog("[INFO] UC11: User Management loaded.");
    }

    @FXML
    private void handleUpdateStatus() {
        String userIdText = targetUserIdField.getText().trim();
        String newStatus = statusCombo.getValue();

        if (userIdText.isEmpty()) {
            showStatus("Select a user from the table.", true);
            return;
        }

        try {
            int userId = Integer.parseInt(userIdText);
            int adminId = 1; // Current admin

            String result = userController.updateUserStatus(adminId, userId, newStatus);
            boolean success = result.startsWith("SUCCESS");
            showStatus(result, !success);
            addLog("[UPDATE] " + result);
            if (success) loadUsers();
        } catch (NumberFormatException e) {
            showStatus("Invalid User ID. Please enter a valid number.", true);
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        addLog("[INFO] User list refreshed.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) userTable.getScene().getWindow();
        stage.close();
    }

    private void loadUsers() {
        try {
            UserService service = new UserService();
            List<User> users = service.getAllUsers();
            if (users != null) {
                userTable.setItems(FXCollections.observableArrayList(users));
                showStatus("Loaded " + users.size() + " users.", false);
            } else {
                showStatus("No users found.", false);
            }
        } catch (Exception e) {
            showStatus("Failed to load users: " + e.getMessage(), true);
            addLog("[ERROR] " + e.getMessage());
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #FF4757; -fx-font-weight: bold;" : "-fx-text-fill: #2ED573; -fx-font-weight: bold;");
    }

    private void addLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n");
        });
    }
}
