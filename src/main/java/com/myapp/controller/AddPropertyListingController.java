package com.myapp.controller;

import com.myapp.model.Sector;
import com.myapp.service.PropertyService;
import com.myapp.service.PropertyService.AddPropertyResult;
import com.myapp.service.PropertyValidator;
import com.myapp.service.PropertyValidator.ValidationResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC3: Add Property Listing - JavaFX Controller
 * Uses existing PropertyController -> PropertyService -> PropertyRepository
 */
public class AddPropertyListingController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> sellingMethodCombo;
    @FXML private ComboBox<String> sectorCombo;
    @FXML private TextArea descriptionArea;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;

    private PropertyController propertyController;
    private SectorController sectorController;
    private List<Sector> sectors;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        propertyController = new PropertyController(1); // Agent ID = 1
        sectorController = new SectorController(1);

        // Setup combos
        typeCombo.setItems(FXCollections.observableArrayList(
            "Residential", "Commercial", "Industrial"
        ));
        typeCombo.getSelectionModel().selectFirst();

        sellingMethodCombo.setItems(FXCollections.observableArrayList(
            "Fixed Price", "Auction"
        ));
        sellingMethodCombo.getSelectionModel().selectFirst();

        // Load sectors
        loadSectors();

        // Price validation - numbers only
        priceField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(old);
            }
        });

        addLog("[INFO] UC3: Add Property Listing screen loaded.");
    }

    private void loadSectors() {
        try {
            List<Sector> allSectors = sectorController.getAllSectors();
            sectors = new ArrayList<>();
            sectorCombo.getItems().clear();
            for (Sector s : allSectors) {
                String name = s.getSectorName().toUpperCase();
                if (!name.contains("DHA") && !name.contains("BAHRIA") && !name.contains("TOWN") && !name.contains("ENCLAVE")) {
                    sectors.add(s);
                    sectorCombo.getItems().add(s.getSectorName() + " (ID: " + s.getSectorId() + ")");
                }
            }
            if (!sectorCombo.getItems().isEmpty()) {
                sectorCombo.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            addLog("[ERROR] Failed to load sectors: " + e.getMessage());
        }
    }

    @FXML
    private void handleValidate() {
        try {
            String title = titleField.getText().trim();
            String baseLoc = locationField.getText().trim();
            BigDecimal price = priceField.getText().isEmpty() ? null : new BigDecimal(priceField.getText().trim());
            String type = typeCombo.getValue();
            int sectorId = getSelectedSectorId();
            
            int sectorIndex = sectorCombo.getSelectionModel().getSelectedIndex();
            String sectorName = (sectorIndex >= 0 && sectors != null && sectorIndex < sectors.size()) ? sectors.get(sectorIndex).getSectorName() : "";
            String loc = baseLoc;
            if (!sectorName.isEmpty() && !baseLoc.isEmpty()) {
                loc = baseLoc + ", " + sectorName + ", Islamabad";
            } else if (baseLoc.isEmpty()) {
                loc = "";
            }

            ValidationResult result = propertyController.validatePropertyData(title, loc, price, type, sectorId);

            if (result.isValid()) {
                showStatus("Validation PASSED - Ready to submit!", false);
                addLog("[VALID] All property fields are valid.");
            } else {
                showStatus("Validation FAILED: " + result.getErrorMessage(), true);
                addLog("[INVALID] " + result.getErrorMessage());
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid price format.", true);
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String baseLoc = locationField.getText().trim();
            String priceText = priceField.getText().trim();
            String type = typeCombo.getValue();
            String sellingMethod = sellingMethodCombo.getValue();
            int sectorId = getSelectedSectorId();
            
            int sectorIndex = sectorCombo.getSelectionModel().getSelectedIndex();
            String sectorName = (sectorIndex >= 0 && sectors != null && sectorIndex < sectors.size()) ? sectors.get(sectorIndex).getSectorName() : "";
            String loc = baseLoc;
            if (!sectorName.isEmpty()) {
                loc = baseLoc + ", " + sectorName + ", Islamabad";
            }

            if (priceText.isEmpty()) {
                showStatus("Please enter a price.", true);
                return;
            }

            BigDecimal price = new BigDecimal(priceText);

            // Call existing PropertyController.addPropertyListing()
            AddPropertyResult result = propertyController.addPropertyListing(
                title, description, loc, price, type, sellingMethod,
                sectorId, new ArrayList<>(), new ArrayList<>()
            );

            if (result.isSuccess()) {
                showStatus("SUCCESS: Property listed! ID: " + result.getPropertyId(), false);
                addLog("[SUCCESS] Property '" + title + "' added with ID: " + result.getPropertyId());
                handleClear();
            } else {
                showStatus("FAILED: " + result.getMessage(), true);
                addLog("[FAILED] " + result.getMessage());
            }
        } catch (NumberFormatException e) {
            showStatus("Invalid price format.", true);
        } catch (SQLException e) {
            showStatus("Database error: " + e.getMessage(), true);
            addLog("[ERROR] " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        titleField.clear();
        locationField.clear();
        priceField.clear();
        descriptionArea.clear();
        typeCombo.getSelectionModel().selectFirst();
        sellingMethodCombo.getSelectionModel().selectFirst();
        if (!sectorCombo.getItems().isEmpty()) {
            sectorCombo.getSelectionModel().selectFirst();
        }
        addLog("[INFO] Form cleared.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private int getSelectedSectorId() {
        int index = sectorCombo.getSelectionModel().getSelectedIndex();
        if (index >= 0 && sectors != null && index < sectors.size()) {
            return sectors.get(index).getSectorId();
        }
        return 0;
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
