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
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC6-9: Bidding Dashboard - JavaFX Controller
 * UC6: Place Bid | UC8: Close Bidding | UC9: Declare Winner
 * Uses existing BidController -> BidService -> BidRepository
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

    private BidController bidController;
    private BidService bidService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bidController = new BidController();
        bidService = new BidService();

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
                sessionIdField.setText(id);
                closeSessionIdField.setText(id);
                winnerSessionIdField.setText(id);
            }
        });

        // Price validation
        bidAmountField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) bidAmountField.setText(old);
        });

        loadSessions();
        addLog("[INFO] UC6-9: Bidding Dashboard loaded.");
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
            int buyerId = 1; // Current user

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
            String result = bidController.closeBiddingSession(sessionId);
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
        statusLabel.setStyle(isError ? "-fx-text-fill: -fx-danger;" : "-fx-text-fill: -fx-success;");
    }

    private void addLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n");
        });
    }
}
