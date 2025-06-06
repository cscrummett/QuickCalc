package com.quickcalc.constants;

import javafx.scene.paint.Color;

/**
 * UI constants for the beam designer
 */
public class UIConstants {
    
    // Canvas dimensions and padding
    public static final double DEFAULT_CANVAS_WIDTH = 800.0;
    public static final double DEFAULT_CANVAS_HEIGHT = 600.0;
    public static final double CANVAS_PADDING_X = 50.0;
    public static final double CANVAS_PADDING_Y = 50.0;
    
    // Beam drawing properties
    public static final double BEAM_LINE_WIDTH = 2.0;
    public static final Color BEAM_COLOR = Color.BLACK;
    public static final double BEAM_HEIGHT = 10.0; // Visual height for beam representation
    
    // Grid properties
    public static final double GRID_LINE_WIDTH = 0.5;
    public static final Color GRID_COLOR = Color.LIGHTGRAY;
    public static final double GRID_SPACING = 1.0; // 1 foot grid
    public static final int GRID_STYLE_NONE = 0;
    public static final int GRID_STYLE_LINES = 1;
    
    // Support drawing properties
    public static final double SUPPORT_SIZE = 20.0;
    public static final Color SUPPORT_COLOR = Color.BLACK;
    public static final double SUPPORT_LINE_WIDTH = 1.5;
    
    // Load drawing properties
    public static final double POINT_LOAD_ARROW_LENGTH = 40.0;
    public static final double POINT_LOAD_ARROW_HEAD_SIZE = 10.0;
    public static final Color POINT_LOAD_COLOR = Color.RED;
    public static final double POINT_LOAD_LINE_WIDTH = 2.0;
    
    public static final double DISTRIBUTED_LOAD_ARROW_LENGTH = 30.0;
    public static final double DISTRIBUTED_LOAD_ARROW_HEAD_SIZE = 8.0;
    public static final double DISTRIBUTED_LOAD_ARROW_SPACING = 20.0;
    public static final Color DISTRIBUTED_LOAD_COLOR = Color.BLUE;
    public static final double DISTRIBUTED_LOAD_LINE_WIDTH = 2.0;
    
    public static final double MOMENT_RADIUS = 20.0;
    public static final double MOMENT_ARROW_HEAD_SIZE = 8.0;
    public static final Color MOMENT_COLOR = Color.GREEN;
    public static final double MOMENT_LINE_WIDTH = 2.0;
    
    // Text properties
    public static final double TEXT_SIZE = 12.0;
    public static final Color TEXT_COLOR = Color.BLACK;
    
    // Selection properties
    public static final Color SELECTION_COLOR = Color.ORANGE;
    public static final double SELECTION_LINE_WIDTH = 2.0;
    public static final double SELECTION_HANDLE_SIZE = 6.0;
    
    // Snap distance (in pixels)
    public static final double SNAP_DISTANCE = 10.0;

    // Marker Interaction Properties (for bounding boxes and highlighting)
    public static final double POINT_LOAD_MARKER_HEIGHT_PX = 40.0; // Corresponds to POINT_LOAD_ARROW_LENGTH
    public static final double POINT_LOAD_MARKER_WIDTH_PX = 15.0;  // Based on POINT_LOAD_ARROW_HEAD_SIZE, made a bit wider
    public static final double DISTRIBUTED_LOAD_MARKER_HEIGHT_PX = 20.0; // A defined height for UDL interaction block
    public static final double MOMENT_MARKER_RADIUS_PX = MOMENT_RADIUS; // Use existing visual radius for interaction
    
    public static final double SUPPORT_MARKER_WIDTH_PX = SUPPORT_SIZE; // Use existing visual size for interaction
    public static final double SUPPORT_MARKER_HEIGHT_PX = SUPPORT_SIZE; // Use existing visual size for interaction

    public static final Color HIGHLIGHT_COLOR = Color.ORANGE; // Using existing SELECTION_COLOR value
    public static final double HIGHLIGHT_LINE_WIDTH = 2.5;

    // Engineering Constraints & Behavior
    public static final double UDL_MIN_LENGTH_FT = 0.5; // Minimum length for a UDL in feet

    public static final double ENGINEERING_SNAP_INCREMENT_FT = 0.1; // Snap increment in feet for precise placement
}
