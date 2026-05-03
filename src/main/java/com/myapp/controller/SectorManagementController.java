package com.myapp.controller;

import com.myapp.model.Sector;
import com.myapp.service.SectorService;
import com.myapp.service.SectorService.UpdateResult;
import com.myapp.service.SectorService.CapacityAdvisory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC1: Define Sector Capacity Limits — JavaFX Controller (FULL OVERHAUL)
 * Includes NON-CRUD: Smart Capacity Advisor (analyzeCapacity)
 *
 * Fixes applied:
 *  • handleApplyRecommendation — uses lastAdvisory (not 0) and actually saves to DB
 *  • handleAnalyze — full visual update with color risk meter
 *  • handleRefresh — resets advisor + reloads charts
 *  • Live PieChart and BarChart analytics populated on load/refresh
 */
public class SectorManagementController implements Initializable {

    // ── Table ────────────────────────────────────────────────────────────────
    @FXML private TableView<Sector>          sectorTable;
    @FXML private TableColumn<Sector, Integer> colId;
    @FXML private TableColumn<Sector, String>  colName;
    @FXML private TableColumn<Sector, Integer> colCapacity;
    @FXML private TableColumn<Sector, Integer> colCount;
    @FXML private TableColumn<Sector, String>  colUtil;   // new: utilization % column
    @FXML private TableColumn<Sector, String>  colStatus;

    // ── Form fields ──────────────────────────────────────────────────────────
    @FXML private TextField sectorIdField;
    @FXML private TextField capacityField;
    @FXML private Label     selectedSectorNameLabel;

    // ── Risk meter in form ───────────────────────────────────────────────────
    @FXML private VBox    riskMeterBox;
    @FXML private Region  riskMeterFill;
    @FXML private Label   riskMeterLabel;

    // ── Smart Advisor panel ──────────────────────────────────────────────────
    @FXML private Label    riskScoreNumber;
    @FXML private Label    advisoryRiskLabel;
    @FXML private Label    advisoryRecommendLabel;
    @FXML private Label    projectedUsageLabel;
    @FXML private TextArea advisoryTextArea;

    // ── Analytics summary cards ──────────────────────────────────────────────
    @FXML private Label statTotalSectors;
    @FXML private Label statTotalCapacity;
    @FXML private Label statTotalProperties;
    @FXML private Label statOverloaded;
    @FXML private Label statAvgUtil;

    // ── Charts ───────────────────────────────────────────────────────────────
    @FXML private PieChart utilizationPieChart;
    @FXML private BarChart<String, Number> sectorBarChart;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis   barYAxis;

    // ── Status / Log ─────────────────────────────────────────────────────────
    @FXML private Label    statusLabel;
    @FXML private TextArea logArea;

    // ── Internal state ────────────────────────────────────────────────────────
    private SectorController sectorController;
    private SectorService    sectorService;

    /**
     * Holds the LAST computed advisory so "Apply Recommendation" can use the
     * real recommended value — not a stale call with proposedCapacity=0.
     */
    private CapacityAdvisory lastAdvisory = null;

    // ═════════════════════════════════════════════════════════════════════════
    //  INITIALIZATION
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sectorController = new SectorController(1); // Authority ID = 1
        sectorService    = new SectorService();

        setupTableColumns();
        setupRowSelection();
        setupLiveCapacityListener();
        styleBarChart();

