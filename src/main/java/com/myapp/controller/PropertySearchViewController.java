package com.myapp.controller;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;
import com.myapp.model.Sector;
import com.myapp.service.PropertySearchService;
import com.myapp.service.PropertySearchService.SearchResult;
import com.myapp.service.FilterValidator;
import com.myapp.service.FilterValidator.ValidationResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UC4: Property Search with Ranking (NON-CRUD) - JavaFX Controller
 * Uses PropertySearchService -> SimpleRankingService for NON-CRUD scoring
 */
public class PropertySearchViewController implements Initializable {

    @FXML private TextField locationField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> sectorCombo;
    @FXML private TableView<Property> resultsTable;
    @FXML private TableColumn<Property, Integer> colRank;
    @FXML private TableColumn<Property, Integer> colScore;
    @FXML private TableColumn<Property, String> colTitle;
    @FXML private TableColumn<Property, String> colLocation;
    @FXML private TableColumn<Property, BigDecimal> colPrice;
    @FXML private TableColumn<Property, String> colType;
    @FXML private TableColumn<Property, Integer> colSector;
    @FXML private Label resultsLabel;
    @FXML private Label rankingInfoLabel;
    @FXML private TextArea messageArea;

    private PropertySearchService searchService;
    private FilterValidator filterValidator;
    private SectorController sectorController;
    private List<Sector> sectors;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchService = new PropertySearchService();
        filterValidator = new FilterValidator();
        sectorController = new SectorController(0);

        setupTableColumns();
        loadSectors();

        // Property type combo
        typeCombo.setItems(FXCollections.observableArrayList(
            "", "Residential", "Commercial", "Industrial"
        ));
        typeCombo.getSelectionModel().selectFirst();

