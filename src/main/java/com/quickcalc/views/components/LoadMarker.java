package com.quickcalc.views.components;

import com.quickcalc.constants.UIConstants;
import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.utils.ViewTransform;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;

public class LoadMarker extends InteractiveElement {
    private Load load;

    public LoadMarker(Load load, BeamModel beamModel) {
        super(load, beamModel);
        this.load = load;
    }

    public Load getLoad() {
        return load;
    }

    @Override
    public BoundingBox getScreenBounds(ViewTransform viewTransform) {
        if (load == null || viewTransform == null) {
            return new BoundingBox(0, 0, 0, 0); 
        }
        
        com.quickcalc.utils.Point2D loadScreenPos = viewTransform.engineeringToScreen(load.getPosition(), 0);
        double screenX = loadScreenPos.getX();
        double screenYBase = loadScreenPos.getY();

        double defaultWidth = 20; 
        double defaultHeight = 20;

        if (load.getType() == Load.Type.POINT) {
            double markerWidth = UIConstants.POINT_LOAD_MARKER_WIDTH_PX;
            double visualHeight = UIConstants.POINT_LOAD_ARROW_LENGTH;
            double minYValue;
            if (load.getMagnitude() < 0) { // Assuming negative magnitude means visually UPWARD
                minYValue = screenYBase - visualHeight; // Hitbox above beam
            } else { // Assuming positive or zero magnitude means visually DOWNWARD or zero
                minYValue = screenYBase; // Hitbox at/below beam
            }
            return new BoundingBox(screenX - markerWidth / 2, minYValue, markerWidth, visualHeight);
        } else if (load.getType() == Load.Type.DISTRIBUTED) {
            com.quickcalc.utils.Point2D loadEndScreenPos = viewTransform.engineeringToScreen(load.getEndPosition(), 0);
            double endX = loadEndScreenPos.getX();
            double distVisualHeight = UIConstants.DISTRIBUTED_LOAD_ARROW_LENGTH;
            double distMinY;
            if (load.getMagnitude() < 0) { // Assuming negative magnitude means visually UPWARD
                distMinY = screenYBase - distVisualHeight; // Hitbox above beam
            } else { // Assuming positive or zero magnitude means visually DOWNWARD or zero
                distMinY = screenYBase; // Hitbox at/below beam
            }
            return new BoundingBox(Math.min(screenX, endX), distMinY, Math.abs(endX - screenX), distVisualHeight);
        } else if (load.getType() == Load.Type.MOMENT) {
            double markerRadius = UIConstants.MOMENT_MARKER_RADIUS_PX;
            return new BoundingBox(screenX - markerRadius, screenYBase - markerRadius, 2 * markerRadius, 2 * markerRadius);
        }
        return new BoundingBox(screenX - defaultWidth / 2, screenYBase - defaultHeight / 2, defaultWidth, defaultHeight);
    }

    @Override
    public void drawHighlight(GraphicsContext gc, ViewTransform viewTransform) {
        if (gc == null || viewTransform == null) {
            return;
        }
        if (isHovered() || isSelected()) {
            BoundingBox bounds = getScreenBounds(viewTransform);
            if (bounds == null || bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
                return;
            }
            gc.setStroke(UIConstants.HIGHLIGHT_COLOR);
            gc.setLineWidth(UIConstants.HIGHLIGHT_LINE_WIDTH);
            gc.strokeRect(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
    }
}
