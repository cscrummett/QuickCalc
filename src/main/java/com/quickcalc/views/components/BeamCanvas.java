package com.quickcalc.views.components;

import com.quickcalc.constants.BeamConstants;
import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;
import com.quickcalc.utils.CoordinateConverter;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.Cursor;
import javafx.scene.text.TextAlignment;

/**
 * Canvas component for drawing the beam with its supports and loads
 */
public class BeamCanvas extends Canvas {
    
    private BeamModel beamModel;
    private ViewTransform viewTransform;
    private double paddingX = UIConstants.CANVAS_PADDING_X; // Horizontal padding in pixels
    private double paddingY = UIConstants.CANVAS_PADDING_Y; // Vertical padding in pixels
    
    // Grid style (0 = none, 1 = lines)
    private int gridStyle = UIConstants.GRID_STYLE_NONE;
    
    // Mouse interaction variables
    private double lastMouseX;
    private double lastMouseY;
    private boolean isPanning = false;
    
    /**
     * Constructor
     * 
     * @param width Initial width of the canvas
     * @param height Initial height of the canvas
     */
    public BeamCanvas(double width, double height) {
        super(width, height);
        
        // Create a default beam model
        this.beamModel = new BeamModel();
        
        // Create the view transform
        this.viewTransform = new ViewTransform();
        
        // Make the canvas resizable
        widthProperty().addListener((obs, oldVal, newVal) -> {
            fitViewToBeam();
            draw();
        });
        heightProperty().addListener((obs, oldVal, newVal) -> {
            fitViewToBeam();
            draw();
        });
        
        // Set up mouse event handlers
        setupMouseHandlers();
        
        // Initial fit and draw
        fitViewToBeam();
        draw();
    }
    
    /**
     * Set the beam model and redraw
     * 
     * @param beamModel Beam model to use
     */
    public void setBeamModel(BeamModel beamModel) {
        this.beamModel = beamModel;
        draw();
    }
    
    /**
     * Get the current beam model
     * 
     * @return Current beam model
     */
    public BeamModel getBeamModel() {
        return beamModel;
    }
    
    /**
     * Get the current grid style
     * 
     * @return Current grid style (GRID_STYLE_NONE or GRID_STYLE_LINES)
     */
    public int getGridStyle() {
        return gridStyle;
    }
    
    /**
     * Set the grid style and redraw
     * 
     * @param gridStyle New grid style (GRID_STYLE_NONE or GRID_STYLE_LINES)
     */
    public void setGridStyle(int gridStyle) {
        this.gridStyle = gridStyle;
        draw();
    }
    
    /**
     * Draw the beam with its supports and loads
     */
    /**
     * Fit the view to show the entire beam
     */
    public void fitViewToBeam() {
        viewTransform.fitBeam(beamModel.getLength(), getWidth(), getHeight(), paddingX, paddingY);
    }
    
    /**
     * Convert screen coordinates to engineering coordinates
     * 
     * @param screenX X-coordinate in screen units (pixels)
     * @param screenY Y-coordinate in screen units (pixels)
     * @return Point in engineering coordinates (feet)
     */
    public Point2D screenToEngineering(double screenX, double screenY) {
        return viewTransform.screenToEngineering(screenX, screenY);
    }
    
    // Track time of last middle click for double-click detection
    private long lastMiddleClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_MS = 300; // Double-click threshold in milliseconds
    
    /**
     * Set up mouse event handlers for zoom and pan
     */
    private void setupMouseHandlers() {
        // Scroll to zoom
        setOnScroll(this::handleScroll);
        
        // Middle-click and drag to pan
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseReleased(this::handleMouseReleased);
        
        // Middle-click double-click to zoom-to-fit
        setOnMouseClicked(this::handleMouseClicked);
    }
    
