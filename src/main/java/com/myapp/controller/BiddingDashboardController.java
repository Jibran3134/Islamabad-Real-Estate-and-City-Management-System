package com.myapp.controller;

import com.myapp.model.BiddingSession;
import com.myapp.service.BidService;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC6-9: Bidding Dashboard - JavaFX Controller
 * UC6: Place Bid | UC8: Close Bidding | UC9: Declare Winner
 * Uses existing BidController -> BidService -> BidRepository
 *
 * Role-aware: hides/shows sections based on current user role.
 *   Agent: Close Session only (bypasses timer, auto-declares winner)
 *   Buyer: Place Bid only (no close/declare)
 */
public class BiddingDashboardController implements Initializable {

    @FXML private TableView<BiddingSession> sessionTable;
    @FXML private TableColumn<BiddingSession, Integer> colSessionId;
    @FXML private TableColumn<BiddingSession, String> colProperty;
    @FXML private TableColumn<BiddingSession, String> colBasePrice;
    @FXML private TableColumn<BiddingSession, String> colHighestBid;
    @FXML private TableColumn<BiddingSession, String> colSessionStatus;
    @FXML private TableColumn<BiddingSession, String> colEndTime;
    @FXML private TextField sessionIdField;
    @FXML private TextField bidAmountField;
    @FXML private TextField closeSessionIdField;
    @FXML private TextField winnerSessionIdField;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;

    // Role-aware sections (fx:id from FXML)
    @FXML private VBox placeBidSection;
    @FXML private VBox closeBiddingSection;
    @FXML private VBox declareWinnerSection;

    private BidController bidController;
    private BidService bidService;
    private String currentRole;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bidController = new BidController();
        bidService = new BidService();

        // Get current user role from DashboardController
        currentRole = DashboardController.getActiveUserRole();

        // Setup table columns
        colSessionId.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getSessionId()).asObject());
        colProperty.setCellValueFactory(data ->
            new SimpleStringProperty("Property #" + data.getValue().getPropertyId()));
        colBasePrice.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getBasePrice() != null ?
                data.getValue().getBasePrice().toString() : "N/A"));
        colHighestBid.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getCurrentHighestBid() != null ?
                data.getValue().getCurrentHighestBid().toString() : "No bids yet"));
        colSessionStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus()));
        colEndTime.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDeadline() != null ?
                data.getValue().getDeadline().toString() : "N/A"));

        // Click table row to populate fields
        sessionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String id = String.valueOf(newVal.getSessionId());
                if (sessionIdField != null) sessionIdField.setText(id);
                if (closeSessionIdField != null) closeSessionIdField.setText(id);
                if (winnerSessionIdField != null) winnerSessionIdField.setText(id);
            }
        });

        // Price validation
        if (bidAmountField != null) {
            bidAmountField.textProperty().addListener((obs, old, val) -> {
                if (!val.matches("\\d*(\\.\\d*)?")) bidAmountField.setText(old);
            });
        }

        // Apply role-based visibility
        applyRoleVisibility();

        loadSessions();
        addLog("[INFO] UC6-9: Bidding Dashboard loaded. Role: " + currentRole.toUpperCase());
    }

    /**
     * Hides/shows sections based on the current user's role.
     * Agent: only Close Session (bypasses timer, auto-declares winner)
     * Buyer: only Place Bid
     */
    private void applyRoleVisibility() {
        switch (currentRole) {
            case "agent":
                // Agent: show Close Session only. Hide Place Bid and Declare Winner.
                if (placeBidSection != null) {
                    placeBidSection.setVisible(false);
                    placeBidSection.setManaged(false);
                }
                if (declareWinnerSection != null) {
                    declareWinnerSection.setVisible(false);
                    declareWinnerSection.setManaged(false);
                }
                break;
            case "buyer":
                // Buyer: show Place Bid only. Hide Close Session and Declare Winner.
                if (closeBiddingSection != null) {
                    closeBiddingSection.setVisible(false);
                    closeBiddingSection.setManaged(false);
                }
                if (declareWinnerSection != null) {
                    declareWinnerSection.setVisible(false);
                    declareWinnerSection.setManaged(false);
                }
                break;
            default:
                // Admin/Authority or other: show all (though they shouldn't normally access this)
                break;
        }
    }

    @FXML
    private void handlePlaceBid() {
        String sessionIdText = sessionIdField.getText().trim();
        String bidText = bidAmountField.getText().trim();

        if (sessionIdText.isEmpty() || bidText.isEmpty()) {
            showStatus("Enter Session ID and Bid Amount.", true);
            return;
        }

        try {
            int sessionId = Integer.parseInt(sessionIdText);
            BigDecimal bidAmount = new BigDecimal(bidText);
            int buyerId = DashboardController.getActiveUserId();
            if (buyerId == 0) buyerId = 1; // Fallback

            String result = bidController.placeBid(sessionId, buyerId, bidAmount);
            boolean success = result.startsWith("SUCCESS");
            showStatus(result, !success);
            addLog("[BID] " + result);
            if (success) loadSessions();
        } catch (NumberFormatException e) {
            showStatus("Invalid number format.", true);
        }
    }

    @FXML
    private void handleCloseBidding() {
        String sessionIdText = closeSessionIdField.getText().trim();
        if (sessionIdText.isEmpty()) { showStatus("Enter Session ID.", true); return; }

        try {
            int sessionId = Integer.parseInt(sessionIdText);
            String result;

            if ("agent".equals(currentRole)) {
                // Agent: bypass timer and auto-declare winner
                result = bidController.forceCloseBiddingSession(sessionId);
            } else {
                result = bidController.closeBiddingSession(sessionId);
            }

            boolean success = result.startsWith("SUCCESS");
            showStatus(result, !success);
            addLog("[CLOSE] " + result);
            if (success) loadSessions();
        } catch (NumberFormatException e) {
            showStatus("Invalid Session ID.", true);
        }
    }

    @FXML
    private void handleDeclareWinner() {
        String sessionIdText = winnerSessionIdField.getText().trim();
        if (sessionIdText.isEmpty()) { showStatus("Enter Session ID.", true); return; }

        try {
            int sessionId = Integer.parseInt(sessionIdText);
            String result = bidController.declareWinner(sessionId);
            boolean success = result.startsWith("SUCCESS");
            showStatus(result, !success);
            addLog("[WINNER] " + result);
            if (success) loadSessions();
        } catch (NumberFormatException e) {
            showStatus("Invalid Session ID.", true);
        }
    }

    @FXML
    private void handleRefresh() {
        loadSessions();
        addLog("[INFO] Sessions refreshed.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) sessionTable.getScene().getWindow();
        stage.close();
    }

    private void loadSessions() {
        try {
            List<BiddingSession> sessions = bidService.getAllActiveSessions();
            if (sessions != null) {
                sessionTable.setItems(FXCollections.observableArrayList(sessions));
                showStatus("Loaded " + sessions.size() + " bidding sessions.", false);
            }
        } catch (Exception e) {
            showStatus("Failed to load sessions: " + e.getMessage(), true);
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
