package com.quickcalc.views.components;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Support;
import com.quickcalc.utils.ViewTransform;
import com.quickcalc.constants.UIConstants;
import com.quickcalc.utils.Point2D; // Import Point2D

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;

public class SupportMarker extends InteractiveElement {

    private Support support;

    public SupportMarker(Support support, BeamModel beamModel) {
        super(support, beamModel);
        this.support = support;
    }

    @Override
    public BoundingBox getScreenBounds(ViewTransform viewTransform) {
        Point2D supportScreenPos = viewTransform.engineeringToScreen(support.getPosition(), 0);
        double screenX = supportScreenPos.getX();
        double screenYBase = supportScreenPos.getY();

        double markerWidth = UIConstants.SUPPORT_MARKER_WIDTH_PX;
        double markerHeight = UIConstants.SUPPORT_MARKER_HEIGHT_PX;

        // Assuming the support symbol is drawn centered horizontally at screenX,
        // and its top edge is at screenYBase (if drawn below the beam line)
        // or centered vertically if that's how BeamCanvas draws them.
        // For now, let's assume it's drawn from screenYBase downwards.
        return new BoundingBox(screenX - markerWidth / 2, screenYBase, markerWidth, markerHeight);
    }

    @Override
    public void drawHighlight(GraphicsContext gc, ViewTransform viewTransform) {
        if (isHovered() || isSelected()) {
            BoundingBox bounds = getScreenBounds(viewTransform);
            gc.setStroke(UIConstants.HIGHLIGHT_COLOR);
            gc.setLineWidth(UIConstants.HIGHLIGHT_LINE_WIDTH);
            gc.strokeRect(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
    }
}
