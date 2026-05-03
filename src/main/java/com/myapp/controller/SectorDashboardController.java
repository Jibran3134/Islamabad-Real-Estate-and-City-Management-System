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
    @FXML private javafx.scene.chart.PieChart impactPieChart;

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
            
            // Get current stats to calculate wasted space without changing backend
            SectorStatistics currentStat = statsTable.getItems().stream()
                    .filter(s -> s.getSectorId() == sectorId).findFirst().orElse(null);

            Platform.runLater(() -> {
                String finalSeverity = impact.getSeverity();
                String extraMessage = "";
                int used = 0;
                int free = 0;

                if (currentStat != null) {
                    used = currentStat.getPropertyCount();
                    int limit = currentStat.getCapacityLimit();
                    free = Math.max(0, limit - used);
                    
                    double freePct = limit > 0 ? (free * 100.0) / limit : 0;
                    
                    // UI-side business logic: huge waste of space = high impact
                    if (freePct >= 50.0) {
                        finalSeverity = "🔴 SEVERE (Wasted Space)";
                        extraMessage = "\n\n❌ CRITICAL: Freezing this sector wastes " + String.format("%.1f%%", freePct) + " of its capacity!\n   A massive amount of empty area will be lost.";
                    } else if (freePct >= 30.0) {
                        if (!finalSeverity.contains("SEVERE")) finalSeverity = "🟠 HIGH (Wasted Space)";
                        extraMessage = "\n\n⚠️ HIGH: Freezing this sector wastes " + String.format("%.1f%%", freePct) + " of its capacity.";
                    }
                }

                if (impactSeverityLabel != null) {
                    impactSeverityLabel.setText("Impact: " + finalSeverity);
                    String color = finalSeverity.contains("SEVERE") ? "#FF4757"
                                 : finalSeverity.contains("HIGH")   ? "#FF6B35"
                                 : finalSeverity.contains("MEDIUM") ? "#FFA502"
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
                    String cleanReport = impact.getReportText()
                        .replaceAll("(?m)^🚫 Listings to be BLOCKED:.*\\n?", "")
                        .replaceAll("(?m)^🔨 Active Auctions Affected:.*\\n?", "")
                        .replaceAll("(?m)^📈 Impact Severity:.*\\n?", "")
                        .replaceAll("(?m)^\\s*⚠️  Some listings will be affected\\. Proceed with caution\\..*\\n?", "");
                    impactTextArea.setText(cleanReport.trim() + extraMessage);
                }
                
                // Update PieChart
                if (impactPieChart != null) {
                    impactPieChart.getData().clear();
                    javafx.scene.chart.PieChart.Data usedSlice = new javafx.scene.chart.PieChart.Data("Used", used);
                    javafx.scene.chart.PieChart.Data freeSlice = new javafx.scene.chart.PieChart.Data("Wasted (Frozen)", free);
                    impactPieChart.getData().addAll(usedSlice, freeSlice);
                    
                    // Apply styles after layout
                    Platform.runLater(() -> {
                        if (usedSlice.getNode() != null) usedSlice.getNode().setStyle("-fx-pie-color: #D4AF37;"); // Gold
                        if (freeSlice.getNode() != null) freeSlice.getNode().setStyle("-fx-pie-color: #FF4757;"); // Red
                        
                        impactPieChart.lookupAll(".default-color0.chart-pie-legend-symbol").forEach(n -> n.setStyle("-fx-background-color: #D4AF37;"));
                        impactPieChart.lookupAll(".default-color1.chart-pie-legend-symbol").forEach(n -> n.setStyle("-fx-background-color: #FF4757;"));
                    });
                }

                addLog("[IMPACT] Sector ID " + sectorId + " → Severity=" + finalSeverity);
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
