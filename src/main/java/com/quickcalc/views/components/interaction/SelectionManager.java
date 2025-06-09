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
        
        System.out.println("MouseMoved: (" + mouseX + ", " + mouseY + ")");
        InteractiveElement currentlyUnderMouse = null;
        
        // Iterate in reverse to check top-most elements first
        for (int i = interactiveElements.size() - 1; i >= 0; i--) {
            InteractiveElement element = interactiveElements.get(i);
            if (element.getScreenBounds(viewTransform).contains(mouseX, mouseY)) {
                System.out.println("  Element " + interactiveElements.indexOf(element) + 
                                 " (" + element.getClass().getSimpleName() + ") CONTAINS mouse. Bounds: " + 
                                 element.getScreenBounds(viewTransform));
                currentlyUnderMouse = element;
                break;
            } else {
                System.out.println("  Element " + interactiveElements.indexOf(element) + 
                                 " (" + element.getClass().getSimpleName() + ") does NOT contain mouse. Bounds: " + 
                                 element.getScreenBounds(viewTransform));
            }
        }

        if (hoveredElement != currentlyUnderMouse) {
            System.out.println("Hover changed. Old: " + 
                             (hoveredElement != null ? hoveredElement.getClass().getSimpleName() + "@" + 
                              Integer.toHexString(System.identityHashCode(hoveredElement)) : "null") + 
                             ", New: " + 
                             (currentlyUnderMouse != null ? currentlyUnderMouse.getClass().getSimpleName() + "@" + 
                              Integer.toHexString(System.identityHashCode(currentlyUnderMouse)) : "null"));
            
            if (hoveredElement != null) {
                hoveredElement.setHovered(false);
            }
            hoveredElement = currentlyUnderMouse;
            if (hoveredElement != null) {
                hoveredElement.setHovered(true);
                canvas.setCursor(Cursor.HAND);
                System.out.println("Set cursor to HAND for " + hoveredElement.getClass().getSimpleName());
            } else {
                canvas.setCursor(Cursor.DEFAULT);
                System.out.println("Set cursor to DEFAULT");
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
        System.out.println("Primary button pressed. Hovered: " + 
                         (hoveredElement != null ? hoveredElement.getClass().getSimpleName() + "@" + 
                          Integer.toHexString(System.identityHashCode(hoveredElement)) : "null") + 
                         ", Selected: " + 
                         (selectedElement != null ? selectedElement.getClass().getSimpleName() + "@" + 
                          Integer.toHexString(System.identityHashCode(selectedElement)) : "null"));
        
        boolean selectionChanged = false;
        
        if (hoveredElement != null) {
            // An interactive element is clicked
            if (selectedElement != null && selectedElement != hoveredElement) {
                selectedElement.setSelected(false); // Deselect previous
                System.out.println("  Deselected old: " + selectedElement.getClass().getSimpleName() + "@" + 
                                 Integer.toHexString(System.identityHashCode(selectedElement)));
            }
            selectedElement = hoveredElement;
            selectedElement.setSelected(true);
            System.out.println("  Selected new: " + selectedElement.getClass().getSimpleName() + "@" + 
                             Integer.toHexString(System.identityHashCode(selectedElement)));
            selectionChanged = true;
        } else {
            // Clicked on empty space
            if (selectedElement != null) {
                selectedElement.setSelected(false);
                System.out.println("  Deselected (clicked empty): " + selectedElement.getClass().getSimpleName() + "@" + 
                                 Integer.toHexString(System.identityHashCode(selectedElement)));
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
            System.out.println("  Deselected (via ESC): " + selectedElement.getClass().getSimpleName() + 
                             "@" + Integer.toHexString(System.identityHashCode(selectedElement)));
            selectedElement = null;
            return true;
        } else {
            System.out.println("No element selected to unselect");
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