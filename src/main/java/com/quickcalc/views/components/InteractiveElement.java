package com.quickcalc.views.components;

import com.quickcalc.models.BeamModel; // Assuming it might need BeamModel
import com.quickcalc.utils.ViewTransform; // For coordinate transformations
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;

public abstract class InteractiveElement {
    protected Object modelElement; // The actual Load, Support, etc.
    protected BeamModel beamModel; // Reference to the main beam model
    protected boolean isSelected;
    protected boolean isHovered;

    // Constructor
    public InteractiveElement(Object modelElement, BeamModel beamModel) {
        this.modelElement = modelElement;
        this.beamModel = beamModel;
        this.isSelected = false;
        this.isHovered = false;
    }

    // Abstract methods to be implemented by subclasses
    public abstract BoundingBox getScreenBounds(ViewTransform viewTransform);
    public abstract void drawHighlight(GraphicsContext gc, ViewTransform viewTransform);

    // Getters and Setters
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isHovered() {
        return isHovered;
    }

    public void setHovered(boolean hovered) {
        isHovered = hovered;
    }

    public Object getModelElement() {
        return modelElement;
    }
    
    public BeamModel getBeamModel() {
        return beamModel;
    }
}
