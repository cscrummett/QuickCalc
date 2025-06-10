package com.quickcalc.views.panels;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Controller for the toolbar - handles top toolbar management
 * including tool selection, view controls, and quick actions
 */
public class ToolbarController {
    
    @FXML
    private ToolBar mainToolbar;
    
    // File operations
    @FXML
    private Button newButton;
    @FXML
    private Button openButton;
    @FXML
    private Button saveButton;
    
    // Edit operations
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    
    // Drawing tools
    @FXML
    private ToggleGroup drawingToolsGroup;
    @FXML
    private ToggleButton selectToolButton;
    @FXML
    private ToggleButton addSupportToolButton;
    @FXML
    private ToggleButton addLoadToolButton;
    @FXML
    private ToggleButton addMomentToolButton;
    @FXML
    private ToggleButton measureToolButton;
    
    // Support type selection
    @FXML
    private HBox supportTypeBox;
    @FXML
    private ToggleGroup supportTypeGroup;
    @FXML
    private ToggleButton fixedSupportButton;
    @FXML
    private ToggleButton pinnedSupportButton;
    @FXML
    private ToggleButton rollerSupportButton;
    
    // Load type selection
    @FXML
    private HBox loadTypeBox;
    @FXML
    private ToggleGroup loadTypeGroup;
    @FXML
    private ToggleButton pointLoadButton;
    @FXML
    private ToggleButton distributedLoadButton;
    @FXML
    private ToggleButton momentLoadButton;
    
    // View controls
    @FXML
    private Button zoomInButton;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Button fitToWindowButton;
    @FXML
    private Button resetViewButton;
    
    // Analysis controls
    @FXML
    private Button runAnalysisButton;
    @FXML
    private Button clearResultsButton;
    
    // View options
    @FXML
    private CheckBox showGridCheckBox;
    @FXML
    private CheckBox showDimensionsCheckBox;
    @FXML
    private CheckBox showLabelsCheckBox;
    
    // Status indicators
    @FXML
    private Label toolStatusLabel;
    @FXML
    private ProgressIndicator analysisProgressIndicator;
    
    private ToolbarUpdateCallback updateCallback;
    
    /**
     * Interface for communicating toolbar actions back to main controller
     */
    public interface ToolbarUpdateCallback {
        // File operations
        void onNewFile();
        void onOpenFile();
        void onSaveFile();
        
        // Edit operations
        void onUndo();
        void onRedo();
        
        // Tool selection
        void onToolSelected(DrawingTool tool);
        void onSupportTypeSelected(SupportType supportType);
        void onLoadTypeSelected(LoadType loadType);
        
        // View operations
        void onZoomIn();
        void onZoomOut();
        void onFitToWindow();
        void onResetView();
        
        // Analysis operations
        void onRunAnalysis();
        void onClearResults();
        
        // View options
        void onViewOptionChanged(ViewOption option, boolean enabled);
    }
    
    /**
     * Enum for drawing tools
     */
    public enum DrawingTool {
        SELECT, ADD_SUPPORT, ADD_LOAD, ADD_MOMENT, MEASURE
    }
    
    /**
     * Enum for support types
     */
    public enum SupportType {
        FIXED, PINNED, ROLLER
    }
    
    /**
     * Enum for load types
     */
    public enum LoadType {
        POINT_LOAD, DISTRIBUTED_LOAD, MOMENT
    }
    
    /**
     * Enum for view options
     */
    public enum ViewOption {
        SHOW_GRID, SHOW_DIMENSIONS, SHOW_LABELS
    }
    
    @FXML
    private void initialize() {
        setupToggleGroups();
        setupEventHandlers();
        setupInitialState();
        setupTooltips();
    }
    
    private void setupToggleGroups() {
        // Setup drawing tools toggle group
        drawingToolsGroup = new ToggleGroup();
        selectToolButton.setToggleGroup(drawingToolsGroup);
        addSupportToolButton.setToggleGroup(drawingToolsGroup);
        addLoadToolButton.setToggleGroup(drawingToolsGroup);
        addMomentToolButton.setToggleGroup(drawingToolsGroup);
        measureToolButton.setToggleGroup(drawingToolsGroup);
        
        // Setup support type toggle group
        supportTypeGroup = new ToggleGroup();
        fixedSupportButton.setToggleGroup(supportTypeGroup);
        pinnedSupportButton.setToggleGroup(supportTypeGroup);
        rollerSupportButton.setToggleGroup(supportTypeGroup);
        
        // Setup load type toggle group
        loadTypeGroup = new ToggleGroup();
        pointLoadButton.setToggleGroup(loadTypeGroup);
        distributedLoadButton.setToggleGroup(loadTypeGroup);
        momentLoadButton.setToggleGroup(loadTypeGroup);
    }
    
