package com.quickcalc.views.components.interaction;

import com.quickcalc.views.components.InteractiveElement;
import com.quickcalc.utils.ViewTransform;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.util.List;

/**
 * Handles all mouse interactions for the beam canvas
 */
public class MouseEventHandler {
    
    private final Canvas canvas;
    private final ViewTransform viewTransform;
    private final SelectionManager selectionManager;
    private final Runnable redrawCallback;
    private final Runnable fitViewCallback;
    
    // Mouse interaction state
    private double lastMouseX;
    private double lastMouseY;
    private boolean isPanning = false;
    
    // Track time of last middle click for double-click detection
    private long lastMiddleClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_MS = 300; // Double-click threshold in milliseconds
    
    public MouseEventHandler(Canvas canvas, ViewTransform viewTransform, 
                           SelectionManager selectionManager, Runnable redrawCallback,
                           Runnable fitViewCallback) {
        this.canvas = canvas;
        this.viewTransform = viewTransform;
        this.selectionManager = selectionManager;
        this.redrawCallback = redrawCallback;
        this.fitViewCallback = fitViewCallback;
    }
    
    /**
     * Handle mouse movement for hover effects
     */
    public void handleMouseMoved(MouseEvent event, List<InteractiveElement> interactiveElements) {
        boolean hoverChanged = selectionManager.updateHover(event.getX(), event.getY(), 
                                                           interactiveElements, viewTransform);
        if (hoverChanged) {
            redrawCallback.run(); // Redraw to show/hide highlight
        }
        event.consume();
    }
    
    /**
     * Handle mouse press events for selection and panning
     */
    public void handleMousePressed(MouseEvent event, List<InteractiveElement> interactiveElements) {
        if (event.isMiddleButtonDown()) {
            isPanning = true;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            canvas.setCursor(Cursor.MOVE);
            event.consume();
        } else if (event.isPrimaryButtonDown()) {
            boolean selectionChanged = selectionManager.selectHoveredElement();
            if (selectionChanged) {
                redrawCallback.run(); // Redraw to show selection changes
            }
            event.consume();
        }
    }
    
    /**
     * Handle mouse drag events for panning
     */
    public void handleMouseDragged(MouseEvent event) {
        if (isPanning) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;
            
            viewTransform.pan(deltaX, deltaY);
            
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            
            redrawCallback.run();
            event.consume();
        }
    }
    
    /**
     * Handle mouse release events for panning
     */
    public void handleMouseReleased(MouseEvent event) {
        if (isPanning) {
            isPanning = false;
            canvas.setCursor(Cursor.DEFAULT);
            event.consume();
        }
    }
    
    /**
     * Handle mouse click events for double-click zoom-to-fit
     */
    public void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMiddleClickTime < DOUBLE_CLICK_TIME_MS) {
                // Double middle-click detected, zoom to fit
                fitViewCallback.run();
                redrawCallback.run();
                event.consume();
            }
            lastMiddleClickTime = currentTime;
        }
    }
    
    /**
     * Handle mouse scroll events for zooming
     */
    public void handleScroll(ScrollEvent event) {
        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
        viewTransform.zoom(event.getX(), event.getY(), zoomFactor);
        redrawCallback.run();
        event.consume();
    }
    
    /**
     * Handle mouse enter events
     */
    public void handleMouseEntered(MouseEvent event) {
        System.out.println("Mouse entered canvas, requesting focus");
        canvas.requestFocus();
    }
    
    /**
     * Check if currently panning
     */
    public boolean isPanning() {
        return isPanning;
    }
}