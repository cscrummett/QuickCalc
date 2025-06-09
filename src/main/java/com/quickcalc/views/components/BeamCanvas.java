package com.quickcalc.views.components;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;
import com.quickcalc.views.components.ClickableDimensionText;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.geometry.BoundingBox;
import com.quickcalc.utils.Point2D;
import com.quickcalc.utils.ViewTransform;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;
import java.util.TreeSet;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

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

    // Interactive elements
    private List<InteractiveElement> interactiveElements = new ArrayList<>();
    private InteractiveElement hoveredElement = null;
    private InteractiveElement selectedElement = null;
    private List<ClickableDimensionText> clickableDimensionTexts = new ArrayList<>();
    private DimensionLineDrawer dimensionLineDrawer;
    
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

        // Initialize the dimension line drawer
        this.dimensionLineDrawer = new DimensionLineDrawer(this.viewTransform, this.beamModel, this.clickableDimensionTexts);
        
        // Make the canvas resizable
        widthProperty().addListener((obs, oldVal, newVal) -> {
            fitViewToBeam();
            draw();
        });
        heightProperty().addListener((obs, oldVal, newVal) -> {
            fitViewToBeam();
            draw();
        });
        
        // Make canvas focusable and request focus
        setFocusTraversable(true);
        
        // Set up mouse and key event handlers
        setupMouseHandlers();
        
        // Initial fit and draw
        populateInteractiveElements(); // Populate before first draw
        fitViewToBeam();
        
        // Request focus after the scene is shown
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Add global key event filter for Escape key
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);
                
                // Request focus after the window is shown
                Platform.runLater(() -> {
                    requestFocus();
                    System.out.println("BeamCanvas: Initial focus requested");
                });
            }
        });
        
        draw();
    }
    
    /**
     * Set the beam model and redraw
     * 
     * @param beamModel Beam model to use
     */
    public void setBeamModel(BeamModel beamModel) {
        this.beamModel = beamModel;
        // Update the beam model in the dimension line drawer as well
        this.dimensionLineDrawer = new DimensionLineDrawer(this.viewTransform, this.beamModel, this.clickableDimensionTexts);
        populateInteractiveElements(); // Repopulate for new model
        fitViewToBeam(); // Ensure view is appropriate for the new model
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
     * Populate the list of interactive elements from the beam model.
     */
    public void populateInteractiveElements() {
        System.out.println("[BeamCanvas] populateInteractiveElements: Starting");
        this.clickableDimensionTexts.clear(); // Clear dimension texts for the new draw cycle
    interactiveElements.clear();
        System.out.println("[BeamCanvas] populateInteractiveElements: Cleared existing elements. Size: " + interactiveElements.size());
        if (beamModel == null) {
            return;
        }

        for (Support support : beamModel.getSupports()) {
            interactiveElements.add(new SupportMarker(support, beamModel));
        }
        System.out.println("[BeamCanvas] populateInteractiveElements: Added supports. Size: " + interactiveElements.size());
        for (Load load : beamModel.getLoads()) {
            interactiveElements.add(new LoadMarker(load, beamModel));
        }

        // Sort elements by priority for selection (higher number = higher priority = checked first by reverse iteration)
        Collections.sort(interactiveElements, Comparator.comparingInt(this::getElementPriority));

        // Request a redraw as the model (and thus interactive elements) have changed
        draw();
        System.out.println("BeamCanvas: Populated " + interactiveElements.size() + " interactive elements. Sorted by priority.");
    }

    private int getElementPriority(InteractiveElement element) {
        if (element instanceof LoadMarker) {
            Load.Type type = ((LoadMarker) element).getLoad().getType();
            switch (type) {
                case POINT:
                case MOMENT:
                    return 2; // Higher priority for selection
                case DISTRIBUTED:
                    return 1; // Lower priority for all distributed loads
                default:
                    return 0;
            }
        } else if (element instanceof SupportMarker) {
            return 2; // Higher priority (same as point loads/moments)
        }
        return 0; // Default/unknown
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
     * Zoom in at the center of the canvas
     */
    public void zoomIn() {
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        viewTransform.zoom(centerX, centerY, 1.1);
        draw();
    }
    
    /**
     * Zoom out at the center of the canvas
     */
    public void zoomOut() {
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        viewTransform.zoom(centerX, centerY, 0.9);
        draw();
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
     * Set up mouse and keyboard event handlers for interaction
     */
    /**
     * Handle global key press events (works even when canvas doesn't have focus)
     */
    private void handleGlobalKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            System.out.println("Global ESC key detected");
            unselectCurrentElement();
            requestFocus(); // Bring focus back to canvas
            event.consume();
        }
    }
    
    private void setupMouseHandlers() {
        System.out.println("[BeamCanvas] setupMouseHandlers: Setting up mouse and keyboard event handlers...");
        // Scroll to zoom
        setOnScroll(this::handleScroll);
        
        // Mouse movement for hover detection
        setOnMouseMoved(this::handleMouseMoved);

        // Mouse clicks for selection and middle-click for pan/zoom-to-fit trigger
        setOnMousePressed(event -> {
            // Request focus on any mouse press
            requestFocus();
            handleMousePressed(event);
        });
        
        setOnMouseDragged(this::handleMouseDragged); // Primarily for panning
        setOnMouseReleased(this::handleMouseReleased); // Primarily for panning
        
        // Middle-click double-click to zoom-to-fit
        setOnMouseClicked(this::handleMouseClicked);
        
        // Key press handling (e.g., ESC to unselect)
        setOnKeyPressed(this::handleKeyPressed);
        
        // Request focus when mouse enters the canvas
        setOnMouseEntered(event -> {
            System.out.println("Mouse entered canvas, requesting focus");
            requestFocus();
        });
        
        // Request focus when the canvas is clicked
        setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                System.out.println("Primary click on canvas, requesting focus");
                requestFocus();
            }
            handleMouseClicked(event);
        });
    }
    
    /**
     * Handle mouse click events for double-click zoom-to-fit
     */
    /**
     * Handle mouse movement for hover effects.
     */
    private void handleMouseMoved(MouseEvent event) {
        System.out.println("MouseMoved: (" + event.getX() + ", " + event.getY() + "), Focused: " + isFocused());
        InteractiveElement currentlyUnderMouse = null;
        // Iterate in reverse to check top-most elements first
        for (int i = interactiveElements.size() - 1; i >= 0; i--) {
            InteractiveElement element = interactiveElements.get(i);
            if (element.getScreenBounds(viewTransform).contains(event.getX(), event.getY())) {
                System.out.println("  Element " + interactiveElements.indexOf(element) + " (" + element.getClass().getSimpleName() + ") CONTAINS mouse. Bounds: " + element.getScreenBounds(viewTransform));
                currentlyUnderMouse = element;
                break;
            }
            else {
                System.out.println("  Element " + interactiveElements.indexOf(element) + " (" + element.getClass().getSimpleName() + ") does NOT contain mouse. Bounds: " + element.getScreenBounds(viewTransform));
            }
        }

        if (hoveredElement != currentlyUnderMouse) {
            System.out.println("Hover changed. Old: " + (hoveredElement != null ? hoveredElement.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(hoveredElement)) : "null") + 
                                ", New: " + (currentlyUnderMouse != null ? currentlyUnderMouse.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(currentlyUnderMouse)) : "null"));
            if (hoveredElement != null) {
                hoveredElement.setHovered(false);
            }
            hoveredElement = currentlyUnderMouse;
            if (hoveredElement != null) {
                hoveredElement.setHovered(true);
                setCursor(Cursor.HAND);
                System.out.println("Set cursor to HAND for " + hoveredElement.getClass().getSimpleName());
            } else {
                setCursor(Cursor.DEFAULT);
                System.out.println("Set cursor to DEFAULT");
            }
            draw(); // Redraw to show/hide highlight
        }
        event.consume();
    }

    /**
     * Handle key press events
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            unselectCurrentElement();
            event.consume();
        }
    }
    
    /**
     * Unselect the currently selected element if any
     */
    private void unselectCurrentElement() {
        if (selectedElement != null) {
            selectedElement.setSelected(false);
            System.out.println("  Deselected (via ESC): " + selectedElement.getClass().getSimpleName() + 
                             "@" + Integer.toHexString(System.identityHashCode(selectedElement)));
            selectedElement = null;
            draw(); // Redraw to show selection changes
        } else {
            System.out.println("No element selected to unselect");
        }
    }
    
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
        } else if (event.isPrimaryButtonDown()) {
            System.out.println("Primary button pressed. Hovered: " + (hoveredElement != null ? hoveredElement.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(hoveredElement)) : "null") + ", Selected: " + (selectedElement != null ? selectedElement.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(selectedElement)) : "null"));
            if (hoveredElement != null) {
                // An interactive element is clicked
                if (selectedElement != null && selectedElement != hoveredElement) {
                    selectedElement.setSelected(false); // Deselect previous
                    System.out.println("  Deselected old: " + selectedElement.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(selectedElement)));
                }
                selectedElement = hoveredElement;
                selectedElement.setSelected(true);
                System.out.println("  Selected new: " + selectedElement.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(selectedElement)));
                // TODO: Future: If selectedElement is already selected, could initiate editing mode (e.g., show dimension lines)
            } else {
                // Clicked on empty space
                if (selectedElement != null) {
                    selectedElement.setSelected(false);
                    System.out.println("  Deselected (clicked empty): " + selectedElement.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(selectedElement)));
                    selectedElement = null;
                }
            }
            draw(); // Redraw to show selection changes
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
        // System.out.println("[BeamCanvas] draw: Starting draw cycle. Interactive elements count: " + interactiveElements.size());
        clickableDimensionTexts.clear();
        GraphicsContext gc = getGraphicsContext2D();
        
        // Clear the canvas
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        // Draw grid first (background)
        drawGrid(gc);
        
        // Draw temporary dimensions for selected or hovered element (before beam and other elements)
        InteractiveElement elementToDimension = null;
        if (selectedElement != null) {
            elementToDimension = selectedElement;
        } else if (hoveredElement != null) {
            elementToDimension = hoveredElement;
        }

        if (elementToDimension != null) {
            dimensionLineDrawer.drawTemporaryElementDimensions(gc, elementToDimension);
        }
        
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
        
        // Draw dimension lines below the beam
        dimensionLineDrawer.drawDetailedBottomDimensionLine(gc);
        dimensionLineDrawer.drawPermanentBottomDimensionLine(gc);

        // Draw highlights for interactive elements (on top of everything except UI elements)
        for (InteractiveElement element : interactiveElements) {
            element.drawHighlight(gc, viewTransform);
        }
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
        System.out.println("Drawing supports:");
        for (Support support : beamModel.getSupports()) {
            Point2D position = viewTransform.engineeringToScreen(support.getPosition(), 0);
            System.out.println("  Support at " + support.getPosition() + " feet, type: " + support.getType());
            
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
                        drawVaryingLoad(gc, position.getX(), endPosition.getX(), position.getY(), load.getMagnitude(), load.getMagnitudeEnd(), load.getType());
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
     * @param magnitude Load magnitude
     */
    private void drawVaryingLoad(GraphicsContext gc, double x1, double x2, double y, double startMagnitude, double endMagnitude, Load.Type loadType) {
        double arrowLength = UIConstants.DISTRIBUTED_LOAD_ARROW_LENGTH;
        double arrowHeadSize = UIConstants.DISTRIBUTED_LOAD_ARROW_HEAD_SIZE;
        double spacing = UIConstants.DISTRIBUTED_LOAD_ARROW_SPACING;
        
        gc.setStroke(UIConstants.DISTRIBUTED_LOAD_COLOR); // Could use a different color for TRIANGULAR if desired
        gc.setLineWidth(UIConstants.DISTRIBUTED_LOAD_LINE_WIDTH);

        // Determine screen Y coordinates for start and end magnitudes
        // Assuming positive magnitude means downward load, so subtract from y
        // Arrow length now represents the visual height of the load representation

        // If both magnitudes are zero, or one is zero and arrowLength is also zero (e.g. from UIConstants)
        // avoid division by zero and just draw at y.
        // A more robust way would be to scale based on a 'max expected load magnitude' or similar view property.
        // For now, let's simplify: if magnitudes are very small or zero, arrowLength might be an issue.
        // Let's use a fixed arrowLength for now and scale the drawn shape based on magnitude later if needed.
        // The current arrowLength is from UIConstants.DISTRIBUTED_LOAD_ARROW_LENGTH.

        // Screen Y for the top line of the load shape
        // Positive magnitudes are typically downwards. Higher magnitude = further from beam (more negative screen Y direction if beam is at y=0 screen).
        // Let's adjust y_load_top based on magnitude directly. Max arrow length is a visual cap.
        // A simple approach: map magnitude to a pixel offset. Max magnitude maps to arrowLength.
        // This needs a reference max magnitude for scaling, or we use a fixed arrowLength for the largest part.
        // For now, let's use arrowLength as the height for the *larger* of the two magnitudes.
        double maxAbsMagnitude = Math.max(Math.abs(startMagnitude), Math.abs(endMagnitude));
        if (maxAbsMagnitude == 0) maxAbsMagnitude = 1; // Avoid division by zero if both are zero

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
            double currentFraction = (i == numArrows - 1 && numArrows > 1) ? 1.0 : (double)i / (numArrows - 1); // Ensure last arrow is at x2
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
        // For pure triangular, one of the magnitudes is zero, this format still works.
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
        
        for (double pos = 0; pos <= beamModel.getLength(); pos += interval) {
            Point2D markerPos = viewTransform.engineeringToScreen(pos, 0);
            
            // Draw marker line
            gc.strokeLine(markerPos.getX(), markerPos.getY() + markerLength, 
                          markerPos.getX(), markerPos.getY() - markerLength);
            
            // Draw position text
            gc.fillText(String.format("%.0f'", pos), markerPos.getX() - 10, markerPos.getY() + markerLength + 15);
        }
    }

    /**
     * Draw the permanent bottom dimension line spanning supports and extending to beam end if necessary.
     * 
     * @param gc Graphics context
     */

}

