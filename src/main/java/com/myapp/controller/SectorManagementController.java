package com.myapp.controller;

import com.myapp.model.Sector;
import com.myapp.service.SectorService;
import com.myapp.service.SectorService.UpdateResult;
import com.myapp.service.SectorService.CapacityAdvisory;
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
 * Includes NON-CRUD: Smart Capacity Advisor (analyzeCapacity)
 */
public class SectorManagementController implements Initializable {

    @FXML private TableView<Sector> sectorTable;
    @FXML private TableColumn<Sector, Integer> colId;
    @FXML private TableColumn<Sector, String>  colName;
    @FXML private TableColumn<Sector, Integer> colCapacity;
    @FXML private TableColumn<Sector, Integer> colCount;
    @FXML private TableColumn<Sector, String>  colStatus;
    @FXML private TextField sectorIdField;
    @FXML private TextField capacityField;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;

    // NON-CRUD: Advisory panel fields
    @FXML private Label advisoryRiskLabel;
    @FXML private Label advisoryRecommendLabel;
    @FXML private TextArea advisoryTextArea;

    private SectorController sectorController;
    private SectorService sectorService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sectorController = new SectorController(1); // Authority ID = 1
        sectorService    = new SectorService();

        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("sectorId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("sectorName"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacityLimit"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("currentPropertyCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Click row → fill fields AND auto-analyze
        sectorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sectorIdField.setText(String.valueOf(newVal.getSectorId()));
                capacityField.setText(String.valueOf(newVal.getCapacityLimit()));
                // Auto-run advisor when row selected
                runSmartAdvisor(newVal.getSectorId(), newVal.getCapacityLimit());
            }
        });

        // Live advisory when capacity field changes
        capacityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !sectorIdField.getText().isEmpty()) {
                try {
                    int sectorId = Integer.parseInt(sectorIdField.getText().trim());
                    int proposed = Integer.parseInt(newVal.trim());
                    runSmartAdvisor(sectorId, proposed);
                } catch (NumberFormatException ignored) {}
            }
        });

        loadSectors();
        addLog("[INFO] UC1: Sector Management loaded. Smart Capacity Advisor ready.");
    }

    // ── NON-CRUD: Smart Capacity Advisor ──────────────────────────────────────

    /**
     * Runs the NON-CRUD analyzeCapacity() from SectorService.
     * Shows risk score, recommendation, and advisory text in the UI panel.
     */
    @FXML
    private void handleAnalyze() {
        String sectorIdText  = sectorIdField.getText().trim();
        String capacityText  = capacityField.getText().trim();

        if (sectorIdText.isEmpty() || capacityText.isEmpty()) {
            showStatus("Select a sector and enter proposed capacity to analyze.", true);
            return;
        }
        try {
            int sectorId = Integer.parseInt(sectorIdText);
            int proposed = Integer.parseInt(capacityText);
            runSmartAdvisor(sectorId, proposed);
        } catch (NumberFormatException e) {
            showStatus("Invalid number format.", true);
        }
    }

    private void runSmartAdvisor(int sectorId, int proposedCapacity) {
        try {
            CapacityAdvisory advisory = sectorService.analyzeCapacity(sectorId, proposedCapacity);

            Platform.runLater(() -> {
                // Risk label
                if (advisoryRiskLabel != null) {
                    advisoryRiskLabel.setText("Risk: " + advisory.getRiskLevel()
                        + "  (" + advisory.getRiskScore() + "/100)");
                    String color = advisory.getRiskScore() >= 85 ? "#FF4757"
                                 : advisory.getRiskScore() >= 60 ? "#FF6B35"
                                 : advisory.getRiskScore() >= 35 ? "#FFA502"
                                 : "#2ED573";
                    advisoryRiskLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                }
                // Recommendation label
                if (advisoryRecommendLabel != null) {
                    advisoryRecommendLabel.setText("💡 Recommended capacity: " + advisory.getRecommendedCapacity());
                    advisoryRecommendLabel.setStyle("-fx-text-fill: #1A1A1A; -fx-font-size: 13px;");
                }
                // Full advisory text
                if (advisoryTextArea != null) {
                    advisoryTextArea.setText(advisory.getAdvisoryText());
                }
                addLog("[ADVISOR] " + advisory.getSectorName()
                    + " → Risk=" + advisory.getRiskScore()
                    + "/100, Projected=" + advisory.getProjectedUsagePct()
                    + "%, Recommended=" + advisory.getRecommendedCapacity());
            });
        } catch (SQLException e) {
            addLog("[ADVISOR ERROR] " + e.getMessage());
        }
    }

    // ── Apply recommendation from advisor ─────────────────────────────────────

    @FXML
    private void handleApplyRecommendation() {
        String sectorIdText = sectorIdField.getText().trim();
        if (sectorIdText.isEmpty()) {
            showStatus("Select a sector first.", true);
            return;
        }
        try {
            int sectorId = Integer.parseInt(sectorIdText);
            CapacityAdvisory advisory = sectorService.analyzeCapacity(sectorId, 0);
            capacityField.setText(String.valueOf(advisory.getRecommendedCapacity()));
            runSmartAdvisor(sectorId, advisory.getRecommendedCapacity());
            addLog("[ADVISOR] Recommended capacity " + advisory.getRecommendedCapacity() + " applied to field.");
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    // ── CRUD: Update Capacity ─────────────────────────────────────────────────

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

            // Run advisory check first — warn but still allow update
            CapacityAdvisory advisory = sectorService.analyzeCapacity(sectorId, capacity);
            if (!advisory.isSafe()) {
                addLog("[WARN] Unsafe capacity submitted: " + advisory.getRiskLevel());
            }

            UpdateResult result = sectorController.defineSectorCapacity(sectorId, capacity);

            if (result.isSuccess()) {
                showStatus("SUCCESS: " + result.getMessage(), false);
                addLog("[SUCCESS] Updated Sector " + sectorId + " capacity to " + capacity
                    + " | Risk was: " + advisory.getRiskLevel());
                loadSectors();
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
        statusLabel.setStyle(isError ? "-fx-text-fill: #FF4757; -fx-font-weight: bold;"
                                     : "-fx-text-fill: #2ED573; -fx-font-weight: bold;");
    }

    private void addLog(String message) {
        Platform.runLater(() ->
            logArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n"));
    }
}
