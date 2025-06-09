package com.quickcalc.views.components.renderers;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.Load;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Renderer for drawing load symbols on the beam
 */
public class LoadRenderer {
    
    /**
     * Draw all loads for the given beam model
     * 
     * @param gc Graphics context
     * @param loads List of loads to draw
     * @param viewTransform View transformation
     */
    public void drawLoads(GraphicsContext gc, java.util.List<Load> loads, ViewTransform viewTransform) {
        // First draw distributed loads and moments
        for (Load load : loads) {
            if (load.getType() != Load.Type.POINT) {
                Point2D position = viewTransform.engineeringToScreen(load.getPosition(), 0);
                
                switch (load.getType()) {
                    case DISTRIBUTED:
                        Point2D endPosition = viewTransform.engineeringToScreen(load.getEndPosition(), 0);
                        drawVaryingLoad(gc, position.getX(), endPosition.getX(), position.getY(), 
                                      load.getMagnitude(), load.getMagnitudeEnd(), load.getType());
                        break;
                    case MOMENT:
                        drawMoment(gc, position.getX(), position.getY(), load.getMagnitude());
                        break;
                    default:
                        break;
                }
            }
        }
        
        // Then draw point loads on top
        for (Load load : loads) {
            if (load.getType() == Load.Type.POINT) {
                Point2D position = viewTransform.engineeringToScreen(load.getPosition(), 0);
                drawPointLoad(gc, position.getX(), position.getY(), load.getMagnitude());
            }
        }
    }
    
    /**
     * Draw a point load (downward arrow)
     * 
     * @param gc Graphics context
     * @param x X-coordinate
     * @param y Y-coordinate (beam centerline)
     * @param magnitude Load magnitude
     */
    private void drawPointLoad(GraphicsContext gc, double x, double y, double magnitude) {
        double arrowLength = UIConstants.POINT_LOAD_ARROW_LENGTH;
        double arrowHeadSize = UIConstants.POINT_LOAD_ARROW_HEAD_SIZE;
        
        gc.setStroke(UIConstants.POINT_LOAD_COLOR);
        gc.setLineWidth(UIConstants.POINT_LOAD_LINE_WIDTH);
        
        // Draw arrow shaft
        gc.strokeLine(x, y - arrowLength, x, y);
        
        // Draw arrow head
        gc.strokeLine(x, y, x - arrowHeadSize, y - arrowHeadSize);
        gc.strokeLine(x, y, x + arrowHeadSize, y - arrowHeadSize);
        
        // Draw magnitude text with white background above the arrow
        String text = String.format("%.1f kip", magnitude);
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Calculate text bounds
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        
        // Position text above the arrow with some padding
        double textY = y - arrowLength - 5; // Position above the arrow with 5px padding
        
        // Draw white background rectangle
        double padding = 2;
        gc.setFill(Color.WHITE);
        gc.fillRect(x - textWidth/2 - padding, textY - textHeight - padding, 
                   textWidth + 2*padding, textHeight + 2*padding);
        
        // Draw text
        gc.setFill(UIConstants.POINT_LOAD_COLOR);
        gc.fillText(text, x, textY);
    }
    
    /**
     * Draw a distributed load (multiple arrows)
     * 
     * @param gc Graphics context
     * @param x1 Start X-coordinate
     * @param x2 End X-coordinate
     * @param y Y-coordinate (beam centerline)
     * @param startMagnitude Start magnitude
     * @param endMagnitude End magnitude
     * @param loadType Load type
     */
    private void drawVaryingLoad(GraphicsContext gc, double x1, double x2, double y, 
                               double startMagnitude, double endMagnitude, Load.Type loadType) {
        double arrowLength = UIConstants.DISTRIBUTED_LOAD_ARROW_LENGTH;
        double arrowHeadSize = UIConstants.DISTRIBUTED_LOAD_ARROW_HEAD_SIZE;
        double spacing = UIConstants.DISTRIBUTED_LOAD_ARROW_SPACING;
        
        gc.setStroke(UIConstants.DISTRIBUTED_LOAD_COLOR);
        gc.setLineWidth(UIConstants.DISTRIBUTED_LOAD_LINE_WIDTH);

        // Calculate maximum magnitude for scaling
        double maxAbsMagnitude = Math.max(Math.abs(startMagnitude), Math.abs(endMagnitude));
        if (maxAbsMagnitude == 0) maxAbsMagnitude = 1; // Avoid division by zero

        double y1_top = y + (startMagnitude / maxAbsMagnitude) * arrowLength;
        double y2_top = y + (endMagnitude / maxAbsMagnitude) * arrowLength;
        
        // Draw top line of the load shape
        gc.strokeLine(x1, y1_top, x2, y2_top);
        
        // Draw arrows
        int numArrows = (int) Math.max(2, Math.floor((x2 - x1) / spacing));
        double step = (x2 - x1) / (numArrows - 1);
        
        for (int i = 0; i < numArrows; i++) {
            double x = x1 + i * step;
            
            // Calculate current interpolated top Y for the arrow
            double currentFraction = (i == numArrows - 1 && numArrows > 1) ? 1.0 : (double)i / (numArrows - 1);
            if (numArrows == 1) currentFraction = 0.5; // Center if only one arrow
            double current_y_top = y1_top + (y2_top - y1_top) * currentFraction;

            // Draw arrow shaft
            gc.strokeLine(x, current_y_top, x, y);
            
            // Draw arrow head
            gc.strokeLine(x, y, x - arrowHeadSize, y - arrowHeadSize);
            gc.strokeLine(x, y, x + arrowHeadSize, y - arrowHeadSize);
        }
        
        // Draw magnitude text with white background
        String text;
        if (startMagnitude == endMagnitude) {
            text = String.format("%.1f kip/ft", startMagnitude);
        } else {
            text = String.format("%.1f to %.1f kip/ft", startMagnitude, endMagnitude);
        }
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Calculate text bounds
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        double textX = (x1 + x2) / 2;
        double textY = y - arrowLength - 5;
        
        // Draw white background rectangle
        double padding = 2;
        gc.setFill(Color.WHITE);
        gc.fillRect(textX - textWidth/2 - padding, textY - textHeight - padding, 
                   textWidth + 2*padding, textHeight + 2*padding);
        
        // Draw text
        gc.setFill(UIConstants.DISTRIBUTED_LOAD_COLOR);
        gc.fillText(text, textX, textY);
    }
    
