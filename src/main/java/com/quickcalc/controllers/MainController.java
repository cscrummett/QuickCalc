package com.quickcalc.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;
import com.quickcalc.services.BeamDataService;
import com.quickcalc.services.CanvasManager;
import com.quickcalc.services.FileService;
import com.quickcalc.services.MenuActionHandler;
import com.quickcalc.utils.DimensionFormatter;
import com.quickcalc.views.components.BeamCanvas;
import com.quickcalc.views.panels.PropertiesPanelController;
import com.quickcalc.views.panels.ResultsPanelController;
import com.quickcalc.views.panels.ToolbarController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Main controller - coordinates between panel controllers and services
 * Reduced responsibility: only handles coordination, not specific UI logic
 */
public class MainController implements 
    MenuActionHandler.ModelUpdateCallback,
    PropertiesPanelController.PanelUpdateCallback,
    ResultsPanelController.ResultsUpdateCallback,
    ToolbarController.ToolbarUpdateCallback {

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
    
    // Services
    private BeamDataService beamDataService;
    private FileService fileService;
    private MenuActionHandler menuActionHandler;
    private CanvasManager canvasManager;
    
    // Panel controllers
    private PropertiesPanelController propertiesPanelController;
    private ResultsPanelController resultsPanelController;
    private ToolbarController toolbarController;
    
    @FXML
    private void initialize() {
        System.out.println("MainController initialized");
        
        initializeServices();
        initializeBeamModel();
        initializeCanvas();
        initializePanelControllers();
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
        
        // Set up coordinate update callback
        canvasManager.setCoordinateUpdateCallback(this::updateCoordinateDisplay);
    }
    
    private void initializePanelControllers() {
        try {
            // Load PropertiesPanel from FXML
            FXMLLoader propertiesLoader = new FXMLLoader(getClass().getResource("/fxml/properties-panel.fxml"));
            VBox propertiesPanelUI = propertiesLoader.load();
            propertiesPanelController = propertiesLoader.getController();
            
            // Replace the placeholder properties panel with the loaded one
            propertiesPanel.getChildren().clear();
            propertiesPanel.getChildren().add(propertiesPanelUI);
            
            // Initialize other panel controllers (without FXML for now)
            resultsPanelController = new ResultsPanelController();
            toolbarController = new ToolbarController();
            
            // Set up callbacks for inter-panel communication
            propertiesPanelController.setUpdateCallback(this);
            resultsPanelController.setUpdateCallback(this);
            toolbarController.setUpdateCallback(this);
            
            // Update panel controllers with initial model
            propertiesPanelController.updateModel(beamModel);
            
        } catch (IOException e) {
            System.err.println("Error loading panel FXML: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: create simple placeholder panels
            createSimplePlaceholderPanels();
        }
    }
    
    private void createSimplePlaceholderPanels() {
        // Create simple placeholder for properties panel
        Label placeholderLabel = new Label("Properties Panel\n(FXML loading failed)");
        placeholderLabel.setStyle("-fx-padding: 20; -fx-text-alignment: center;");
        propertiesPanel.getChildren().clear();
        propertiesPanel.getChildren().add(placeholderLabel);
        
        // Initialize controllers without FXML dependencies
        resultsPanelController = new ResultsPanelController();
        toolbarController = new ToolbarController();
        
        // Set up callbacks
        resultsPanelController.setUpdateCallback(this);
        toolbarController.setUpdateCallback(this);
    }
    
    private void setupMenuActionHandler() {
        if (fileService != null) {
            menuActionHandler = new MenuActionHandler(beamDataService, fileService);
            menuActionHandler.setBeamModel(beamModel);
            menuActionHandler.setBeamCanvas(canvasManager.getBeamCanvas());
            menuActionHandler.setModelUpdateCallback(this);
        }
    }
    
    // MenuActionHandler.ModelUpdateCallback implementation
    @Override
    public void onModelUpdated(BeamModel newModel) {
        this.beamModel = newModel;
        canvasManager.setBeamModel(newModel);
        if (propertiesPanelController != null) {
            propertiesPanelController.updateModel(newModel);
        }
    }
    
    // PropertiesPanelController.PanelUpdateCallback implementation
    @Override
    public void onBeamPropertiesChanged(BeamModel updatedModel) {
        this.beamModel = updatedModel;
        canvasManager.setBeamModel(updatedModel);
    }
    
    @Override
    public void onSupportAdded(Support support) {
        beamModel.addSupport(support);
        canvasManager.setBeamModel(beamModel);
        if (propertiesPanelController != null) {
            propertiesPanelController.updateModel(beamModel);
        }
    }
    
    @Override
    public void onSupportRemoved(Support support) {
        beamModel.removeSupport(support);
        canvasManager.setBeamModel(beamModel);
        if (propertiesPanelController != null) {
            propertiesPanelController.updateModel(beamModel);
        }
    }
    
    @Override
    public void onLoadAdded(Load load) {
        beamModel.addLoad(load);
        canvasManager.setBeamModel(beamModel);
        if (propertiesPanelController != null) {
            propertiesPanelController.updateModel(beamModel);
        }
    }
    
    @Override
    public void onLoadRemoved(Load load) {
        beamModel.removeLoad(load);
        canvasManager.setBeamModel(beamModel);
        if (propertiesPanelController != null) {
            propertiesPanelController.updateModel(beamModel);
        }
    }
    
    @Override
    public void onAnalysisRequested() {
        runAnalysis();
    }
    
    // ResultsPanelController.ResultsUpdateCallback implementation
    @Override
    public void onExportRequested(ResultsPanelController.ExportType exportType) {
        // TODO: Implement export functionality
        System.out.println("Export requested: " + exportType);
    }
    
    @Override
    public void onClearResults() {
        if (resultsPanelController != null) {
            resultsPanelController.clearAllResults();
        }
        if (toolbarController != null) {
            toolbarController.setClearResultsEnabled(false);
        }
    }
    
    @Override
    public void onDiagramSettingsChanged() {
        // TODO: Redraw diagrams with new settings
        System.out.println("Diagram settings changed");
    }
    
    // ToolbarController.ToolbarUpdateCallback implementation
    @Override
    public void onNewFile() {
        if (menuActionHandler != null) {
            menuActionHandler.handleNew();
        }
    }
    
    @Override
    public void onOpenFile() {
        if (menuActionHandler != null) {
            menuActionHandler.handleOpen();
        }
    }
    
    @Override
    public void onSaveFile() {
        if (menuActionHandler != null) {
            menuActionHandler.handleSave();
        }
    }
    
    @Override
    public void onUndo() {
        if (menuActionHandler != null) {
            menuActionHandler.handleUndo();
        }
    }
    
    @Override
    public void onRedo() {
        if (menuActionHandler != null) {
            menuActionHandler.handleRedo();
        }
    }
    
    @Override
    public void onToolSelected(ToolbarController.DrawingTool tool) {
        // TODO: Update canvas mode based on selected tool
        System.out.println("Tool selected: " + tool);
    }
    
    @Override
    public void onSupportTypeSelected(ToolbarController.SupportType supportType) {
        // TODO: Set default support type for next placement
        System.out.println("Support type selected: " + supportType);
    }
    
    @Override
    public void onLoadTypeSelected(ToolbarController.LoadType loadType) {
        // TODO: Set default load type for next placement
        System.out.println("Load type selected: " + loadType);
    }
    
    @Override
    public void onZoomIn() {
        if (menuActionHandler != null) {
            menuActionHandler.handleZoomIn();
        }
    }
    
    @Override
    public void onZoomOut() {
        if (menuActionHandler != null) {
            menuActionHandler.handleZoomOut();
        }
    }
    
    @Override
    public void onFitToWindow() {
        if (menuActionHandler != null) {
            menuActionHandler.handleFitToWindow();
        }
    }
    
    @Override
    public void onResetView() {
        // TODO: Implement reset view
        System.out.println("Reset view requested");
    }
    
    @Override
    public void onRunAnalysis() {
        runAnalysis();
    }
    
    @Override
    public void onViewOptionChanged(ToolbarController.ViewOption option, boolean enabled) {
        // TODO: Update canvas display options
        System.out.println("View option changed: " + option + " = " + enabled);
    }
    
    private void runAnalysis() {
        if (menuActionHandler != null) {
            if (toolbarController != null) {
                toolbarController.setAnalysisInProgress(true);
            }
            
            // TODO: Replace with actual analysis
            // For now, create dummy results
            List<ResultsPanelController.ResultRow> dummyResults = createDummyResults();
            Map<String, Double> dummySummary = createDummySummary();
            
            if (resultsPanelController != null) {
                resultsPanelController.updateResults(dummyResults, dummySummary);
            }
            if (toolbarController != null) {
                toolbarController.setClearResultsEnabled(true);
                toolbarController.setAnalysisInProgress(false);
            }
        }
    }
    
    private List<ResultsPanelController.ResultRow> createDummyResults() {
        // TODO: Replace with actual analysis results
        return List.of(
            new ResultsPanelController.ResultRow("0.0'", 0.0, 0.0, 0.0),
            new ResultsPanelController.ResultRow("5.0'", 500.0, 1250.0, -0.025),
            new ResultsPanelController.ResultRow("10.0'", 0.0, 0.0, -0.050),
            new ResultsPanelController.ResultRow("15.0'", -500.0, -1250.0, -0.025),
            new ResultsPanelController.ResultRow("20.0'", 0.0, 0.0, 0.0)
        );
    }
    
    private Map<String, Double> createDummySummary() {
        // TODO: Replace with actual analysis summary
        Map<String, Double> summary = new HashMap<>();
        summary.put("maxMoment", 1250.0);
        summary.put("maxShear", 500.0);
        summary.put("maxDeflection", 0.050);
        return summary;
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        fileService = new FileService(primaryStage);
        setupMenuActionHandler();
    }
    
    // FXML Event Handlers - delegate to callback implementations
    
    @FXML
    private void handleNew() {
        onNewFile();
    }
    
    @FXML
    private void handleOpen() {
        onOpenFile();
    }
    
    @FXML
    private void handleSave() {
        onSaveFile();
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
        onUndo();
    }
    
    @FXML
    private void handleRedo() {
        onRedo();
    }
    
    @FXML
    private void handlePreferences() {
        if (menuActionHandler != null) {
            menuActionHandler.handlePreferences();
        }
    }
    
    @FXML
    private void handleRunAnalysis() {
        onRunAnalysis();
    }
    
    @FXML
    private void handleClearResults() {
        onClearResults();
    }
    
    @FXML
    private void handleAbout() {
        if (menuActionHandler != null) {
            menuActionHandler.handleAbout();
        }
    }
    
    @FXML
    private void handleAddSupport() {
        onToolSelected(ToolbarController.DrawingTool.ADD_SUPPORT);
    }
    
    @FXML
    private void handleAddLoad() {
        onToolSelected(ToolbarController.DrawingTool.ADD_LOAD);
    }
    
    /**
     * Update the coordinate display in the status bar
     * This method is called by the mouse event handler when the mouse moves
     * 
     * @param worldX X-coordinate in engineering units (feet)
     * @param worldY Y-coordinate in engineering units (feet)
     */
    private void updateCoordinateDisplay(double worldX, double worldY) {
        // For now, just show mouse position. Later we can enhance this to show 
        // selected element positions when an element is selected
        if (coordinatesLabel != null) {
            coordinatesLabel.setText(DimensionFormatter.formatCoordinates(worldX, worldY));
        }
    }
}
