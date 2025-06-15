package com.quickcalc.views.components.interaction;

import com.quickcalc.views.components.InteractiveElement;
import com.quickcalc.utils.ViewTransform;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;

import java.util.List;

/**
 * Manages element selection and hover states
 */
public class SelectionManager {
    
    private InteractiveElement hoveredElement = null;
    private InteractiveElement selectedElement = null;
    private Canvas canvas;
    private Runnable redrawCallback;
    
    public SelectionManager(Canvas canvas, Runnable redrawCallback) {
        this.canvas = canvas;
        this.redrawCallback = redrawCallback;
    }
    
    /**
     * Update hover state based on mouse position
     * 
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     * @param interactiveElements List of interactive elements
     * @param viewTransform View transformation
     * @return true if hover state changed
     */
    public boolean updateHover(double mouseX, double mouseY, 
                              List<InteractiveElement> interactiveElements, 
                              ViewTransform viewTransform) {
        
        InteractiveElement currentlyUnderMouse = null;
        
        // Iterate in reverse to check top-most elements first
        for (int i = interactiveElements.size() - 1; i >= 0; i--) {
            InteractiveElement element = interactiveElements.get(i);
            if (element.getScreenBounds(viewTransform).contains(mouseX, mouseY)) {
                currentlyUnderMouse = element;
                break;
            }
        }

        if (hoveredElement != currentlyUnderMouse) {
            if (hoveredElement != null) {
                hoveredElement.setHovered(false);
            }
            hoveredElement = currentlyUnderMouse;
            if (hoveredElement != null) {
                hoveredElement.setHovered(true);
                canvas.setCursor(Cursor.HAND);
            } else {
                canvas.setCursor(Cursor.DEFAULT);
            }
            return true; // Hover state changed
        }
        return false; // No change
    }
    
    /**
     * Handle element selection
     * 
     * @return true if selection state changed
     */
    public boolean selectHoveredElement() {
        boolean selectionChanged = false;
        
        if (hoveredElement != null) {
            // An interactive element is clicked
            if (selectedElement != null && selectedElement != hoveredElement) {
                selectedElement.setSelected(false); // Deselect previous
            }
            selectedElement = hoveredElement;
            selectedElement.setSelected(true);
            selectionChanged = true;
        } else {
            // Clicked on empty space
            if (selectedElement != null) {
                selectedElement.setSelected(false);
                selectedElement = null;
                selectionChanged = true;
            }
        }
        
        return selectionChanged;
    }
    
    /**
     * Unselect the currently selected element if any
     * 
     * @return true if an element was unselected
     */
    public boolean unselectCurrentElement() {
        if (selectedElement != null) {
            selectedElement.setSelected(false);
            selectedElement = null;
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Get the currently hovered element
     */
    public InteractiveElement getHoveredElement() {
        return hoveredElement;
    }
    
    /**
     * Get the currently selected element
     */
    public InteractiveElement getSelectedElement() {
        return selectedElement;
    }
    
    /**
     * Get the element to show dimensions for (selected takes priority over hovered)
     */
    public InteractiveElement getElementToShowDimensions() {
        return selectedElement != null ? selectedElement : hoveredElement;
    }
}