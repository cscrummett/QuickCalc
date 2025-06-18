package com.quickcalc.views.components.renderers;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;
import javafx.scene.canvas.GraphicsContext;

/**
 * Renderer for drawing grid on the canvas
 */
public class GridRenderer {
    
    /**
     * Draw the background grid
     * 
     * @param gc Graphics context
     * @param gridStyle Grid style (GRID_STYLE_NONE or GRID_STYLE_LINES)
     * @param viewTransform View transformation
     * @param canvasWidth Canvas width
     * @param canvasHeight Canvas height
     */
    public void drawGrid(GraphicsContext gc, int gridStyle, ViewTransform viewTransform, 
                        double canvasWidth, double canvasHeight) {
        // If grid style is set to none, don't draw anything
        if (gridStyle == UIConstants.GRID_STYLE_NONE) {
            return;
        }
        
        // Set grid properties
        gc.setStroke(UIConstants.GRID_COLOR);
        gc.setLineWidth(UIConstants.GRID_LINE_WIDTH);
        
        // Get the visible area in engineering coordinates
        Point2D topLeft = viewTransform.screenToEngineering(0, 0);
        Point2D bottomRight = viewTransform.screenToEngineering(canvasWidth, canvasHeight);
        
        // Round to nearest grid lines
        double startX = Math.floor(topLeft.getX());
        double endX = Math.ceil(bottomRight.getX());
        double startY = Math.floor(bottomRight.getY()); // Y is flipped
        double endY = Math.ceil(topLeft.getY());
        
        if (gridStyle == UIConstants.GRID_STYLE_LINES) {
            // Draw vertical grid lines (every 1 foot)
            for (double x = startX; x <= endX; x += UIConstants.GRID_SPACING) {
                Point2D top = viewTransform.engineeringToScreen(x, endY);
                Point2D bottom = viewTransform.engineeringToScreen(x, startY);
                gc.strokeLine(top.getX(), top.getY(), bottom.getX(), bottom.getY());
            }
            
            // Draw horizontal grid lines (every 1 foot)
            for (double y = startY; y <= endY; y += UIConstants.GRID_SPACING) {
                Point2D left = viewTransform.engineeringToScreen(startX, y);
                Point2D right = viewTransform.engineeringToScreen(endX, y);
                gc.strokeLine(left.getX(), left.getY(), right.getX(), right.getY());
            }
        }
    }
}