package com.quickcalc.views.components;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;
import com.quickcalc.views.components.ClickableDimensionText;
import com.quickcalc.views.components.renderers.DrawingContext;
import com.quickcalc.views.components.interaction.SelectionManager;
import com.quickcalc.views.components.interaction.MouseEventHandler;
import com.quickcalc.views.components.interaction.KeyboardEventHandler;
import com.quickcalc.views.components.interaction.commands.CommandManager;
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
    
    // Interactive elements
    private List<InteractiveElement> interactiveElements = new ArrayList<>();
    private List<ClickableDimensionText> clickableDimensionTexts = new ArrayList<>();
    private DimensionLineDrawer dimensionLineDrawer;
    private DrawingContext drawingContext;
    
    // Interaction managers
    private SelectionManager selectionManager;
    private MouseEventHandler mouseEventHandler;
    private KeyboardEventHandler keyboardEventHandler;
    private CommandManager commandManager;
    
    // Coordinate update callback
    private MouseEventHandler.CoordinateUpdateCallback coordinateUpdateCallback;
    
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

        // Initialize the dimension line drawer and drawing context
        this.dimensionLineDrawer = new DimensionLineDrawer(this.viewTransform, this.beamModel, this.clickableDimensionTexts);
        this.drawingContext = new DrawingContext(this.viewTransform, this.beamModel, this.clickableDimensionTexts);
        
        // Initialize interaction managers
        this.commandManager = new CommandManager();
        this.selectionManager = new SelectionManager(this, this::draw);
        this.mouseEventHandler = new MouseEventHandler(this, this.viewTransform, this.selectionManager, 
                                                      this::draw, this::fitViewToBeam);
        this.keyboardEventHandler = new KeyboardEventHandler(this, this.selectionManager, this::draw);
        
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
        
        // Set up coordinate update callback if available
        if (coordinateUpdateCallback != null) {
            mouseEventHandler.setCoordinateUpdateCallback(coordinateUpdateCallback);
        }
        
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
        // Update the beam model in the dimension line drawer and drawing context
        this.dimensionLineDrawer = new DimensionLineDrawer(this.viewTransform, this.beamModel, this.clickableDimensionTexts);
        this.drawingContext = new DrawingContext(this.viewTransform, this.beamModel, this.clickableDimensionTexts);
        
        // Update interaction managers with new model
        this.mouseEventHandler = new MouseEventHandler(this, this.viewTransform, this.selectionManager, 
                                                      this::draw, this::fitViewToBeam);
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
     * Get the selection manager
     */
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    /**
     * Get the command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
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
    
    
    /**
     * Handle global key press events (works even when canvas doesn't have focus)
     */
    private void handleGlobalKeyPress(KeyEvent event) {
        keyboardEventHandler.handleGlobalKeyPress(event);
    }
    
    /**
     * Set up mouse and keyboard event handlers for interaction
     */
    private void setupMouseHandlers() {
        // Mouse movement for hover detection
        setOnMouseMoved(event -> mouseEventHandler.handleMouseMoved(event, interactiveElements));

        // Mouse clicks for selection and middle-click for pan/zoom-to-fit trigger
        setOnMousePressed(event -> {
            // Request focus on any mouse press
            requestFocus();
            mouseEventHandler.handleMousePressed(event, interactiveElements);
        });
        
        // Mouse dragging for panning
        setOnMouseDragged(event -> mouseEventHandler.handleMouseDragged(event));
        
        // Mouse release for panning
        setOnMouseReleased(event -> mouseEventHandler.handleMouseReleased(event));
        
        // Mouse clicks for double-click zoom-to-fit
        setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                requestFocus();
            }
            mouseEventHandler.handleMouseClicked(event);
        });
        
        // Scroll to zoom
        setOnScroll(event -> mouseEventHandler.handleScroll(event));
        
        // Key press handling (e.g., ESC to unselect)
        setOnKeyPressed(event -> keyboardEventHandler.handleKeyPressed(event));
        
        // Request focus when mouse enters the canvas
        setOnMouseEntered(event -> mouseEventHandler.handleMouseEntered(event));
    }
    
    
    /**
     * Draw the beam with its supports and loads
     */
    public void draw() {
        // System.out.println("[BeamCanvas] draw: Starting draw cycle. Interactive elements count: " + interactiveElements.size());
        clickableDimensionTexts.clear();
        GraphicsContext gc = getGraphicsContext2D();
        
        // Use the drawing context to render everything
        drawingContext.drawBeam(gc, beamModel, viewTransform, gridStyle, 
                              getWidth(), getHeight(), selectionManager.getSelectedElement(), 
                              selectionManager.getHoveredElement(), interactiveElements, dimensionLineDrawer);
    }

    
    
    
    
    
    
    
    
    

    /**
     * Draw the permanent bottom dimension line spanning supports and extending to beam end if necessary.
     * 
     * @param gc Graphics context
     */
    
    /**
     * Set the coordinate update callback for mouse position reporting
     * 
     * @param callback The callback to receive coordinate updates
     */
    public void setCoordinateUpdateCallback(MouseEventHandler.CoordinateUpdateCallback callback) {
        this.coordinateUpdateCallback = callback;
        if (mouseEventHandler != null) {
            mouseEventHandler.setCoordinateUpdateCallback(callback);
        }
    }

}

