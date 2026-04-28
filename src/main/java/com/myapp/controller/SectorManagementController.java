package com.myapp.controller;

import com.myapp.model.Sector;
import com.myapp.service.SectorService;
import com.myapp.service.SectorService.UpdateResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC1: Define Sector Capacity Limits - JavaFX Controller
 * Uses existing SectorController -> SectorService -> SectorRepository
 */
public class SectorManagementController implements Initializable {

    @FXML private TableView<Sector> sectorTable;
    @FXML private TableColumn<Sector, Integer> colId;
    @FXML private TableColumn<Sector, String> colName;
    @FXML private TableColumn<Sector, Integer> colCapacity;
    @FXML private TableColumn<Sector, Integer> colCount;
    @FXML private TableColumn<Sector, String> colStatus;
    @FXML private TextField sectorIdField;
    @FXML private TextField capacityField;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;

    private SectorController sectorController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sectorController = new SectorController(1); // Authority ID = 1

        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("sectorId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("sectorName"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacityLimit"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("currentPropertyCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Click on table row to populate fields
        sectorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sectorIdField.setText(String.valueOf(newVal.getSectorId()));
                capacityField.setText(String.valueOf(newVal.getCapacityLimit()));
            }
        });

        // Load data
        loadSectors();
        addLog("[INFO] UC1: Sector Management screen loaded.");
    }

    @FXML
    private void handleUpdateCapacity() {
        String sectorIdText = sectorIdField.getText().trim();
        String capacityText = capacityField.getText().trim();

        if (sectorIdText.isEmpty() || capacityText.isEmpty()) {
            showStatus("Please enter both Sector ID and Capacity.", true);
            return;
        }

        try {
            int sectorId = Integer.parseInt(sectorIdText);
            int capacity = Integer.parseInt(capacityText);

            UpdateResult result = sectorController.defineSectorCapacity(sectorId, capacity);

            if (result.isSuccess()) {
                showStatus("SUCCESS: " + result.getMessage(), false);
                addLog("[SUCCESS] Updated Sector " + sectorId + " capacity to " + capacity);
                loadSectors(); // Refresh table
            } else {
                showStatus("FAILED: " + result.getMessage(), true);
                addLog("[FAILED] " + result.getMessage());
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid number format.", true);
        } catch (SQLException e) {
            showStatus("Database error: " + e.getMessage(), true);
            addLog("[ERROR] " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadSectors();
        addLog("[INFO] Sector list refreshed.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) sectorTable.getScene().getWindow();
        stage.close();
    }

    private void loadSectors() {
        try {
            List<Sector> sectors = sectorController.getAllSectors();
            sectorTable.setItems(FXCollections.observableArrayList(sectors));
            showStatus("Loaded " + sectors.size() + " sectors.", false);
        } catch (SQLException e) {
            showStatus("Failed to load sectors: " + e.getMessage(), true);
            addLog("[ERROR] " + e.getMessage());
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: -fx-danger;" : "-fx-text-fill: -fx-success;");
    }

    private void addLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n");
        });
    }
}
