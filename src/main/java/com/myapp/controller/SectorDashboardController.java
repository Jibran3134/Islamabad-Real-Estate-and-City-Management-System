package com.myapp.controller;

import com.myapp.model.Sector;
import com.myapp.repository.SectorRepository;
import com.myapp.repository.SectorRepository.SectorStatistics;
import com.myapp.service.SectorService.FreezeResult;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC2: Sector Dashboard - Freeze Overloaded Sectors - JavaFX Controller
 * Uses existing SectorController -> SectorService -> SectorRepository
 */
public class SectorDashboardController implements Initializable {

    @FXML private TableView<SectorStatistics> statsTable;
    @FXML private TableColumn<SectorStatistics, Integer> colStatId;
    @FXML private TableColumn<SectorStatistics, String> colStatName;
    @FXML private TableColumn<SectorStatistics, Integer> colStatCapacity;
    @FXML private TableColumn<SectorStatistics, Integer> colStatCount;
    @FXML private TableColumn<SectorStatistics, String> colStatUsage;
    @FXML private TableColumn<SectorStatistics, String> colStatStatus;
    @FXML private TableColumn<SectorStatistics, String> colStatOverloaded;
    @FXML private TextField freezeSectorIdField;
    @FXML private CheckBox overrideCheckbox;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;

    private SectorController sectorController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sectorController = new SectorController(1);

        // Setup table columns using SectorStatistics fields
        colStatId.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getSectorId()).asObject());
        colStatName.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getSectorName()));
        colStatCapacity.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getCapacityLimit()).asObject());
        colStatCount.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getPropertyCount()).asObject());
        colStatUsage.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("%.1f%%", data.getValue().getUsagePercentage())));
        colStatStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus()));
        colStatOverloaded.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isOverloaded() ? "YES" : "NO"));

        // Click table row to populate freeze field
        statsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                freezeSectorIdField.setText(String.valueOf(newVal.getSectorId()));
            }
        });

        loadStatistics();
        addLog("[INFO] UC2: Sector Dashboard loaded.");
    }

    @FXML
    private void handleFreezeSector() {
        String sectorIdText = freezeSectorIdField.getText().trim();
        if (sectorIdText.isEmpty()) {
            showStatus("Please enter a Sector ID.", true);
            return;
        }

        try {
            int sectorId = Integer.parseInt(sectorIdText);
            boolean override = overrideCheckbox.isSelected();

            FreezeResult result = sectorController.freezeSector(sectorId, override);

            if (result.isSuccess()) {
                showStatus("SUCCESS: " + result.getMessage(), false);
                addLog("[FROZEN] Sector " + sectorId + " has been frozen.");
                loadStatistics();
            } else {
                showStatus("FAILED: " + result.getMessage(), true);
                addLog("[FAILED] " + result.getMessage());
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid Sector ID.", true);
        } catch (SQLException e) {
            showStatus("Database error: " + e.getMessage(), true);
            addLog("[ERROR] " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadStatistics();
        addLog("[INFO] Statistics refreshed.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) statsTable.getScene().getWindow();
        stage.close();
    }

    private void loadStatistics() {
        try {
            List<SectorStatistics> stats = sectorController.getSectorStatistics();
            statsTable.setItems(FXCollections.observableArrayList(stats));
            showStatus("Loaded " + stats.size() + " sector statistics.", false);
        } catch (SQLException e) {
            showStatus("Failed to load: " + e.getMessage(), true);
            addLog("[ERROR] " + e.getMessage());
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: -fx-danger;" : "-fx-text-fill: -fx-success;");
    }

    private void addLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n");
        });
    }
}
