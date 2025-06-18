package com.quickcalc.views.components.renderers;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.BeamModel;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;
import com.quickcalc.views.components.DimensionLineDrawer;
import com.quickcalc.views.components.ClickableDimensionText;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * Central drawing context that manages all rendering operations
 */
public class DrawingContext {
    
    private final SupportRenderer supportRenderer;
    private final LoadRenderer loadRenderer;
    private final GridRenderer gridRenderer;
    private final MeasurementRenderer measurementRenderer;
    private final DimensionLineDrawer dimensionLineDrawer;
    
    public DrawingContext(ViewTransform viewTransform, BeamModel beamModel, 
                         List<ClickableDimensionText> clickableDimensionTexts) {
        this.supportRenderer = new SupportRenderer();
        this.loadRenderer = new LoadRenderer();
        this.gridRenderer = new GridRenderer();
        this.measurementRenderer = new MeasurementRenderer();
        this.dimensionLineDrawer = new DimensionLineDrawer(viewTransform, beamModel, clickableDimensionTexts);
    }
    
    /**
     * Update the dimension line drawer with new beam model
     */
    public void updateDimensionLineDrawer(ViewTransform viewTransform, BeamModel beamModel, 
                                        List<ClickableDimensionText> clickableDimensionTexts) {
        // Since DimensionLineDrawer is recreated when beam model changes, 
        // we need to update our reference
    }
    
    /**
     * Draw the complete beam visualization
     */
    public void drawBeam(GraphicsContext gc, BeamModel beamModel, ViewTransform viewTransform,
                        int gridStyle, double canvasWidth, double canvasHeight,
                        com.quickcalc.views.components.InteractiveElement selectedElement,
                        com.quickcalc.views.components.InteractiveElement hoveredElement,
                        List<com.quickcalc.views.components.InteractiveElement> interactiveElements,
                        DimensionLineDrawer currentDimensionLineDrawer) {
        
        // Clear the canvas
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        
        // Draw grid first (background)
        gridRenderer.drawGrid(gc, gridStyle, viewTransform, canvasWidth, canvasHeight);
        
        // Draw temporary dimensions for selected or hovered element (before beam and other elements)
        com.quickcalc.views.components.InteractiveElement elementToDimension = null;
        if (selectedElement != null) {
            elementToDimension = selectedElement;
        } else if (hoveredElement != null) {
            elementToDimension = hoveredElement;
        }

        if (elementToDimension != null && currentDimensionLineDrawer != null) {
            currentDimensionLineDrawer.drawTemporaryElementDimensions(gc, elementToDimension);
        }
        
        // Draw the beam centerline
        Point2D start = viewTransform.engineeringToScreen(0, 0);
        Point2D end = viewTransform.engineeringToScreen(beamModel.getLength(), 0);
        
        gc.setStroke(UIConstants.BEAM_COLOR);
        gc.setLineWidth(UIConstants.BEAM_LINE_WIDTH);
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
        
        // Draw supports
        supportRenderer.drawSupports(gc, beamModel.getSupports(), viewTransform);
        
        // Draw loads
        loadRenderer.drawLoads(gc, beamModel.getLoads(), viewTransform);
        
        // Draw dimension lines below the beam
        if (currentDimensionLineDrawer != null) {
            currentDimensionLineDrawer.drawDetailedBottomDimensionLine(gc);
            currentDimensionLineDrawer.drawPermanentBottomDimensionLine(gc);
        }

        // Draw highlights for interactive elements (on top of everything except UI elements)
        for (com.quickcalc.views.components.InteractiveElement element : interactiveElements) {
            element.drawHighlight(gc, viewTransform);
        }
    }
    
    /**
     * Get the support renderer
     */
    public SupportRenderer getSupportRenderer() {
        return supportRenderer;
    }
    
    /**
     * Get the load renderer
     */
    public LoadRenderer getLoadRenderer() {
        return loadRenderer;
    }
    
    /**
     * Get the grid renderer
     */
    public GridRenderer getGridRenderer() {
        return gridRenderer;
    }
    
    /**
     * Get the measurement renderer
     */
    public MeasurementRenderer getMeasurementRenderer() {
        return measurementRenderer;
    }
}