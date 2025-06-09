package com.quickcalc.services;

import com.quickcalc.models.BeamModel;
import com.quickcalc.views.components.BeamCanvas;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class CanvasManager {
    
    private BeamCanvas beamCanvas;
    private Pane canvasContainer;
    private BeamModel beamModel;
    
    public void initializeCanvas(Pane canvasContainer, BeamModel beamModel) {
        this.canvasContainer = canvasContainer;
        this.beamModel = beamModel;
        
        beamCanvas = new BeamCanvas(canvasContainer.getWidth(), canvasContainer.getHeight());
        beamCanvas.setBeamModel(beamModel);
        canvasContainer.getChildren().add(beamCanvas);
        
        setupCanvasBindings();
        setupKeyEventHandling();
        requestFocusForCanvas();
    }
    
    private void setupCanvasBindings() {
        beamCanvas.widthProperty().bind(canvasContainer.widthProperty());
        beamCanvas.heightProperty().bind(canvasContainer.heightProperty());
    }
    
    private void setupKeyEventHandling() {
        canvasContainer.setOnKeyPressed(this::handleKeyPressed);
        canvasContainer.setFocusTraversable(true);
    }
    
    private void requestFocusForCanvas() {
        Platform.runLater(() -> canvasContainer.requestFocus());
    }
    
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F) {
            beamCanvas.fitViewToBeam();
            beamCanvas.draw();
            event.consume();
        }
    }
    
    public BeamCanvas getBeamCanvas() {
        return beamCanvas;
    }
    
    public void setBeamModel(BeamModel beamModel) {
        this.beamModel = beamModel;
        if (beamCanvas != null) {
            beamCanvas.setBeamModel(beamModel);
        }
    }
    
    public void refreshCanvas() {
        if (beamCanvas != null) {
            beamCanvas.draw();
        }
    }
    
    public void fitViewToBeam() {
        if (beamCanvas != null) {
            beamCanvas.fitViewToBeam();
            beamCanvas.draw();
        }
    }
}