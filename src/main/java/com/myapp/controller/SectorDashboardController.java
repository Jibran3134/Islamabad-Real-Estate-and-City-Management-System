package com.myapp.controller;

import com.myapp.repository.SectorRepository.SectorStatistics;
import com.myapp.service.SectorService;
import com.myapp.service.SectorService.FreezeResult;
import com.myapp.service.SectorService.FreezeImpact;
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
 * NON-CRUD: analyzeFreezeImpact() shows impact BEFORE freeze is executed
 */
public class SectorDashboardController implements Initializable {

    @FXML private TableView<SectorStatistics> statsTable;
    @FXML private TableColumn<SectorStatistics, Integer> colStatId;
    @FXML private TableColumn<SectorStatistics, String>  colStatName;
    @FXML private TableColumn<SectorStatistics, Integer> colStatCapacity;
    @FXML private TableColumn<SectorStatistics, Integer> colStatCount;
    @FXML private TableColumn<SectorStatistics, String>  colStatUsage;
    @FXML private TableColumn<SectorStatistics, String>  colStatStatus;
    @FXML private TableColumn<SectorStatistics, String>  colStatOverloaded;
    @FXML private TextField freezeSectorIdField;
    @FXML private CheckBox  overrideCheckbox;
    @FXML private Label     statusLabel;
    @FXML private TextArea  logArea;

    // NON-CRUD: Impact analysis panel
    @FXML private Label    impactSeverityLabel;
    @FXML private Label    impactSummaryLabel;
    @FXML private TextArea impactTextArea;

    private SectorController sectorController;
    private SectorService    sectorService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sectorController = new SectorController(1);
        sectorService    = new SectorService();

        // Table columns
        colStatId.setCellValueFactory(d ->
            new SimpleIntegerProperty(d.getValue().getSectorId()).asObject());
        colStatName.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getSectorName()));
        colStatCapacity.setCellValueFactory(d ->
            new SimpleIntegerProperty(d.getValue().getCapacityLimit()).asObject());
        colStatCount.setCellValueFactory(d ->
            new SimpleIntegerProperty(d.getValue().getPropertyCount()).asObject());
        colStatUsage.setCellValueFactory(d ->
            new SimpleStringProperty(String.format("%.1f%%", d.getValue().getUsagePercentage())));
        colStatStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getStatus()));
        colStatOverloaded.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().isOverloaded() ? "⚠ YES" : "✓ NO"));

        // Click row → fill freeze field AND auto-run impact analysis
        statsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                freezeSectorIdField.setText(String.valueOf(newVal.getSectorId()));
                runImpactAnalysis(newVal.getSectorId());
            }
        });

        loadStatistics();
        addLog("[INFO] UC2: Sector Dashboard loaded. Freeze Impact Analyzer ready.");
    }

    // ── NON-CRUD: Freeze Impact Analysis ──────────────────────────────────────

    /**
     * Analyzes IMPACT of freezing a sector BEFORE executing the freeze.
     * Calls SectorService.analyzeFreezeImpact() — NON-CRUD business intelligence.
     */
    @FXML
    private void handleAnalyzeImpact() {
        String idText = freezeSectorIdField.getText().trim();
        if (idText.isEmpty()) {
            showStatus("Select a sector from the table first.", true);
            return;
        }
        try {
            int sectorId = Integer.parseInt(idText);
            runImpactAnalysis(sectorId);
        } catch (NumberFormatException e) {
            showStatus("Invalid Sector ID.", true);
        }
    }

    private void runImpactAnalysis(int sectorId) {
        try {
            FreezeImpact impact = sectorService.analyzeFreezeImpact(sectorId);
            Platform.runLater(() -> {
                if (impactSeverityLabel != null) {
                    impactSeverityLabel.setText("Impact: " + impact.getSeverity());
                    String color = impact.getSeverity().contains("SEVERE") ? "#FF4757"
                                 : impact.getSeverity().contains("HIGH")   ? "#FF6B35"
                                 : impact.getSeverity().contains("MEDIUM") ? "#FFA502"
                                 : "#2ED573";
                    impactSeverityLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                }
                if (impactSummaryLabel != null) {
                    impactSummaryLabel.setText(
                        "🚫 " + impact.getBlockedListings() + " listings blocked  |  " +
                        "🔨 " + impact.getAffectedBidSessions() + " auctions affected"
                    );
                    impactSummaryLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px;");
                }
                if (impactTextArea != null) {
                    impactTextArea.setText(impact.getReportText());
                }
                addLog("[IMPACT] " + impact.getSectorName()
                    + " → " + impact.getBlockedListings() + " listings, "
                    + impact.getAffectedBidSessions() + " auctions, Severity=" + impact.getSeverity());
            });
        } catch (SQLException e) {
            addLog("[IMPACT ERROR] " + e.getMessage());
        }
    }

    // ── CRUD: Freeze Sector ───────────────────────────────────────────────────

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

            // Run impact analysis and log before freeze
            try {
                FreezeImpact impact = sectorService.analyzeFreezeImpact(sectorId);
                addLog("[PRE-FREEZE IMPACT] " + impact.getBlockedListings()
                    + " listings blocked, " + impact.getAffectedBidSessions()
                    + " auctions affected. Severity: " + impact.getSeverity());
            } catch (Exception ignored) {}

            FreezeResult result = sectorController.freezeSector(sectorId, override);
            if (result.isSuccess()) {
                showStatus("SUCCESS: " + result.getMessage(), false);
                addLog("[FROZEN] Sector " + sectorId + " has been frozen.");
                loadStatistics();
                // Clear impact panel after freeze
                if (impactTextArea != null) impactTextArea.setText("Sector frozen. Select another sector to analyze.");
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
        // Fixed: hard-coded hex instead of CSS variable references
        statusLabel.setStyle(isError
            ? "-fx-text-fill: #FF4757; -fx-font-weight: bold;"
            : "-fx-text-fill: #2ED573; -fx-font-weight: bold;");
    }

    private void addLog(String message) {
        Platform.runLater(() ->
            logArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n"));
    }
}
