package com.quickcalc.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.File;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;
import com.quickcalc.views.components.BeamCanvas;

/**
 * Controller for the main application window
 */
public class MainController {

    @FXML
    private Pane beamCanvasContainer;
    
    @FXML
    private HBox statusBar;
    
    @FXML
    private Label coordinatesLabel;
    
    @FXML
    private VBox propertiesPanel;
    
    @FXML
    private VBox resultsPanel;
    
    // Reference to the primary stage
    private Stage primaryStage;
    
    /**
     * Initialize the controller
     * This method is automatically called after the FXML is loaded
     */
    // The beam model and canvas
    private BeamModel beamModel;
    private BeamCanvas beamCanvas;
    
    @FXML
    private void initialize() {
        // This will be called when the FXML is loaded
        System.out.println("MainController initialized");
        
        // Initialize the beam model
        beamModel = new BeamModel(20.0); // 20-foot beam
        
        // Clear default supports
        beamModel.getSupports().clear();
        
        // Add a pinned support at the left end and a fixed support at the right end
        beamModel.addSupport(new Support(0.0, Support.Type.PINNED));
        beamModel.addSupport(new Support(beamModel.getLength(), Support.Type.FIXED));
        
        // Add test loads for Stage 3 testing
        beamModel.addLoad(new Load(10.0, -5.0, Load.Type.POINT)); // Point load at 10 ft
        beamModel.addLoad(new Load(5.0, 15.0, -2.0)); // Distributed load from 5-15 ft
        beamModel.addLoad(new Load(15.0, 10.0, Load.Type.MOMENT)); // Moment at 15 ft
        
        // Create and add the beam canvas to the container
        beamCanvas = new BeamCanvas(beamCanvasContainer.getWidth(), beamCanvasContainer.getHeight());
        beamCanvas.setBeamModel(beamModel); // Set the beam model on the canvas
        beamCanvasContainer.getChildren().add(beamCanvas);
        
        // Bind the canvas size to the container size
        beamCanvas.widthProperty().bind(beamCanvasContainer.widthProperty());
        beamCanvas.heightProperty().bind(beamCanvasContainer.heightProperty());
        
        // Set up key event handler at the container level
        beamCanvasContainer.setOnKeyPressed(this::handleKeyPressed);
        beamCanvasContainer.setFocusTraversable(true);
        
        // Request focus for the container to receive key events
        Platform.runLater(() -> beamCanvasContainer.requestFocus());
    }
    
    /**
     * Handle key press events
     */
    private void handleKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.F) {
            // F key pressed, zoom to fit
            beamCanvas.fitViewToBeam();
            beamCanvas.draw();
            event.consume();
        }
    }
    
    /**
     * Set the primary stage reference
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    // /**
    //  * Update the coordinates display in the status bar
    //  * 
    //  * @param event Mouse event
    //  */
    // private void updateCoordinatesDisplay(MouseEvent event) {
        // if (beamCanvas != null) {
            // Convert screen coordinates to engineering coordinates
            // Point2D engineeringPoint = beamCanvas.screenToEngineering(event.getX(), event.getY());
            
            // Update the coordinates label
            // coordinatesLabel.setText(String.format("Position: (%.2f', %.2f')", 
            //         engineeringPoint.getX(), engineeringPoint.getY()));
        // }
    // }
    
    // File menu handlers
    
    @FXML
    private void handleNew() {
        System.out.println("New project requested");
        
        // Reset the beam model with default values
        beamModel.reset(20.0);
        
        // Add a sample point load for testing
        beamModel.addLoad(new Load(10.0, 5.0, Load.Type.POINT));
        
        // Redraw the canvas
        beamCanvas.draw();
    }
    
    @FXML
    private void handleOpen() {
        System.out.println("Open project requested");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Beam Project");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("QuickCalc Projects", "*.json")
        );
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            // TODO: Implement file loading
        }
    }
    
    @FXML
    private void handleSave() {
        System.out.println("Save project requested");
        // TODO: Implement save functionality
    }
    
    @FXML
    private void handleSaveAs() {
        System.out.println("Save As project requested");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Beam Project");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("QuickCalc Projects", "*.json")
        );
        
        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile != null) {
            System.out.println("Save to file: " + selectedFile.getAbsolutePath());
            // TODO: Implement file saving
        }
    }
    
    @FXML
    private void handleExit() {
        System.out.println("Exit requested");
        Platform.exit();
    }
    
    // Edit menu handlers
    
    @FXML
    private void handleUndo() {
        System.out.println("Undo requested");
        // TODO: Implement undo functionality
    }
    
    @FXML
    private void handleRedo() {
        System.out.println("Redo requested");
        // TODO: Implement redo functionality
    }
    
    @FXML
    private void handlePreferences() {
        System.out.println("Preferences requested");
        // TODO: Implement preferences dialog
    }
    
    // Analysis menu handlers
    
    @FXML
    private void handleRunAnalysis() {
        System.out.println("Run Analysis requested");
        // TODO: Implement analysis functionality
        
        // For now, just show a placeholder alert
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Analysis");
        alert.setHeaderText("Analysis Results");
        alert.setContentText("Analysis will be implemented in a future stage.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleClearResults() {
        System.out.println("Clear Results requested");
        // TODO: Implement clear results functionality
    }
    
    // Help menu handlers
    
    @FXML
    private void handleAbout() {
        System.out.println("About requested");
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About QuickCalc");
        alert.setHeaderText("QuickCalc - Structural Beam Analysis");
        alert.setContentText("Version 0.1.0\n\nA cross-platform desktop application for structural engineers to analyze and design single and multi-span beams.");
        alert.showAndWait();
    }
    
    // Toolbar handlers
    
    @FXML
    private void handleAddSupport() {
        System.out.println("Add Support requested");
        // TODO: Implement add support functionality
    }
    
    @FXML
    private void handleAddLoad() {
        System.out.println("Add Load requested");
        // TODO: Implement add load functionality
    }
}
