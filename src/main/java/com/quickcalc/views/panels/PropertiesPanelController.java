package com.quickcalc.views.panels;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;

/**
 * Controller for the properties panel - handles right side input forms
 * for beam properties, supports, and loads
 */
public class PropertiesPanelController {
    
    @FXML
    private VBox propertiesPanel;
    
    // Beam properties section
    @FXML
    private TextField beamLengthField;
    @FXML
    private TextField materialPropertiesField;
    @FXML
    private TextField crossSectionField;
    
    // Support configuration section
    @FXML
    private ComboBox<String> supportTypeComboBox;
    @FXML
    private TextField supportPositionField;
    @FXML
    private Button addSupportButton;
    @FXML
    private Button removeSupportButton;
    @FXML
    private ListView<Support> supportListView;
    
    // Load configuration section
    @FXML
    private ComboBox<String> loadTypeComboBox;
    @FXML
    private TextField loadMagnitudeField;
    @FXML
    private TextField loadPositionField;
    @FXML
    private TextField loadDistanceField; // For distributed loads
    @FXML
    private Button addLoadButton;
    @FXML
    private Button removeLoadButton;
    @FXML
    private ListView<Load> loadListView;
    
    // Analysis settings section
    @FXML
    private CheckBox includeDeflectionCheckBox;
    @FXML
    private CheckBox includeMomentDiagramCheckBox;
    @FXML
    private CheckBox includeShearDiagramCheckBox;
    @FXML
    private Button runAnalysisButton;
    
    private BeamModel beamModel;
    private PanelUpdateCallback updateCallback;
    
    /**
     * Interface for communicating updates back to main controller
     */
    public interface PanelUpdateCallback {
        void onBeamPropertiesChanged(BeamModel updatedModel);
        void onSupportAdded(Support support);
        void onSupportRemoved(Support support);
        void onLoadAdded(Load load);
        void onLoadRemoved(Load load);
        void onAnalysisRequested();
    }
    
    @FXML
    private void initialize() {
        setupSupportTypeComboBox();
        setupLoadTypeComboBox();
        setupEventHandlers();
        setupListViews();
    }
    
    private void setupSupportTypeComboBox() {
        supportTypeComboBox.getItems().addAll("Fixed", "Pinned", "Roller");
        supportTypeComboBox.setValue("Pinned"); // Default selection
    }
    
    private void setupLoadTypeComboBox() {
        loadTypeComboBox.getItems().addAll("Point Load", "Distributed Load", "Moment");
        loadTypeComboBox.setValue("Point Load"); // Default selection
    }
    