        loadSectors(); // loads table + analytics cards + charts
        addLog("[INFO] UC1: Sector Management loaded. Smart Capacity Advisor ready.");
    }

    // ── Column setup ─────────────────────────────────────────────────────────

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("sectorId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("sectorName"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacityLimit"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("currentPropertyCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Computed utilization % column (not stored on model, so we compute it)
        colUtil.setCellValueFactory(cellData -> {
            Sector s = cellData.getValue();
            if (s.getCapacityLimit() <= 0) return new javafx.beans.property.SimpleStringProperty("—");
            double pct = (s.getCurrentPropertyCount() * 100.0) / s.getCapacityLimit();
            return new javafx.beans.property.SimpleStringProperty(
                    String.format("%.1f%%", pct));
        });

        // Color-code the utilization cell
        colUtil.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                try {
                    double val = Double.parseDouble(item.replace("%", ""));
                    if      (val >= 90) setStyle("-fx-text-fill: #FF4757; -fx-font-weight: bold;");
                    else if (val >= 75) setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold;");
                    else if (val >= 50) setStyle("-fx-text-fill: #FFA502;");
                    else                setStyle("-fx-text-fill: #2ED573;");
                } catch (NumberFormatException ignored) { setStyle(""); }
            }
        });

        // Color-code status column
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equalsIgnoreCase("FROZEN"))     setStyle("-fx-text-fill: #74B9FF; -fx-font-weight: bold;");
                else if (item.equalsIgnoreCase("ACTIVE")) setStyle("-fx-text-fill: #2ED573; -fx-font-weight: bold;");
                else                                      setStyle("-fx-text-fill: #AAAAAA;");
            }
        });
    }

    // ── Row click → auto-fill + auto-analyze ────────────────────────────────

    private void setupRowSelection() {
        sectorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sectorIdField.setText(String.valueOf(newVal.getSectorId()));
                capacityField.setText(String.valueOf(newVal.getCapacityLimit()));
                selectedSectorNameLabel.setText(newVal.getSectorName());
                selectedSectorNameLabel.setStyle(
                    "-fx-text-fill: #D4AF37; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 6 0;");
                // Auto-run advisor when row selected
                runSmartAdvisor(newVal.getSectorId(), newVal.getCapacityLimit());
            }
        });
    }

    // ── Live advisory as capacity changes ────────────────────────────────────

    private void setupLiveCapacityListener() {
        capacityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !sectorIdField.getText().isEmpty()) {
                try {
                    int sectorId = Integer.parseInt(sectorIdField.getText().trim());
                    int proposed = Integer.parseInt(newVal.trim());
                    runSmartAdvisor(sectorId, proposed);
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    private void styleBarChart() {
        sectorBarChart.setLegendVisible(true);
        sectorBarChart.setAnimated(true);
        barYAxis.setLabel("Count");
        sectorBarChart.setStyle("-fx-background-color: transparent;");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  NON-CRUD: SMART CAPACITY ADVISOR
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Manually trigger analysis from the "Analyze Manually" button.
     * Shows a full breakdown with colored risk indicator.
     */
    @FXML
    private void handleAnalyze() {
        String sectorIdText = sectorIdField.getText().trim();
        String capacityText = capacityField.getText().trim();

        if (sectorIdText.isEmpty() || capacityText.isEmpty()) {
            showStatus("⚠ Select a sector row and enter proposed capacity first.", true);
            addLog("[WARN] Analyze clicked without sector/capacity selected.");
            return;
        }
        try {
            int sectorId = Integer.parseInt(sectorIdText);
            int proposed = Integer.parseInt(capacityText);
            runSmartAdvisor(sectorId, proposed);
            showStatus("✔ Analysis complete for sector #" + sectorId, false);
            addLog("[ANALYZE] Manual analysis triggered for sector #" + sectorId
                    + " with proposed capacity=" + proposed);
        } catch (NumberFormatException e) {
            showStatus("⚠ Enter valid numeric values.", true);
        }
    }

    /**
     * Core NON-CRUD method — calls SectorService.analyzeCapacity() and
     * updates ALL advisory UI elements including the risk gauge and meter bar.
     * Also stores result as lastAdvisory for Apply Recommendation button.
     */
    private void runSmartAdvisor(int sectorId, int proposedCapacity) {
        new Thread(() -> {
            try {
                CapacityAdvisory advisory = sectorService.analyzeCapacity(sectorId, proposedCapacity);
                lastAdvisory = advisory; // ← save for Apply button (FIX)

                Platform.runLater(() -> {
                    // ── Risk Score number ──────────────────────────────────
                    riskScoreNumber.setText(String.valueOf(advisory.getRiskScore()));
                    String riskColor = getRiskColor(advisory.getRiskScore());
                    riskScoreNumber.setStyle(
                        "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + riskColor + ";");

                    // ── Risk label ─────────────────────────────────────────
                    if (advisoryRiskLabel != null) {
                        advisoryRiskLabel.setText("Risk: " + advisory.getRiskLevel()
                            + "  (" + advisory.getRiskScore() + "/100)");
                        advisoryRiskLabel.setStyle(
                            "-fx-text-fill: " + riskColor + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }

                    // ── Recommendation label ────────────────────────────────
                    if (advisoryRecommendLabel != null) {
                        advisoryRecommendLabel.setText(
                            "💡 Recommended safe capacity: " + advisory.getRecommendedCapacity());
                        advisoryRecommendLabel.setStyle(
                            "-fx-text-fill: #2ED573; -fx-font-size: 13px; -fx-font-weight: bold;");
                    }

                    // ── Projected usage label ───────────────────────────────
                    if (projectedUsageLabel != null) {
                        projectedUsageLabel.setText(
                            "📈 Projected usage after change: " + advisory.getProjectedUsagePct() + "%");
                        String projColor = advisory.getProjectedUsagePct() >= 90 ? "#FF4757"
                                         : advisory.getProjectedUsagePct() >= 60 ? "#FFA502"
                                         : "#2ED573";
                        projectedUsageLabel.setStyle("-fx-text-fill: " + projColor + "; -fx-font-size: 12px;");
                    }

                    // ── Full advisory text ──────────────────────────────────
                    if (advisoryTextArea != null) {
                        advisoryTextArea.setText(advisory.getAdvisoryText());
                    }

                    // ── Risk meter bar in form ──────────────────────────────
                    if (riskMeterBox != null) {
                        riskMeterBox.setVisible(true);
                        riskMeterBox.setManaged(true);
                        double fillPct = advisory.getRiskScore() / 100.0;
                        // StackPane parent width ≈ 280px (form half)
                        riskMeterFill.setPrefWidth(fillPct * 280);
                        riskMeterFill.setStyle(
                            "-fx-background-color: " + riskColor + "; -fx-background-radius: 6;");
                        riskMeterLabel.setText(advisory.getRiskScore() + "/100 — " + advisory.getRiskLevel());
                        riskMeterLabel.setStyle(
                            "-fx-font-size: 11px; -fx-text-fill: " + riskColor + "; -fx-font-weight: bold;");
                    }

                    addLog("[ADVISOR] " + advisory.getSectorName()
                        + " → Risk=" + advisory.getRiskScore()
                        + "/100 [" + advisory.getRiskLevel() + "]"
                        + ", Projected=" + advisory.getProjectedUsagePct()
                        + "%, Recommended=" + advisory.getRecommendedCapacity());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    addLog("[ADVISOR ERROR] " + e.getMessage());
                    showStatus("⚠ Advisor error: " + e.getMessage(), true);
                });
            }
        }, "AdvisorThread").start();
    }

    // ── Apply Recommendation — FIXED ─────────────────────────────────────────

    /**
     * FIX: Uses lastAdvisory (not a fresh call with proposedCapacity=0).
     * Also actually saves the recommended capacity to DB and reloads.
     */
    @FXML
    private void handleApplyRecommendation() {
        String sectorIdText = sectorIdField.getText().trim();
        if (sectorIdText.isEmpty()) {
            showStatus("⚠ Select a sector first.", true);
            return;
        }
        if (lastAdvisory == null) {
            showStatus("⚠ Run analysis first (click a row or type a capacity).", true);
            addLog("[WARN] Apply Recommendation clicked but no advisory computed yet.");
            return;
        }

        int recommendedCapacity = lastAdvisory.getRecommendedCapacity();
        capacityField.setText(String.valueOf(recommendedCapacity));

        // Immediately save the recommended value to DB
        try {
            int sectorId = Integer.parseInt(sectorIdText);
            UpdateResult result = sectorController.defineSectorCapacity(sectorId, recommendedCapacity);

            if (result.isSuccess()) {
                showStatus("✔ Recommendation applied & saved: capacity=" + recommendedCapacity, false);
                addLog("[APPLIED] Sector #" + sectorId
                    + " capacity set to recommended=" + recommendedCapacity
                    + " | was: " + lastAdvisory.getRiskLevel());
                loadSectors(); // refresh table + charts
                runSmartAdvisor(sectorId, recommendedCapacity); // re-analyze with new value
            } else {
                showStatus("⚠ Could not save: " + result.getMessage(), true);
                addLog("[FAILED] Apply recommendation: " + result.getMessage());
            }
        } catch (NumberFormatException | SQLException e) {
            showStatus("⚠ Error: " + e.getMessage(), true);
            addLog("[ERROR] " + e.getMessage());
        }
    }

    // ── Clear advisor ─────────────────────────────────────────────────────────

    @FXML
    private void handleClearAdvisor() {
        lastAdvisory = null;
        advisoryRiskLabel.setText("⬤  Select a sector to begin analysis");
        advisoryRiskLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-font-weight: bold;");
        advisoryRecommendLabel.setText("💡 Recommendation: —");
        advisoryRecommendLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px;");
        projectedUsageLabel.setText("📈 Projected Usage: —");
        projectedUsageLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
        riskScoreNumber.setText("—");
        riskScoreNumber.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #AAAAAA;");
        advisoryTextArea.clear();
        if (riskMeterBox != null) {
            riskMeterBox.setVisible(false);
            riskMeterBox.setManaged(false);
        }
        addLog("[INFO] Advisor panel cleared.");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CRUD: UPDATE CAPACITY
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleUpdateCapacity() {
        String sectorIdText = sectorIdField.getText().trim();
        String capacityText = capacityField.getText().trim();

        if (sectorIdText.isEmpty() || capacityText.isEmpty()) {
            showStatus("⚠ Select a sector row and enter a capacity value.", true);
            return;
        }

        try {
            int sectorId = Integer.parseInt(sectorIdText);
            int capacity = Integer.parseInt(capacityText);

            // Run advisory check first — warn in log but still allow update
            if (lastAdvisory != null && !lastAdvisory.isSafe()) {
                addLog("[WARN] Unsafe capacity submitted: " + lastAdvisory.getRiskLevel()
                    + " (score=" + lastAdvisory.getRiskScore() + ")");
            }

            UpdateResult result = sectorController.defineSectorCapacity(sectorId, capacity);

            if (result.isSuccess()) {
                showStatus("✔ SUCCESS: Sector #" + sectorId + " capacity → " + capacity, false);
                addLog("[SUCCESS] Updated Sector #" + sectorId + " capacity to " + capacity
                    + (lastAdvisory != null ? " | Risk was: " + lastAdvisory.getRiskLevel() : ""));
                loadSectors();
                runSmartAdvisor(sectorId, capacity); // re-analyze after update
            } else {
                showStatus("⚠ FAILED: " + result.getMessage(), true);
                addLog("[FAILED] " + result.getMessage());
            }
        } catch (NumberFormatException e) {
            showStatus("⚠ Enter valid numeric values.", true);
        } catch (SQLException e) {
            showStatus("⚠ Database error: " + e.getMessage(), true);
            addLog("[DB ERROR] " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  REFRESH — properly resets advisor + reloads everything
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleRefresh() {
        // Clear advisor state
        lastAdvisory = null;
        // Clear selection-dependent fields
        sectorIdField.clear();
        capacityField.clear();
        selectedSectorNameLabel.setText("None selected");
        selectedSectorNameLabel.setStyle(
            "-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-padding: 6 0;");
        // Reset advisor display
        handleClearAdvisor();
        // Reload everything fresh
        loadSectors();
        addLog("[REFRESH] Full data reload complete — advisor reset.");
        showStatus("✔ Refreshed successfully.", false);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) sectorTable.getScene().getWindow();
        stage.close();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DATA LOADING + ANALYTICS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Loads sectors, updates table, populates analytics cards, and refreshes charts.
     */
    private void loadSectors() {
        try {
            List<Sector> sectors = sectorController.getAllSectors();
            sectorTable.setItems(FXCollections.observableArrayList(sectors));

            // ── Compute analytics ─────────────────────────────────────────
            int totalCapacity   = 0;
            int totalProperties = 0;
            int overloadedCount = 0;

            for (Sector s : sectors) {
                totalCapacity   += s.getCapacityLimit();
                totalProperties += s.getCurrentPropertyCount();
                if (s.isOverloaded()) overloadedCount++;
            }

            double avgUtil = totalCapacity > 0
                ? (totalProperties * 100.0) / totalCapacity : 0;

            // ── Update stat cards ─────────────────────────────────────────
            statTotalSectors.setText(String.valueOf(sectors.size()));
            statTotalCapacity.setText(String.valueOf(totalCapacity));
            statTotalProperties.setText(String.valueOf(totalProperties));
            statOverloaded.setText(String.valueOf(overloadedCount));
            statAvgUtil.setText(String.format("%.0f%%", avgUtil));

            // Color avg util label
            String utilColor = avgUtil >= 90 ? "#FF4757" : avgUtil >= 70 ? "#FFA502" : "#2ED573";
            statAvgUtil.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + utilColor + ";");

            // ── Pie Chart: used vs free capacity ─────────────────────────
            updatePieChart(totalProperties, Math.max(0, totalCapacity - totalProperties));

            // ── Bar Chart: per-sector capacity vs properties ──────────────
            updateBarChart(sectors);

            showStatus("✔ Loaded " + sectors.size() + " sectors.", false);
        } catch (SQLException e) {
            showStatus("⚠ Failed to load sectors: " + e.getMessage(), true);
            addLog("[DB ERROR] " + e.getMessage());
        }
    }

    // ── Pie Chart ─────────────────────────────────────────────────────────────

    private void updatePieChart(int used, int free) {
        utilizationPieChart.getData().clear();

        PieChart.Data usedSlice = new PieChart.Data("Used (" + used + ")", used);
        PieChart.Data freeSlice = new PieChart.Data("Free (" + free + ")", Math.max(free, 0));

        utilizationPieChart.getData().addAll(usedSlice, freeSlice);
        utilizationPieChart.setTitle("");

        // Apply colors after layout pass
        Platform.runLater(() -> {
            if (usedSlice.getNode() != null)
                usedSlice.getNode().setStyle("-fx-pie-color: #D4AF37;");
            if (freeSlice.getNode() != null)
                freeSlice.getNode().setStyle("-fx-pie-color: #2ED573;");
        });
    }

    // ── Bar Chart ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void updateBarChart(List<Sector> sectors) {
        sectorBarChart.getData().clear();

        XYChart.Series<String, Number> capacitySeries = new XYChart.Series<>();
        capacitySeries.setName("Capacity Limit");

        XYChart.Series<String, Number> propertySeries = new XYChart.Series<>();
        propertySeries.setName("Properties Listed");

        for (Sector s : sectors) {
            // Abbreviate long names
            String label = s.getSectorName().length() > 10
                ? s.getSectorName().substring(0, 10) + "…"
                : s.getSectorName();
            capacitySeries.getData().add(new XYChart.Data<>(label, s.getCapacityLimit()));
            propertySeries.getData().add(new XYChart.Data<>(label, s.getCurrentPropertyCount()));
        }

        sectorBarChart.getData().addAll(capacitySeries, propertySeries);

        // Apply colors to bars AND legend symbols after layout renders.
        // Legend symbols are separate nodes — must be styled independently.
        Platform.runLater(() -> {
            // ── Bar nodes ────────────────────────────────────────────────
            for (XYChart.Data<String, Number> d : capacitySeries.getData()) {
                if (d.getNode() != null)
                    d.getNode().setStyle("-fx-bar-fill: #D4AF37;");
            }
            for (XYChart.Data<String, Number> d : propertySeries.getData()) {
                if (d.getNode() != null)
                    d.getNode().setStyle("-fx-bar-fill: #A29BFE;");
            }

            // ── Legend symbol nodes (the small colored squares/circles) ──
            // JavaFX assigns class 'default-color0' to series 0, etc.
            // CSS alone doesn't always override runtime defaults, so we
            // walk the scene graph and set background color explicitly.
            sectorBarChart.lookupAll(".default-color0.chart-legend-item-symbol")
                .forEach(n -> n.setStyle("-fx-background-color: #D4AF37;"));
            sectorBarChart.lookupAll(".default-color1.chart-legend-item-symbol")
                .forEach(n -> n.setStyle("-fx-background-color: #A29BFE;"));
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Returns hex color string based on risk score.
     *  Matches the updated risk bands in SectorService.analyzeCapacity():
     *  ≥85 → CRITICAL (red), ≥65 → HIGH (orange), ≥40 → MEDIUM (amber), <40 → SAFE (green)
     */
    private String getRiskColor(int riskScore) {
        if (riskScore >= 85) return "#FF4757"; // CRITICAL — red
        if (riskScore >= 65) return "#FF6B35"; // HIGH     — orange
        if (riskScore >= 40) return "#FFA502"; // MEDIUM   — amber
        return "#2ED573";                       // SAFE     — green
    }

    private void showStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setStyle(isError
                ? "-fx-text-fill: #FF4757; -fx-font-weight: bold; -fx-font-size: 13px;"
                : "-fx-text-fill: #2ED573; -fx-font-weight: bold; -fx-font-size: 13px;");
        });
    }

    private void addLog(String message) {
        Platform.runLater(() ->
            logArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n"));
    }
}
