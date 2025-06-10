package com.quickcalc.views.panels;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

/**
 * Controller for the results panel - handles bottom results display
 * including analysis results, diagrams, and export options
 */
public class ResultsPanelController {
    
    @FXML
    private VBox resultsPanel;
    
    @FXML
    private TabPane resultsTabPane;
    
    // Results table tab
    @FXML
    private Tab resultsTableTab;
    @FXML
    private TableView<ResultRow> resultsTable;
    @FXML
    private TableColumn<ResultRow, String> locationColumn;
    @FXML
    private TableColumn<ResultRow, Double> shearColumn;
    @FXML
    private TableColumn<ResultRow, Double> momentColumn;
    @FXML
    private TableColumn<ResultRow, Double> deflectionColumn;
    
    // Shear diagram tab
    @FXML
    private Tab shearDiagramTab;
    @FXML
    private Canvas shearDiagramCanvas;
    @FXML
    private ScrollPane shearScrollPane;
    
    // Moment diagram tab
    @FXML
    private Tab momentDiagramTab;
    @FXML
    private Canvas momentDiagramCanvas;
    @FXML
    private ScrollPane momentScrollPane;
    
    // Deflection chart tab
    @FXML
    private Tab deflectionChartTab;
    @FXML
    private Canvas deflectionCanvas;
    @FXML
    private ScrollPane deflectionScrollPane;
    
    // Control buttons
    @FXML
    private HBox controlButtonsBox;
    @FXML
    private Button exportResultsButton;
    @FXML
    private Button printDiagramsButton;
    @FXML
    private Button clearResultsButton;
    @FXML
    private CheckBox showValuesCheckBox;
    @FXML
    private CheckBox showGridCheckBox;
    
    // Status and summary
    @FXML
    private Label analysisStatusLabel;
    @FXML
    private Label maxMomentLabel;
    @FXML
    private Label maxDeflectionLabel;
    @FXML
    private Label maxShearLabel;
    
    private ResultsUpdateCallback updateCallback;
    
    /**
     * Interface for communicating updates back to main controller
     */
    public interface ResultsUpdateCallback {
        void onExportRequested(ExportType exportType);
        void onClearResults();
        void onDiagramSettingsChanged();
    }
    
    /**
     * Enum for export types
     */
    public enum ExportType {
        PDF_REPORT, CSV_DATA, PNG_DIAGRAMS, ALL
    }
    
    /**
     * Data class for results table rows
     */
    public static class ResultRow {
        private String location;
        private Double shear;
        private Double moment;
        private Double deflection;
        
        public ResultRow(String location, Double shear, Double moment, Double deflection) {
            this.location = location;
            this.shear = shear;
            this.moment = moment;
            this.deflection = deflection;
        }
        
        // Getters for TableView
        public String getLocation() { return location; }
        public Double getShear() { return shear; }
        public Double getMoment() { return moment; }
        public Double getDeflection() { return deflection; }
        
        // Setters
        public void setLocation(String location) { this.location = location; }
        public void setShear(Double shear) { this.shear = shear; }
        public void setMoment(Double moment) { this.moment = moment; }
        public void setDeflection(Double deflection) { this.deflection = deflection; }
    }
    
    @FXML
    private void initialize() {
        setupResultsTable();
        setupCanvases();
        setupEventHandlers();
        setupInitialState();
    }
    