    private void setupEventHandlers() {
        // Beam property change handlers
        beamLengthField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateBeamLength(newVal);
        });
        
        // Support button handlers
        addSupportButton.setOnAction(e -> handleAddSupport());
        removeSupportButton.setOnAction(e -> handleRemoveSupport());
        
        // Load button handlers
        addLoadButton.setOnAction(e -> handleAddLoad());
        removeLoadButton.setOnAction(e -> handleRemoveLoad());
        
        // Analysis button handler
        runAnalysisButton.setOnAction(e -> handleRunAnalysis());
        
        // Selection handlers for list views
        supportListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                removeSupportButton.setDisable(newSelection == null);
                populateSupportFields(newSelection);
            }
        );
        
        loadListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                removeLoadButton.setDisable(newSelection == null);
                populateLoadFields(newSelection);
            }
        );
    }
    
    private void setupListViews() {
        // Configure support list view display
        supportListView.setCellFactory(listView -> new ListCell<Support>() {
            @Override
            protected void updateItem(Support support, boolean empty) {
                super.updateItem(support, empty);
                if (empty || support == null) {
                    setText(null);
                } else {
                    setText(String.format("%s at %.2f'", 
                        support.getType().toString(), support.getPosition()));
                }
            }
        });
        
        // Configure load list view display
        loadListView.setCellFactory(listView -> new ListCell<Load>() {
            @Override
            protected void updateItem(Load load, boolean empty) {
                super.updateItem(load, empty);
                if (empty || load == null) {
                    setText(null);
                } else {
                    setText(String.format("%s: %.2f @ %.2f'", 
                        load.getType().toString(), load.getMagnitude(), load.getPosition()));
                }
            }
        });
    }
    
    private void updateBeamLength(String newLength) {
        try {
            double length = Double.parseDouble(newLength);
            if (beamModel != null && updateCallback != null) {
                beamModel.setLength(length);
                updateCallback.onBeamPropertiesChanged(beamModel);
            }
        } catch (NumberFormatException e) {
            // Invalid input - could show error message
        }
    }
    
    @FXML
    private void handleAddSupport() {
        try {
            String type = supportTypeComboBox.getValue();
            double position = Double.parseDouble(supportPositionField.getText());
            
            Support.Type supportType = Support.Type.valueOf(type.toUpperCase());
            Support newSupport = new Support(position, supportType);
            
            if (updateCallback != null) {
                updateCallback.onSupportAdded(newSupport);
            }
            
            // Clear input fields
            supportPositionField.clear();
            
        } catch (NumberFormatException e) {
            showError("Please enter a valid position for the support.");
        } catch (IllegalArgumentException e) {
            showError("Invalid support type selected.");
        }
    }
    
    @FXML
    private void handleRemoveSupport() {
        Support selectedSupport = supportListView.getSelectionModel().getSelectedItem();
        if (selectedSupport != null && updateCallback != null) {
            updateCallback.onSupportRemoved(selectedSupport);
        }
    }
    
    @FXML
    private void handleAddLoad() {
        try {
            String type = loadTypeComboBox.getValue();
            double magnitude = Double.parseDouble(loadMagnitudeField.getText());
            double position = Double.parseDouble(loadPositionField.getText());
            
            Load.Type loadType = parseLoadType(type);
            Load newLoad = new Load(position, magnitude, loadType);
            
            // Handle distributed load distance if applicable
            if (loadType == Load.Type.DISTRIBUTED && !loadDistanceField.getText().isEmpty()) {
                double distance = Double.parseDouble(loadDistanceField.getText());
                newLoad.setEndPosition(position + distance);
            }
            
            if (updateCallback != null) {
                updateCallback.onLoadAdded(newLoad);
            }
            
            // Clear input fields
            loadMagnitudeField.clear();
            loadPositionField.clear();
            loadDistanceField.clear();
            
        } catch (NumberFormatException e) {
            showError("Please enter valid numeric values for load properties.");
        } catch (IllegalArgumentException e) {
            showError("Invalid load type selected.");
        }
    }
    
    @FXML
    private void handleRemoveLoad() {
        Load selectedLoad = loadListView.getSelectionModel().getSelectedItem();
        if (selectedLoad != null && updateCallback != null) {
            updateCallback.onLoadRemoved(selectedLoad);
        }
    }
    
    @FXML
    private void handleRunAnalysis() {
        if (updateCallback != null) {
            updateCallback.onAnalysisRequested();
        }
    }
    
    private Load.Type parseLoadType(String typeString) {
        switch (typeString) {
            case "Point Load":
                return Load.Type.POINT;
            case "Distributed Load":
                return Load.Type.DISTRIBUTED;
            case "Moment":
                return Load.Type.MOMENT;
            default:
                throw new IllegalArgumentException("Unknown load type: " + typeString);
        }
    }
    
    private void populateSupportFields(Support support) {
        if (support != null) {
            supportTypeComboBox.setValue(support.getType().toString());
            supportPositionField.setText(String.valueOf(support.getPosition()));
        }
    }
    
    private void populateLoadFields(Load load) {
        if (load != null) {
            loadTypeComboBox.setValue(getLoadTypeDisplayName(load.getType()));
            loadMagnitudeField.setText(String.valueOf(load.getMagnitude()));
            loadPositionField.setText(String.valueOf(load.getPosition()));
            
            if (load.getType() == Load.Type.DISTRIBUTED) {
                double distance = load.getEndPosition() - load.getPosition();
                loadDistanceField.setText(String.valueOf(distance));
            }
        }
    }
    
    private String getLoadTypeDisplayName(Load.Type loadType) {
        switch (loadType) {
            case POINT:
                return "Point Load";
            case DISTRIBUTED:
                return "Distributed Load";
            case MOMENT:
                return "Moment";
            default:
                return loadType.toString();
        }
    }
    
    /**
     * Update the panel with new beam model data
     */
    public void updateModel(BeamModel newModel) {
        this.beamModel = newModel;
        
        if (newModel != null) {
            // Update beam properties fields
            beamLengthField.setText(String.valueOf(newModel.getLength()));
            
            // Update support list
            supportListView.getItems().clear();
            supportListView.getItems().addAll(newModel.getSupports());
            
            // Update load list
            loadListView.getItems().clear();
            loadListView.getItems().addAll(newModel.getLoads());
        }
    }
    
    /**
     * Set the callback for panel updates
     */
    public void setUpdateCallback(PanelUpdateCallback callback) {
        this.updateCallback = callback;
    }
    
    /**
     * Clear all form fields
     */
    public void clearForm() {
        beamLengthField.clear();
        materialPropertiesField.clear();
        crossSectionField.clear();
        supportPositionField.clear();
        loadMagnitudeField.clear();
        loadPositionField.clear();
        loadDistanceField.clear();
    }
    
    /**
     * Enable/disable form controls
     */
    public void setFormEnabled(boolean enabled) {
        beamLengthField.setDisable(!enabled);
        materialPropertiesField.setDisable(!enabled);
        crossSectionField.setDisable(!enabled);
        supportTypeComboBox.setDisable(!enabled);
        supportPositionField.setDisable(!enabled);
        addSupportButton.setDisable(!enabled);
        loadTypeComboBox.setDisable(!enabled);
        loadMagnitudeField.setDisable(!enabled);
        loadPositionField.setDisable(!enabled);
        loadDistanceField.setDisable(!enabled);
        addLoadButton.setDisable(!enabled);
        runAnalysisButton.setDisable(!enabled);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}