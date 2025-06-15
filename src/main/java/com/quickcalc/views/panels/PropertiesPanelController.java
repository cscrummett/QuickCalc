package com.quickcalc.views.panels;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;

/**
 * Controller for the properties panel - simplified version with no input forms yet
 */
public class PropertiesPanelController {
    
    // No @FXML references needed since we removed all input controls
    
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
        // Nothing to initialize yet - just placeholder panel
    }
    
    /**
     * Update the panel with new beam model data
     */
    public void updateModel(BeamModel newModel) {
        this.beamModel = newModel;
        // No UI to update yet - just store the model
    }
    
    /**
     * Set the callback for panel updates
     */
    public void setUpdateCallback(PanelUpdateCallback callback) {
        this.updateCallback = callback;
    }
}