package com.quickcalc.views.components;

import javafx.geometry.BoundingBox;

public record ClickableDimensionText(
    BoundingBox screenBounds,
    Object modelElement, // The Load or Support
    String dimensionType, // "distanceToLeftAnchor", "distanceToRightAnchor", "elementLength"
    double originalValue, // The length of this dimension segment
    String originalText,  // The text that was displayed
    // Additional context for editing:
    double anchor1EngX,   // For "distanceToLeftAnchor", this is leftAnchorEngX. For "distanceToRightAnchor", this is elementEngX (or elementEngX2).
    double anchor2EngX    // For "distanceToLeftAnchor", this is elementEngX1. For "distanceToRightAnchor", this is rightAnchorEngX.
                          // For "elementLength", anchor1 is elementEngX1, anchor2 is elementEngX2.
) {}