        // Price field validation - numbers only
        minPriceField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) minPriceField.setText(old);
        });
        maxPriceField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) maxPriceField.setText(old);
        });

        // Load all properties on startup
        handleReset();
        addMessage("[INFO] UC4: Search module loaded. Enter filters and click Search.");
    }

    private void setupTableColumns() {
        colRank.setCellValueFactory(new PropertyValueFactory<>("rankPosition"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("relevanceScore"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colType.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
        colSector.setCellValueFactory(new PropertyValueFactory<>("sectorId"));

        // Gold/Silver/Bronze colors for rank
        colRank.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer rank, boolean empty) {
                super.updateItem(rank, empty);
                if (empty || rank == null) { setText(null); setStyle(""); return; }
                setText("#" + rank);
                setAlignment(Pos.CENTER);
                if (rank == 1) setStyle("-fx-text-fill: #D4AF37; -fx-font-weight: bold; -fx-font-size: 14px;");
                else if (rank == 2) setStyle("-fx-text-fill: #C0C0C0; -fx-font-weight: bold;");
                else if (rank == 3) setStyle("-fx-text-fill: #CD7F32; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: -fx-text-light;");
            }
        });

        // Click-to-expand text cell factory for long content
        javafx.util.Callback<TableColumn<Property, String>, TableCell<Property, String>> textCellFactory = column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setOnMouseClicked(null);
                } else {
                    setText(item);
                    setOnMouseClicked(e -> {
                        if (e.getClickCount() == 1) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Detailed Information");
                            alert.setHeaderText(null);
                            TextArea area = new TextArea(item);
                            area.setWrapText(true);
                            area.setEditable(false);
                            area.setPrefRowCount(4);
                            alert.getDialogPane().setContent(area);
                            alert.showAndWait();
                        }
                    });
                }
            }
        };

        colTitle.setCellFactory(textCellFactory);
        colLocation.setCellFactory(textCellFactory);
        colType.setCellFactory(textCellFactory);

        colSector.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer sectorId, boolean empty) {
                super.updateItem(sectorId, empty);
                if (empty || sectorId == null || sectorId == 0) { setText(null); return; }
                String sectorName = "Sector " + sectorId;
                if (sectors != null) {
                    for (Sector s : sectors) {
                        if (s.getSectorId() == sectorId) {
                            sectorName = s.getSectorName();
                            break;
                        }
                    }
                }
                setText(sectorName);
                setAlignment(Pos.CENTER);
            }
        });

        // Color-coded score
        colScore.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) { setText(null); return; }
                setText(score + "/40");
                setAlignment(Pos.CENTER);
                if (score >= 30) setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                else if (score >= 20) setStyle("-fx-text-fill: #ffc107;");
                else if (score >= 10) setStyle("-fx-text-fill: #fd7e14;");
                else setStyle("-fx-text-fill: -fx-text-muted;");
            }
        });
    }

    private void loadSectors() {
        try {
            List<Sector> allSectors = sectorController.getAllSectors();
            sectors = new java.util.ArrayList<>();
            
            sectorCombo.getItems().clear();
            sectorCombo.getItems().add("All Sectors");
            for (Sector s : allSectors) {
                String name = s.getSectorName().toUpperCase();
                // Filter only Islamabad sectors (F, G, I, H series, etc., no DHA, no Bahria)
                if (!name.contains("DHA") && !name.contains("BAHRIA") && !name.contains("TOWN") && !name.contains("ENCLAVE")) {
                    sectors.add(s);
                    sectorCombo.getItems().add(s.getSectorName());
                }
            }
            sectorCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            addMessage("[ERROR] Failed to load sectors: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        SearchCriteria criteria = new SearchCriteria();

        // Location
        String loc = locationField.getText().trim();
        if (!loc.isEmpty()) criteria.setLocation(loc);

        // Price range
        try {
            String minText = minPriceField.getText().trim();
            if (!minText.isEmpty()) criteria.setMinPrice(new BigDecimal(minText));
        } catch (NumberFormatException e) { addMessage("[WARNING] Invalid min price"); }

        try {
            String maxText = maxPriceField.getText().trim();
            if (!maxText.isEmpty()) criteria.setMaxPrice(new BigDecimal(maxText));
        } catch (NumberFormatException e) { addMessage("[WARNING] Invalid max price"); }

        // Type
        String type = typeCombo.getValue();
        if (type != null && !type.isEmpty()) criteria.setPropertyType(type);

        // Sector
        int sectorIndex = sectorCombo.getSelectionModel().getSelectedIndex();
        if (sectorIndex > 0 && sectors != null && sectorIndex - 1 < sectors.size()) {
            criteria.setSectorId(sectors.get(sectorIndex - 1).getSectorId());
        }

        // Validate
        ValidationResult validation = filterValidator.validate(criteria);
        if (!validation.isValid()) {
            addMessage("[VALIDATION ERROR] " + validation.getMessage());
            return;
        }

        // Execute ranked search (NON-CRUD)
        addMessage("[INFO] Searching with NON-CRUD ranking algorithm...");
        SearchResult result = searchService.searchProperties(criteria);
        displayResults(result);
    }

    @FXML
    private void handleClearFilters() {
        locationField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        typeCombo.getSelectionModel().selectFirst();
        sectorCombo.getSelectionModel().selectFirst();
        addMessage("[INFO] Filters cleared.");
    }

    @FXML
    private void handleReset() {
        handleClearFilters();
        SearchCriteria empty = new SearchCriteria();
        SearchResult result = searchService.searchProperties(empty);
        displayResults(result);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) resultsTable.getScene().getWindow();
        stage.close();
    }

    private void displayResults(SearchResult result) {
        if (result.isSuccess()) {
            resultsTable.setItems(FXCollections.observableArrayList(result.getProperties()));
            resultsLabel.setText(result.getMessage());
            addMessage("[SUCCESS] " + result.getMessage());

            // Top result info
            if (result.getProperties() != null && !result.getProperties().isEmpty()) {
                Property top = result.getProperties().get(0);
                rankingInfoLabel.setText("Top Result: " + top.getTitle() +
                    " | Score: " + top.getRelevanceScore() + "/40 | Rank: #" + top.getRankPosition());
            }

            if (result.getSearchTimeMs() > 3000) {
                addMessage("[WARNING] Search exceeded 3 second requirement!");
            }
        } else {
            resultsLabel.setText("Error: " + result.getMessage());
            addMessage("[FAILED] " + result.getMessage());
        }
    }

    private void addMessage(String message) {
        Platform.runLater(() -> {
            messageArea.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n");
        });
    }
}