    /**
     * Handle mouse click events for double-click zoom-to-fit
     */
    private void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMiddleClickTime < DOUBLE_CLICK_TIME_MS) {
                // Double middle-click detected, zoom to fit
                fitViewToBeam();
                draw();
                event.consume();
            }
            lastMiddleClickTime = currentTime;
        }
    }
    
    /**
     * Handle mouse scroll events for zooming
     */
    private void handleScroll(ScrollEvent event) {
        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
        viewTransform.zoom(event.getX(), event.getY(), zoomFactor);
        draw();
        event.consume();
    }
    
    /**
     * Handle mouse press events for panning
     */
    private void handleMousePressed(MouseEvent event) {
        if (event.isMiddleButtonDown()) {
            isPanning = true;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            setCursor(Cursor.MOVE);
            event.consume();
        }
    }
    
    /**
     * Handle mouse drag events for panning
     */
    private void handleMouseDragged(MouseEvent event) {
        if (isPanning) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;
            
            viewTransform.pan(deltaX, deltaY);
            
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            
            draw();
            event.consume();
        }
    }
    
    /**
     * Handle mouse release events for panning
     */
    private void handleMouseReleased(MouseEvent event) {
        if (isPanning) {
            isPanning = false;
            setCursor(Cursor.DEFAULT);
            event.consume();
        }
    }
    
    /**
     * Draw the beam with its supports and loads
     */
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        
        // Clear the canvas
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        // Draw grid
        drawGrid(gc);
        
        // Draw the beam centerline
        Point2D start = viewTransform.engineeringToScreen(0, 0);
        Point2D end = viewTransform.engineeringToScreen(beamModel.getLength(), 0);
        
        gc.setStroke(UIConstants.BEAM_COLOR);
        gc.setLineWidth(UIConstants.BEAM_LINE_WIDTH);
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
        
        // Draw supports
        drawSupports(gc);
        
        // Draw loads
        drawLoads(gc);
        
        // Draw measurement markers
        drawMeasurementMarkers(gc);
    }
    
    /**
     * Draw the background grid
     * 
     * @param gc Graphics context
     */
    private void drawGrid(GraphicsContext gc) {
        // If grid style is set to none, don't draw anything
        if (gridStyle == UIConstants.GRID_STYLE_NONE) {
            return;
        }
        
        // Set grid properties
        gc.setStroke(UIConstants.GRID_COLOR);
        gc.setLineWidth(UIConstants.GRID_LINE_WIDTH);
        
        // Get the visible area in engineering coordinates
        Point2D topLeft = viewTransform.screenToEngineering(0, 0);
        Point2D bottomRight = viewTransform.screenToEngineering(getWidth(), getHeight());
        
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
    
    /**
     * Draw the supports on the beam
     * 
     * @param gc Graphics context
     */
    private void drawSupports(GraphicsContext gc) {
        for (Support support : beamModel.getSupports()) {
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
        double radius = size / 2;
        
        gc.setStroke(UIConstants.SUPPORT_COLOR);
        gc.setLineWidth(UIConstants.SUPPORT_LINE_WIDTH);
        
        // Draw circle (roller) with top edge at the beam
        gc.setFill(Color.WHITE);
        gc.fillOval(x - radius, y, size, size);
        gc.strokeOval(x - radius, y, size, size);
        
        // Draw ground line (wider than the circle)
        double groundLineExtension = size * 0.5;
        gc.strokeLine(x - size - groundLineExtension, y + size, 
                     x + size + groundLineExtension, y + size);
    }
    
    /**
     * Draw a fixed support (rectangle with hatching)
     * 
     * @param gc Graphics context
     * @param x X-coordinate
     * @param y Y-coordinate (beam centerline)
     */
    private void drawFixedSupport(GraphicsContext gc, double x, double y) {
        double width = UIConstants.SUPPORT_SIZE / 2;
        double height = UIConstants.SUPPORT_SIZE * 2;
        
        gc.setStroke(UIConstants.SUPPORT_COLOR);
        gc.setLineWidth(UIConstants.SUPPORT_LINE_WIDTH);
        
        // Draw rectangle
        gc.strokeRect(x - width/2, y - height/2, width, height);
        
        // Draw hatching lines
        double spacing = UIConstants.SUPPORT_SIZE / 4;
        for (double i = -height/2 + spacing; i < height/2; i += spacing) {
            gc.strokeLine(x - width/2, y + i, x + width/2, y + i);
        }
    }
    
    /**
     * Draw the loads on the beam
     * 
     * @param gc Graphics context
     */
    private void drawLoads(GraphicsContext gc) {
        // First draw distributed loads and moments
        for (Load load : beamModel.getLoads()) {
            if (load.getType() != Load.Type.POINT) {
                Point2D position = viewTransform.engineeringToScreen(load.getPosition(), 0);
                
                switch (load.getType()) {
                    case DISTRIBUTED:
                        Point2D endPosition = viewTransform.engineeringToScreen(load.getEndPosition(), 0);
                        drawDistributedLoad(gc, position.getX(), endPosition.getX(), position.getY(), load.getMagnitude());
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
        for (Load load : beamModel.getLoads()) {
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
        
        // Draw magnitude text with white background
        String text = String.format("%.1f kip", magnitude);
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Calculate text bounds
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        
        // Draw white background rectangle
        double padding = 2;
        gc.setFill(Color.WHITE);
        gc.fillRect(x - textWidth/2 - padding, y - arrowLength / 2 - textHeight - padding, 
                   textWidth + 2*padding, textHeight + 2*padding);
        
        // Draw text
        gc.setFill(UIConstants.POINT_LOAD_COLOR);
        gc.fillText(text, x, y - arrowLength / 2);
    }
    
    /**
     * Draw a distributed load (multiple arrows)
     * 
     * @param gc Graphics context
     * @param x1 Start X-coordinate
     * @param x2 End X-coordinate
     * @param y Y-coordinate (beam centerline)
     * @param magnitude Load magnitude
     */
    private void drawDistributedLoad(GraphicsContext gc, double x1, double x2, double y, double magnitude) {
        double arrowLength = UIConstants.DISTRIBUTED_LOAD_ARROW_LENGTH;
        double arrowHeadSize = UIConstants.DISTRIBUTED_LOAD_ARROW_HEAD_SIZE;
        double spacing = UIConstants.DISTRIBUTED_LOAD_ARROW_SPACING;
        
        gc.setStroke(UIConstants.DISTRIBUTED_LOAD_COLOR);
        gc.setLineWidth(UIConstants.DISTRIBUTED_LOAD_LINE_WIDTH);
        
        // Draw load line
        gc.strokeLine(x1, y - arrowLength, x2, y - arrowLength);
        
        // Draw arrows
        int numArrows = (int) Math.max(2, Math.floor((x2 - x1) / spacing));
        double step = (x2 - x1) / (numArrows - 1);
        
        for (int i = 0; i < numArrows; i++) {
            double x = x1 + i * step;
            
            // Draw arrow shaft
            gc.strokeLine(x, y - arrowLength, x, y);
            
            // Draw arrow head
            gc.strokeLine(x, y, x - arrowHeadSize, y - arrowHeadSize);
            gc.strokeLine(x, y, x + arrowHeadSize, y - arrowHeadSize);
        }
        
        // Draw magnitude text with white background
        String text = String.format("%.1f kip/ft", magnitude);
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
        double radius = UIConstants.MOMENT_RADIUS;
        double arrowHeadSize = UIConstants.MOMENT_ARROW_HEAD_SIZE;
        
        gc.setStroke(UIConstants.MOMENT_COLOR);
        gc.setLineWidth(UIConstants.MOMENT_LINE_WIDTH);
        
        // Determine direction based on sign of magnitude
        boolean clockwise = magnitude < 0;
        double startAngle = clockwise ? 0 : 180;
        double endAngle = clockwise ? 270 : 270;
        
        // Draw arc
        gc.strokeArc(x - radius, y - radius, radius * 2, radius * 2, startAngle, endAngle - startAngle, javafx.scene.shape.ArcType.OPEN);
        
        // Draw arrow head
        double arrowAngle = Math.toRadians(endAngle);
        double arrowX = x + radius * Math.cos(arrowAngle);
        double arrowY = y - radius * Math.sin(arrowAngle);
        
        double angle1 = arrowAngle + (clockwise ? -Math.PI/4 : Math.PI/4);
        double angle2 = arrowAngle + (clockwise ? Math.PI/4 : -Math.PI/4);
        
        gc.strokeLine(arrowX, arrowY, 
                      arrowX + arrowHeadSize * Math.cos(angle1), 
                      arrowY - arrowHeadSize * Math.sin(angle1));
        gc.strokeLine(arrowX, arrowY, 
                      arrowX + arrowHeadSize * Math.cos(angle2), 
                      arrowY - arrowHeadSize * Math.sin(angle2));
        
        // Draw magnitude text with white background
        String text = String.format("%.1f kip-ft", Math.abs(magnitude));
        gc.setTextAlign(TextAlignment.LEFT);
        
        // Calculate text bounds
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        double textX = x + radius + 5; // Add some padding from the moment symbol
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
    
    /**
     * Draw measurement markers along the beam
     * 
     * @param gc Graphics context
     * @param scaleX Horizontal scale factor
     * @param beamY Y-coordinate of the beam centerline
     */
    private void drawMeasurementMarkers(GraphicsContext gc) {
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
        
        // Round to nearest interval for start position
        double startPos = Math.floor(beamModel.getLength() / interval) * interval;
        
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