    private void setupResultsTable() {
        // Configure table columns
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        shearColumn.setCellValueFactory(new PropertyValueFactory<>("shear"));
        momentColumn.setCellValueFactory(new PropertyValueFactory<>("moment"));
        deflectionColumn.setCellValueFactory(new PropertyValueFactory<>("deflection"));
        
        // Format numeric columns
        shearColumn.setCellFactory(col -> new TableCell<ResultRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.3f", item));
                }
            }
        });
        
        momentColumn.setCellFactory(col -> new TableCell<ResultRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.3f", item));
                }
            }
        });
        
        deflectionColumn.setCellFactory(col -> new TableCell<ResultRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.6f", item));
                }
            }
        });
    }
    
    private void setupCanvases() {
        // Set initial canvas sizes
        double canvasWidth = 800;
        double canvasHeight = 300;
        
        shearDiagramCanvas.setWidth(canvasWidth);
        shearDiagramCanvas.setHeight(canvasHeight);
        
        momentDiagramCanvas.setWidth(canvasWidth);
        momentDiagramCanvas.setHeight(canvasHeight);
        
        deflectionCanvas.setWidth(canvasWidth);
        deflectionCanvas.setHeight(canvasHeight);
        
        // Make canvases focusable for keyboard events if needed
        shearDiagramCanvas.setFocusTraversable(true);
        momentDiagramCanvas.setFocusTraversable(true);
        deflectionCanvas.setFocusTraversable(true);
    }
    
    private void setupEventHandlers() {
        // Button handlers
        exportResultsButton.setOnAction(e -> handleExportResults());
        printDiagramsButton.setOnAction(e -> handlePrintDiagrams());
        clearResultsButton.setOnAction(e -> handleClearResults());
        
        // Checkbox handlers for diagram settings
        showValuesCheckBox.setOnAction(e -> handleDiagramSettingsChange());
        showGridCheckBox.setOnAction(e -> handleDiagramSettingsChange());
        
        // Tab selection handler to redraw diagrams when shown
        resultsTabPane.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldTab, newTab) -> {
                if (newTab == shearDiagramTab) {
                    redrawShearDiagram();
                } else if (newTab == momentDiagramTab) {
                    redrawMomentDiagram();
                } else if (newTab == deflectionChartTab) {
                    redrawDeflectionChart();
                }
            }
        );
    }
    
    private void setupInitialState() {
        // Initially disable export/print buttons until results are available
        exportResultsButton.setDisable(true);
        printDiagramsButton.setDisable(true);
        
        // Set default checkbox states
        showValuesCheckBox.setSelected(true);
        showGridCheckBox.setSelected(true);
        
        // Set initial status
        analysisStatusLabel.setText("No analysis results available");
        maxMomentLabel.setText("Max Moment: --");
        maxDeflectionLabel.setText("Max Deflection: --");
        maxShearLabel.setText("Max Shear: --");
    }
    
    @FXML
    private void handleExportResults() {
        if (updateCallback != null) {
            // Show export options dialog
            ChoiceDialog<ExportType> dialog = new ChoiceDialog<>(ExportType.PDF_REPORT, ExportType.values());
            dialog.setTitle("Export Results");
            dialog.setHeaderText("Choose export format:");
            dialog.setContentText("Export type:");
            
            dialog.showAndWait().ifPresent(exportType -> {
                updateCallback.onExportRequested(exportType);
            });
        }
    }
    
    @FXML
    private void handlePrintDiagrams() {
        // TODO: Implement print functionality
        showInfo("Print functionality not yet implemented");
    }
    
    @FXML
    private void handleClearResults() {
        if (updateCallback != null) {
            updateCallback.onClearResults();
        }
        clearAllResults();
    }
    
    @FXML
    private void handleDiagramSettingsChange() {
        if (updateCallback != null) {
            updateCallback.onDiagramSettingsChanged();
        }
        
        // Redraw current diagram with new settings
        Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == shearDiagramTab) {
            redrawShearDiagram();
        } else if (selectedTab == momentDiagramTab) {
            redrawMomentDiagram();
        } else if (selectedTab == deflectionChartTab) {
            redrawDeflectionChart();
        }
    }
    
    /**
     * Update the results panel with new analysis results
     */
    public void updateResults(List<ResultRow> results, Map<String, Double> summary) {
        // Update table
        resultsTable.getItems().clear();
        resultsTable.getItems().addAll(results);
        
        // Update summary labels
        if (summary != null) {
            maxMomentLabel.setText(String.format("Max Moment: %.3f", 
                summary.getOrDefault("maxMoment", 0.0)));
            maxDeflectionLabel.setText(String.format("Max Deflection: %.6f", 
                summary.getOrDefault("maxDeflection", 0.0)));
            maxShearLabel.setText(String.format("Max Shear: %.3f", 
                summary.getOrDefault("maxShear", 0.0)));
        }
        
        // Update status
        analysisStatusLabel.setText("Analysis completed successfully");
        
        // Enable export/print buttons
        exportResultsButton.setDisable(false);
        printDiagramsButton.setDisable(false);
        
        // Switch to results table tab
        resultsTabPane.getSelectionModel().select(resultsTableTab);
    }
    
    /**
     * Update shear diagram
     */
    public void updateShearDiagram(List<Double> positions, List<Double> shearValues) {
        drawDiagram(shearDiagramCanvas, positions, shearValues, "Shear Force", "lbs", Color.BLUE);
    }
    
    /**
     * Update moment diagram
     */
    public void updateMomentDiagram(List<Double> positions, List<Double> momentValues) {
        drawDiagram(momentDiagramCanvas, positions, momentValues, "Bending Moment", "lb-ft", Color.RED);
    }
    
    /**
     * Update deflection chart
     */
    public void updateDeflectionChart(List<Double> positions, List<Double> deflectionValues) {
        drawDiagram(deflectionCanvas, positions, deflectionValues, "Deflection", "in", Color.GREEN);
    }
    
    private void drawDiagram(Canvas canvas, List<Double> positions, List<Double> values, 
                           String title, String units, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Clear canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        if (positions == null || values == null || positions.isEmpty() || values.isEmpty()) {
            // Draw "No data" message
            gc.setFill(Color.GRAY);
            gc.fillText("No " + title.toLowerCase() + " data available", 
                canvas.getWidth()/2 - 80, canvas.getHeight()/2);
            return;
        }
        
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        double margin = 50;
        double plotWidth = canvasWidth - 2 * margin;
        double plotHeight = canvasHeight - 2 * margin;
        
        // Find min/max values for scaling
        double minPos = positions.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxPos = positions.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double minVal = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxVal = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        
        // Add some padding to value range
        double valueRange = Math.abs(maxVal - minVal);
        if (valueRange == 0) valueRange = 1;
        minVal -= valueRange * 0.1;
        maxVal += valueRange * 0.1;
        
        // Draw grid if enabled
        if (showGridCheckBox.isSelected()) {
            drawGrid(gc, margin, plotWidth, plotHeight, minPos, maxPos, minVal, maxVal);
        }
        
        // Draw axes
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        
        // X-axis
        gc.strokeLine(margin, canvasHeight - margin, canvasWidth - margin, canvasHeight - margin);
        // Y-axis  
        gc.strokeLine(margin, margin, margin, canvasHeight - margin);
        
        // Draw zero line if needed
        if (minVal < 0 && maxVal > 0) {
            double zeroY = margin + plotHeight * (maxVal - 0) / (maxVal - minVal);
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(margin, zeroY, canvasWidth - margin, zeroY);
        }
        
        // Draw the diagram
        gc.setStroke(color);
        gc.setLineWidth(2);
        
        for (int i = 0; i < positions.size() - 1; i++) {
            double x1 = margin + plotWidth * (positions.get(i) - minPos) / (maxPos - minPos);
            double y1 = margin + plotHeight * (maxVal - values.get(i)) / (maxVal - minVal);
            double x2 = margin + plotWidth * (positions.get(i + 1) - minPos) / (maxPos - minPos);
            double y2 = margin + plotHeight * (maxVal - values.get(i + 1)) / (maxVal - minVal);
            
            gc.strokeLine(x1, y1, x2, y2);
        }
        
        // Draw values if enabled
        if (showValuesCheckBox.isSelected()) {
            drawValues(gc, positions, values, minPos, maxPos, minVal, maxVal, 
                      margin, plotWidth, plotHeight, units);
        }
        
        // Draw title and labels
        gc.setFill(Color.BLACK);
        gc.fillText(title + " Diagram", canvasWidth/2 - 50, 20);
        gc.fillText("Position (ft)", canvasWidth/2 - 30, canvasHeight - 10);
        
        // Rotate and draw Y-axis label
        gc.save();
        gc.translate(15, canvasHeight/2);
        gc.rotate(-Math.PI/2);
        gc.fillText(title + " (" + units + ")", -50, 0);
        gc.restore();
    }
    
    private void drawGrid(GraphicsContext gc, double margin, double plotWidth, double plotHeight,
                         double minPos, double maxPos, double minVal, double maxVal) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        
        // Vertical grid lines
        int numVerticalLines = 10;
        for (int i = 0; i <= numVerticalLines; i++) {
            double x = margin + plotWidth * i / numVerticalLines;
            gc.strokeLine(x, margin, x, margin + plotHeight);
        }
        
        // Horizontal grid lines
        int numHorizontalLines = 8;
        for (int i = 0; i <= numHorizontalLines; i++) {
            double y = margin + plotHeight * i / numHorizontalLines;
            gc.strokeLine(margin, y, margin + plotWidth, y);
        }
    }
    
    private void drawValues(GraphicsContext gc, List<Double> positions, List<Double> values,
                           double minPos, double maxPos, double minVal, double maxVal,
                           double margin, double plotWidth, double plotHeight, String units) {
        gc.setFill(Color.BLACK);
        
        // Only draw values at key points to avoid clutter
        int step = Math.max(1, positions.size() / 10);
        for (int i = 0; i < positions.size(); i += step) {
            double x = margin + plotWidth * (positions.get(i) - minPos) / (maxPos - minPos);
            double y = margin + plotHeight * (maxVal - values.get(i)) / (maxVal - minVal);
            
            String valueText = String.format("%.2f", values.get(i));
            gc.fillText(valueText, x - 15, y - 5);
        }
    }
    
    private void redrawShearDiagram() {
        // This would be called with current shear data
        // For now, just clear the canvas
        GraphicsContext gc = shearDiagramCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, shearDiagramCanvas.getWidth(), shearDiagramCanvas.getHeight());
    }
    
    private void redrawMomentDiagram() {
        // This would be called with current moment data
        // For now, just clear the canvas
        GraphicsContext gc = momentDiagramCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, momentDiagramCanvas.getWidth(), momentDiagramCanvas.getHeight());
    }
    
    private void redrawDeflectionChart() {
        // This would be called with current deflection data
        // For now, just clear the canvas
        GraphicsContext gc = deflectionCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, deflectionCanvas.getWidth(), deflectionCanvas.getHeight());
    }
    
    /**
     * Clear all results from the panel
     */
    public void clearAllResults() {
        resultsTable.getItems().clear();
        
        // Clear canvases
        shearDiagramCanvas.getGraphicsContext2D().clearRect(
            0, 0, shearDiagramCanvas.getWidth(), shearDiagramCanvas.getHeight());
        momentDiagramCanvas.getGraphicsContext2D().clearRect(
            0, 0, momentDiagramCanvas.getWidth(), momentDiagramCanvas.getHeight());
        deflectionCanvas.getGraphicsContext2D().clearRect(
            0, 0, deflectionCanvas.getWidth(), deflectionCanvas.getHeight());
        
        // Reset summary labels
        analysisStatusLabel.setText("Results cleared");
        maxMomentLabel.setText("Max Moment: --");
        maxDeflectionLabel.setText("Max Deflection: --");
        maxShearLabel.setText("Max Shear: --");
        
        // Disable export/print buttons
        exportResultsButton.setDisable(true);
        printDiagramsButton.setDisable(true);
    }
    
    /**
     * Set the callback for results updates
     */
    public void setUpdateCallback(ResultsUpdateCallback callback) {
        this.updateCallback = callback;
    }
    
    /**
     * Show/hide the results panel
     */
    public void setVisible(boolean visible) {
        resultsPanel.setVisible(visible);
        resultsPanel.setManaged(visible);
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}