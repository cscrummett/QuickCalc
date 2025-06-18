package com.quickcalc.services;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.views.components.BeamCanvas;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;

public class MenuActionHandler {
    
    public interface ModelUpdateCallback {
        void onModelUpdated(BeamModel newModel);
    }
    
    private final BeamDataService beamDataService;
    private final FileService fileService;
    private BeamModel beamModel;
    private BeamCanvas beamCanvas;
    private ModelUpdateCallback modelUpdateCallback;
    
    public MenuActionHandler(BeamDataService beamDataService, FileService fileService) {
        this.beamDataService = beamDataService;
        this.fileService = fileService;
    }
    
    public void setBeamModel(BeamModel beamModel) {
        this.beamModel = beamModel;
    }
    
    public void setBeamCanvas(BeamCanvas beamCanvas) {
        this.beamCanvas = beamCanvas;
    }
    
    public void setModelUpdateCallback(ModelUpdateCallback callback) {
        this.modelUpdateCallback = callback;
    }
    
    public void handleNew() {
        System.out.println("New project requested");
        
        beamModel.reset(20.0);
        beamModel.addLoad(new Load(10.0, 5.0, Load.Type.POINT));
        
        if (beamCanvas != null) {
            beamCanvas.draw();
        }
        
        fileService.clearCurrentFile();
        
        if (modelUpdateCallback != null) {
            modelUpdateCallback.onModelUpdated(beamModel);
        }
    }
    
    public void handleOpen() {
        System.out.println("Open project requested");
        
        Optional<BeamModel> loadedModel = fileService.open();
        if (loadedModel.isPresent()) {
            this.beamModel = loadedModel.get();
            if (beamCanvas != null) {
                beamCanvas.setBeamModel(beamModel);
                beamCanvas.draw();
            }
            if (modelUpdateCallback != null) {
                modelUpdateCallback.onModelUpdated(beamModel);
            }
        }
    }
    
    public void handleSave() {
        System.out.println("Save project requested");
        fileService.save(beamModel);
    }
    
    public void handleSaveAs() {
        System.out.println("Save As project requested");
        fileService.saveAs(beamModel);
    }
    
    public void handleExit() {
        System.out.println("Exit requested");
        Platform.exit();
    }
    
    public void handleUndo() {
        System.out.println("Undo requested");
    }
    
    public void handleRedo() {
        System.out.println("Redo requested");
    }
    
    public void handlePreferences() {
        System.out.println("Preferences requested");
    }
    
    public void handleRunAnalysis() {
        System.out.println("Run Analysis requested");
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Analysis");
        alert.setHeaderText("Analysis Results");
        alert.setContentText("Analysis will be implemented in a future stage.");
        alert.showAndWait();
    }
    
    public void handleClearResults() {
        System.out.println("Clear Results requested");
    }
    
    public void handleZoomIn() {
        System.out.println("Zoom In requested");
        if (beamCanvas != null) {
            beamCanvas.zoomIn();
        }
    }
    
    public void handleZoomOut() {
        System.out.println("Zoom Out requested");
        if (beamCanvas != null) {
            beamCanvas.zoomOut();
        }
    }
    
    public void handleFitToWindow() {
        System.out.println("Fit to Window requested");
        if (beamCanvas != null) {
            beamCanvas.fitViewToBeam();
            beamCanvas.draw();
        }
    }
    
    public void handleAbout() {
        System.out.println("About requested");
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About QuickCalc");
        alert.setHeaderText("QuickCalc Structural Analysis");
        alert.setContentText("A JavaFX-based structural analysis application for beam calculations.");
        alert.showAndWait();
    }
}