    /**
     * Draw a moment (curved arrow)
     * 
     * @param gc Graphics context
     * @param x X-coordinate
     * @param y Y-coordinate (beam centerline)
     * @param magnitude Moment magnitude
     */
    private void drawMoment(GraphicsContext gc, double x, double y, double magnitude) {
        double baseRadius = UIConstants.MOMENT_RADIUS * 0.7; // 70% of the original radius
        double arrowHeadSize = UIConstants.MOMENT_ARROW_HEAD_SIZE * 0.8; // Slightly smaller arrow head
        
        gc.setStroke(UIConstants.MOMENT_COLOR);
        gc.setLineWidth(UIConstants.MOMENT_LINE_WIDTH);
        
        // Draw position indicator dot at the center of the moment symbol
        double dotRadius = 3.0;
        gc.setFill(UIConstants.MOMENT_COLOR);
        gc.fillOval(x - dotRadius, y - dotRadius, dotRadius * 2, dotRadius * 2);
        
        // Determine direction based on sign of magnitude
        boolean clockwise = magnitude < 0;
        double arcStartAngle = clockwise ? 30 : 210; // Start at 30° or 210°
        double arcExtent = 240; // 2/3 of a circle (240°)
        
        // Draw arc
        gc.strokeArc(x - baseRadius, y - baseRadius, 
                    baseRadius * 2, baseRadius * 2, 
                    arcStartAngle, arcExtent, 
                    javafx.scene.shape.ArcType.OPEN);

        // Calculate arrow head position at the end of the arc
        double endAngle = arcStartAngle + (clockwise ? arcExtent : 0);
        double arrowAngle = Math.toRadians(endAngle);
        double arrowX = x + baseRadius * Math.cos(arrowAngle);
        double arrowY = y - baseRadius * Math.sin(arrowAngle);

        // Calculate arrow head points (tangent to the arc)
        double tangentAngle = arrowAngle + (clockwise ? -Math.PI/2 : Math.PI/2);
        double arrowLength = arrowHeadSize * 1.5;

        // First arrow line (longer, at 30° from tangent)
        double angle1 = tangentAngle + Math.toRadians(30) * (clockwise ? -1 : 1);
        gc.strokeLine(arrowX, arrowY, 
                     arrowX + arrowLength * Math.cos(angle1), 
                     arrowY - arrowLength * Math.sin(angle1));

        // Second arrow line (shorter, at 10° from tangent)
        double angle2 = tangentAngle + Math.toRadians(10) * (clockwise ? -1 : 1);
        gc.strokeLine(arrowX, arrowY, 
                     arrowX + arrowLength * 0.7 * Math.cos(angle2), 
                     arrowY - arrowLength * 0.7 * Math.sin(angle2));
        
        // Draw magnitude text with white background
        String text = String.format("%.1f kip-ft", Math.abs(magnitude));
        gc.setTextAlign(TextAlignment.LEFT);
        
        // Calculate text bounds
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        double textX = x + baseRadius + 5; // Add some padding from the moment symbol
        double textY = y + textHeight/2; // Vertically center with the moment symbol
        
        // Draw white background rectangle
        double padding = 2;
        gc.setFill(Color.WHITE);
        gc.fillRect(textX - padding, textY - textHeight - padding, 
                   textWidth + 2*padding, textHeight + 2*padding);
        
        // Draw text
        gc.setFill(UIConstants.MOMENT_COLOR);
        gc.fillText(text, textX, textY);
    }
}