    private void setupEventHandlers() {
        // File operation handlers
        newButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onNewFile();
        });
        
        openButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onOpenFile();
        });
        
        saveButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onSaveFile();
        });
        
        // Edit operation handlers
        undoButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onUndo();
        });
        
        redoButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onRedo();
        });
        
        // Drawing tool selection handlers
        drawingToolsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null && updateCallback != null) {
                DrawingTool tool = getSelectedDrawingTool();
                updateCallback.onToolSelected(tool);
                updateToolStatus(tool);
                updateSubToolVisibility(tool);
            }
        });
        
        // Support type selection handlers
        supportTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null && updateCallback != null) {
                SupportType supportType = getSelectedSupportType();
                updateCallback.onSupportTypeSelected(supportType);
            }
        });
        
        // Load type selection handlers
        loadTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null && updateCallback != null) {
                LoadType loadType = getSelectedLoadType();
                updateCallback.onLoadTypeSelected(loadType);
            }
        });
        
        // View control handlers
        zoomInButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onZoomIn();
        });
        
        zoomOutButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onZoomOut();
        });
        
        fitToWindowButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onFitToWindow();
        });
        
        resetViewButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onResetView();
        });
        
        // Analysis control handlers
        runAnalysisButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onRunAnalysis();
        });
        
        clearResultsButton.setOnAction(e -> {
            if (updateCallback != null) updateCallback.onClearResults();
        });
        
        // View option handlers
        showGridCheckBox.setOnAction(e -> {
            if (updateCallback != null) {
                updateCallback.onViewOptionChanged(ViewOption.SHOW_GRID, showGridCheckBox.isSelected());
            }
        });
        
        showDimensionsCheckBox.setOnAction(e -> {
            if (updateCallback != null) {
                updateCallback.onViewOptionChanged(ViewOption.SHOW_DIMENSIONS, showDimensionsCheckBox.isSelected());
            }
        });
        
        showLabelsCheckBox.setOnAction(e -> {
            if (updateCallback != null) {
                updateCallback.onViewOptionChanged(ViewOption.SHOW_LABELS, showLabelsCheckBox.isSelected());
            }
        });
    }
    
    private void setupInitialState() {
        // Set default tool selection
        selectToolButton.setSelected(true);
        
        // Set default support type
        pinnedSupportButton.setSelected(true);
        
        // Set default load type
        pointLoadButton.setSelected(true);
        
        // Set default view options
        showGridCheckBox.setSelected(true);
        showDimensionsCheckBox.setSelected(true);
        showLabelsCheckBox.setSelected(false);
        
        // Initially hide sub-tool groups
        supportTypeBox.setVisible(false);
        loadTypeBox.setVisible(false);
        
        // Hide analysis progress indicator
        analysisProgressIndicator.setVisible(false);
        
        // Set initial tool status
        toolStatusLabel.setText("Select tool active");
        
        // Initially disable some buttons
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        clearResultsButton.setDisable(true);
    }
    
    private void setupTooltips() {
        // File operation tooltips
        newButton.setTooltip(new Tooltip("New beam (Ctrl+N)"));
        openButton.setTooltip(new Tooltip("Open file (Ctrl+O)"));
        saveButton.setTooltip(new Tooltip("Save file (Ctrl+S)"));
        
        // Edit operation tooltips
        undoButton.setTooltip(new Tooltip("Undo (Ctrl+Z)"));
        redoButton.setTooltip(new Tooltip("Redo (Ctrl+Y)"));
        
        // Drawing tool tooltips
        selectToolButton.setTooltip(new Tooltip("Select and move elements (S)"));
        addSupportToolButton.setTooltip(new Tooltip("Add support (P)"));
        addLoadToolButton.setTooltip(new Tooltip("Add load (L)"));
        addMomentToolButton.setTooltip(new Tooltip("Add moment (M)"));
        measureToolButton.setTooltip(new Tooltip("Measure distances (R)"));
        
        // Support type tooltips
        fixedSupportButton.setTooltip(new Tooltip("Fixed support"));
        pinnedSupportButton.setTooltip(new Tooltip("Pinned support"));
        rollerSupportButton.setTooltip(new Tooltip("Roller support"));
        
        // Load type tooltips
        pointLoadButton.setTooltip(new Tooltip("Point load"));
        distributedLoadButton.setTooltip(new Tooltip("Distributed load"));
        momentLoadButton.setTooltip(new Tooltip("Applied moment"));
        
        // View control tooltips
        zoomInButton.setTooltip(new Tooltip("Zoom in (+)"));
        zoomOutButton.setTooltip(new Tooltip("Zoom out (-)"));
        fitToWindowButton.setTooltip(new Tooltip("Fit to window (F)"));
        resetViewButton.setTooltip(new Tooltip("Reset view (R)"));
        
        // Analysis tooltips
        runAnalysisButton.setTooltip(new Tooltip("Run structural analysis (F5)"));
        clearResultsButton.setTooltip(new Tooltip("Clear analysis results"));
    }
    
    private DrawingTool getSelectedDrawingTool() {
        Toggle selected = drawingToolsGroup.getSelectedToggle();
        if (selected == selectToolButton) return DrawingTool.SELECT;
        if (selected == addSupportToolButton) return DrawingTool.ADD_SUPPORT;
        if (selected == addLoadToolButton) return DrawingTool.ADD_LOAD;
        if (selected == addMomentToolButton) return DrawingTool.ADD_MOMENT;
        if (selected == measureToolButton) return DrawingTool.MEASURE;
        return DrawingTool.SELECT; // Default
    }
    
    private SupportType getSelectedSupportType() {
        Toggle selected = supportTypeGroup.getSelectedToggle();
        if (selected == fixedSupportButton) return SupportType.FIXED;
        if (selected == pinnedSupportButton) return SupportType.PINNED;
        if (selected == rollerSupportButton) return SupportType.ROLLER;
        return SupportType.PINNED; // Default
    }
    
    private LoadType getSelectedLoadType() {
        Toggle selected = loadTypeGroup.getSelectedToggle();
        if (selected == pointLoadButton) return LoadType.POINT_LOAD;
        if (selected == distributedLoadButton) return LoadType.DISTRIBUTED_LOAD;
        if (selected == momentLoadButton) return LoadType.MOMENT;
        return LoadType.POINT_LOAD; // Default
    }
    
    private void updateToolStatus(DrawingTool tool) {
        switch (tool) {
            case SELECT:
                toolStatusLabel.setText("Select tool active - Click to select elements");
                break;
            case ADD_SUPPORT:
                toolStatusLabel.setText("Add support tool active - Click to place support");
                break;
            case ADD_LOAD:
                toolStatusLabel.setText("Add load tool active - Click to place load");
                break;
            case ADD_MOMENT:
                toolStatusLabel.setText("Add moment tool active - Click to place moment");
                break;
            case MEASURE:
                toolStatusLabel.setText("Measure tool active - Click two points to measure");
                break;
        }
    }
    
    private void updateSubToolVisibility(DrawingTool tool) {
        // Show/hide sub-tool groups based on selected tool
        supportTypeBox.setVisible(tool == DrawingTool.ADD_SUPPORT);
        loadTypeBox.setVisible(tool == DrawingTool.ADD_LOAD || tool == DrawingTool.ADD_MOMENT);
    }
    
    /**
     * Set the callback for toolbar updates
     */
    public void setUpdateCallback(ToolbarUpdateCallback callback) {
        this.updateCallback = callback;
    }
    
    /**
     * Enable/disable undo button
     */
    public void setUndoEnabled(boolean enabled) {
        undoButton.setDisable(!enabled);
    }
    
    /**
     * Enable/disable redo button
     */
    public void setRedoEnabled(boolean enabled) {
        redoButton.setDisable(!enabled);
    }
    
    /**
     * Enable/disable clear results button
     */
    public void setClearResultsEnabled(boolean enabled) {
        clearResultsButton.setDisable(!enabled);
    }
    
    /**
     * Show/hide analysis progress indicator
     */
    public void setAnalysisInProgress(boolean inProgress) {
        analysisProgressIndicator.setVisible(inProgress);
        runAnalysisButton.setDisable(inProgress);
        
        if (inProgress) {
            toolStatusLabel.setText("Running analysis...");
        } else {
            updateToolStatus(getSelectedDrawingTool());
        }
    }
    
    /**
     * Update tool status message
     */
    public void setToolStatus(String status) {
        toolStatusLabel.setText(status);
    }
    
    /**
     * Select a specific drawing tool programmatically
     */
    public void selectDrawingTool(DrawingTool tool) {
        switch (tool) {
            case SELECT:
                selectToolButton.setSelected(true);
                break;
            case ADD_SUPPORT:
                addSupportToolButton.setSelected(true);
                break;
            case ADD_LOAD:
                addLoadToolButton.setSelected(true);
                break;
            case ADD_MOMENT:
                addMomentToolButton.setSelected(true);
                break;
            case MEASURE:
                measureToolButton.setSelected(true);
                break;
        }
    }
    
    /**
     * Select a specific support type programmatically
     */
    public void selectSupportType(SupportType supportType) {
        switch (supportType) {
            case FIXED:
                fixedSupportButton.setSelected(true);
                break;
            case PINNED:
                pinnedSupportButton.setSelected(true);
                break;
            case ROLLER:
                rollerSupportButton.setSelected(true);
                break;
        }
    }
    
    /**
     * Select a specific load type programmatically
     */
    public void selectLoadType(LoadType loadType) {
        switch (loadType) {
            case POINT_LOAD:
                pointLoadButton.setSelected(true);
                break;
            case DISTRIBUTED_LOAD:
                distributedLoadButton.setSelected(true);
                break;
            case MOMENT:
                momentLoadButton.setSelected(true);
                break;
        }
    }
    
    /**
     * Get current view option states
     */
    public boolean isGridVisible() {
        return showGridCheckBox.isSelected();
    }
    
    public boolean areDimensionsVisible() {
        return showDimensionsCheckBox.isSelected();
    }
    
    public boolean areLabelsVisible() {
        return showLabelsCheckBox.isSelected();
    }
    
    /**
     * Set view option states programmatically
     */
    public void setViewOptions(boolean showGrid, boolean showDimensions, boolean showLabels) {
        showGridCheckBox.setSelected(showGrid);
        showDimensionsCheckBox.setSelected(showDimensions);
        showLabelsCheckBox.setSelected(showLabels);
    }
}