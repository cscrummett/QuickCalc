package com.quickcalc.views.components.renderers;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.BeamModel;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;
import javafx.scene.canvas.GraphicsContext;

/**
 * Renderer for drawing measurement markers and dimension lines
 */
public class MeasurementRenderer {
    
    /**
     * Draw measurement markers along the beam
     * 
     * @param gc Graphics context
     * @param beamModel Beam model
     * @param viewTransform View transformation
     */
    public void drawMeasurementMarkers(GraphicsContext gc, BeamModel beamModel, ViewTransform viewTransform) {
        gc.setStroke(UIConstants.TEXT_COLOR);
        gc.setLineWidth(1.0);
        gc.setFill(UIConstants.TEXT_COLOR);
        
        // Determine appropriate interval based on zoom level
        double scale = viewTransform.getScaleX();
        double interval;
        
        if (scale < 10) interval = 10.0;
        else if (scale < 20) interval = 5.0;
        else if (scale < 50) interval = 2.0;
        else interval = 1.0;
        
        // Draw markers at calculated intervals
        double markerLength = 5.0;
        
        for (double pos = 0; pos <= beamModel.getLength(); pos += interval) {
            Point2D markerPos = viewTransform.engineeringToScreen(pos, 0);
            
            // Draw marker line
            gc.strokeLine(markerPos.getX(), markerPos.getY() + markerLength, 
                          markerPos.getX(), markerPos.getY() - markerLength);
            
            // Draw position text
            gc.fillText(String.format("%.0f'", pos), markerPos.getX() - 10, markerPos.getY() + markerLength + 15);
        }
    }
}