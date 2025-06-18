package com.quickcalc.views.components.interaction;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Handles keyboard events for the beam canvas
 */
public class KeyboardEventHandler {
    
    private final Canvas canvas;
    private final SelectionManager selectionManager;
    private final Runnable redrawCallback;
    
    public KeyboardEventHandler(Canvas canvas, SelectionManager selectionManager, 
                              Runnable redrawCallback) {
        this.canvas = canvas;
        this.selectionManager = selectionManager;
        this.redrawCallback = redrawCallback;
    }
    
    /**
     * Handle key press events (works when canvas has focus)
     */
    public void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            boolean elementUnselected = selectionManager.unselectCurrentElement();
            if (elementUnselected) {
                redrawCallback.run(); // Redraw to show selection changes
            }
            event.consume();
        }
        // Future: Add more keyboard shortcuts here
        // - Delete key for removing selected elements
        // - Ctrl+Z for undo
        // - Arrow keys for moving elements
        // - Number keys for quick tool selection
    }
    
    /**
     * Handle global key press events (works even when canvas doesn't have focus)
     */
    public void handleGlobalKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            System.out.println("Global ESC key detected");
            boolean elementUnselected = selectionManager.unselectCurrentElement();
            canvas.requestFocus(); // Bring focus back to canvas
            if (elementUnselected) {
                redrawCallback.run(); // Redraw to show selection changes
            }
            event.consume();
        }
    }
}