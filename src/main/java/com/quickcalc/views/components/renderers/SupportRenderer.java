package com.quickcalc.views.components.renderers;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.Support;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Renderer for drawing support symbols on the beam
 */
public class SupportRenderer {
    
    /**
     * Draw all supports for the given beam model
     * 
     * @param gc Graphics context
     * @param supports List of supports to draw
     * @param viewTransform View transformation
     */
    public void drawSupports(GraphicsContext gc, java.util.List<Support> supports, ViewTransform viewTransform) {
        for (Support support : supports) {
            Point2D position = viewTransform.engineeringToScreen(support.getPosition(), 0);
            
            switch (support.getType()) {
                case PINNED:
                    drawPinnedSupport(gc, position.getX(), position.getY());
                    break;
                case ROLLER:
                    drawRollerSupport(gc, position.getX(), position.getY());
                    break;
                case FIXED:
                    drawFixedSupport(gc, position.getX(), position.getY());
                    break;
            }
        }
    }
    
    /**
     * Draw a pinned support (triangle)
     * 
     * @param gc Graphics context
     * @param x X-coordinate
     * @param y Y-coordinate (beam centerline)
     */
    private void drawPinnedSupport(GraphicsContext gc, double x, double y) {
        double size = UIConstants.SUPPORT_SIZE;
        double halfWidth = size / 2;
        double height = size * 0.866; // Height of equilateral triangle (sqrt(3)/2 * size)
        
        gc.setStroke(UIConstants.SUPPORT_COLOR);
        gc.setLineWidth(UIConstants.SUPPORT_LINE_WIDTH);
        
        // Draw equilateral triangle pointing down from the beam
        double[] xPoints = {x - halfWidth, x, x + halfWidth};
        double[] yPoints = {y + height, y, y + height};
        
        // Draw filled triangle for better visibility
        gc.setFill(Color.WHITE);
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);
        
        // Draw ground line (wider than the triangle)
        double groundLineExtension = size * 0.5;
        gc.strokeLine(x - size - groundLineExtension, y + height, 
                     x + size + groundLineExtension, y + height);
        
        // Draw position indicator dot at the top center of the triangle (connection point)
        double dotRadius = 3.0;
        gc.setFill(UIConstants.SUPPORT_COLOR);
        gc.fillOval(x - dotRadius, y - dotRadius, dotRadius * 2, dotRadius * 2);
    }
    
    /**
     * Draw a roller support (circle)
     * 
     * @param gc Graphics context
     * @param x X-coordinate
     * @param y Y-coordinate (beam centerline)
     */
    private void drawRollerSupport(GraphicsContext gc, double x, double y) {
        double size = UIConstants.SUPPORT_SIZE;
        double height = size * 0.866; // Match the height of the pinned support (height of equilateral triangle)
        
        // Calculate radius to make a perfect circle with the desired height
        double radius = height / 2; // For a perfect circle, height = 2 * radius
        double circleSize = radius * 2; // Diameter of the circle
        
        gc.setStroke(UIConstants.SUPPORT_COLOR);
        gc.setLineWidth(UIConstants.SUPPORT_LINE_WIDTH);
        
        // Draw circle (roller) with top edge at the beam
        gc.setFill(Color.WHITE);
        gc.fillOval(x - radius, y, circleSize, circleSize);
        gc.strokeOval(x - radius, y, circleSize, circleSize);
        
        // Draw ground line (wider than the circle)
        double groundLineExtension = radius * 0.5;
        gc.strokeLine(x - radius - groundLineExtension, y + circleSize, 
                     x + radius + groundLineExtension, y + circleSize);
        
        // Draw position indicator dot at the top center of the roller (connection point)
        double dotRadius = 3.0;
        gc.setFill(UIConstants.SUPPORT_COLOR);
        gc.fillOval(x - dotRadius, y - dotRadius, dotRadius * 2, dotRadius * 2);
    }
    
    /**
     * Draw a fixed support (square with dot at connection point)
     * 
     * @param gc Graphics context
     * @param x X-coordinate
     * @param y Y-coordinate (beam centerline)
     */
    private void drawFixedSupport(GraphicsContext gc, double x, double y) {
        double size = UIConstants.SUPPORT_SIZE;
        double halfSize = size / 2;
        double height = size * 0.866; // Match height of pinned support (height of equilateral triangle)
        
        // Draw filled square below the beam
        gc.setFill(Color.WHITE);
        gc.setStroke(UIConstants.SUPPORT_COLOR);
        gc.setLineWidth(UIConstants.SUPPORT_LINE_WIDTH);
        
        // Draw square centered below the beam with matching height
        gc.fillRect(x - halfSize, y, size, height);
        gc.strokeRect(x - halfSize, y, size, height);
        
        // Draw ground line (wider than the square)
        double groundLineExtension = size * 0.5;
        gc.strokeLine(x - size - groundLineExtension, y + height, 
                     x + size + groundLineExtension, y + height);
        
        // Draw position indicator dot at the top center of the square (connection point)
        double dotRadius = 3.0;
        gc.setFill(UIConstants.SUPPORT_COLOR);
        gc.fillOval(x - dotRadius, y - dotRadius, dotRadius * 2, dotRadius * 2);
    }
}