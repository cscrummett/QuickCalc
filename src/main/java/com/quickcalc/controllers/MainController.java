package com.quickcalc.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.quickcalc.models.BeamModel;
import com.quickcalc.services.BeamDataService;
import com.quickcalc.services.CanvasManager;
import com.quickcalc.services.FileService;
import com.quickcalc.services.MenuActionHandler;
import com.quickcalc.views.components.BeamCanvas;

/**
 * Controller for the main application window
 */
public class MainController implements MenuActionHandler.ModelUpdateCallback {

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
    
    private Stage primaryStage;
    private BeamModel beamModel;
    
    private BeamDataService beamDataService;
    private FileService fileService;
    private MenuActionHandler menuActionHandler;
    private CanvasManager canvasManager;
    
    @FXML
    private void initialize() {
        System.out.println("MainController initialized");
        
        initializeServices();
        initializeBeamModel();
        initializeCanvas();
        setupMenuActionHandler();
    }
    
    private void initializeServices() {
        beamDataService = new BeamDataService();
        canvasManager = new CanvasManager();
    }
    
    private void initializeBeamModel() {
        beamModel = beamDataService.createSampleBeam();
    }
    
    private void initializeCanvas() {
        canvasManager.initializeCanvas(beamCanvasContainer, beamModel);
    }
    
    private void setupMenuActionHandler() {
        if (fileService != null) {
            menuActionHandler = new MenuActionHandler(beamDataService, fileService);
            menuActionHandler.setBeamModel(beamModel);
            menuActionHandler.setBeamCanvas(canvasManager.getBeamCanvas());
            menuActionHandler.setModelUpdateCallback(this);
        }
    }
    
    @Override
    public void onModelUpdated(BeamModel newModel) {
        this.beamModel = newModel;
        canvasManager.setBeamModel(newModel);
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        fileService = new FileService(primaryStage);
        setupMenuActionHandler();
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
    
    // Menu handlers - delegate to MenuActionHandler
    
    @FXML
    private void handleNew() {
        if (menuActionHandler != null) {
            menuActionHandler.handleNew();
        }
    }
    
    @FXML
    private void handleOpen() {
        if (menuActionHandler != null) {
            menuActionHandler.handleOpen();
        }
    }
    
    @FXML
    private void handleSave() {
        if (menuActionHandler != null) {
            menuActionHandler.handleSave();
        }
    }
    
    @FXML
    private void handleSaveAs() {
        if (menuActionHandler != null) {
            menuActionHandler.handleSaveAs();
        }
    }
    
    @FXML
    private void handleExit() {
        if (menuActionHandler != null) {
            menuActionHandler.handleExit();
        }
    }
    
    @FXML
    private void handleUndo() {
        if (menuActionHandler != null) {
            menuActionHandler.handleUndo();
        }
    }
    
    @FXML
    private void handleRedo() {
        if (menuActionHandler != null) {
            menuActionHandler.handleRedo();
        }
    }
    
    @FXML
    private void handlePreferences() {
        if (menuActionHandler != null) {
            menuActionHandler.handlePreferences();
        }
    }
    
    @FXML
    private void handleRunAnalysis() {
        if (menuActionHandler != null) {
            menuActionHandler.handleRunAnalysis();
        }
    }
    
    @FXML
    private void handleClearResults() {
        if (menuActionHandler != null) {
            menuActionHandler.handleClearResults();
        }
    }
    
    @FXML
    private void handleAbout() {
        if (menuActionHandler != null) {
            menuActionHandler.handleAbout();
        }
    }
    
    // Toolbar handlers
    
    @FXML
    private void handleAddSupport() {
        System.out.println("Add Support requested");
    }
    
    @FXML
    private void handleAddLoad() {
        System.out.println("Add Load requested");
    }
    
    @FXML
    private void handleZoomIn() {
        if (menuActionHandler != null) {
            menuActionHandler.handleZoomIn();
        }
    }
    
    @FXML
    private void handleZoomOut() {
        if (menuActionHandler != null) {
            menuActionHandler.handleZoomOut();
        }
    }
    
    @FXML
    private void handleFitToWindow() {
        if (menuActionHandler != null) {
            menuActionHandler.handleFitToWindow();
        }
    }
}
