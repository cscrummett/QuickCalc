package com.quickcalc.utils;

/**
 * Utility class for coordinate conversion operations
 * Provides helper methods for common coordinate conversions
 */
public class CoordinateConverter {
    
    /**
     * Convert a length in engineering units to screen units
     * 
     * @param length Length in engineering units (feet)
     * @param scale Scale factor (pixels per foot)
     * @return Length in screen units (pixels)
     */
    public static double lengthToScreen(double length, double scale) {
        return length * scale;
    }
    
    /**
     * Convert a length in screen units to engineering units
     * 
     * @param pixels Length in screen units (pixels)
     * @param scale Scale factor (pixels per foot)
     * @return Length in engineering units (feet)
     */
    public static double lengthToEngineering(double pixels, double scale) {
        return pixels / scale;
    }
    
    /**
     * Snap a coordinate to the nearest grid line
     * 
     * @param coordinate Coordinate to snap
     * @param gridSize Grid size in the same units as the coordinate
     * @return Snapped coordinate
     */
    public static double snapToGrid(double coordinate, double gridSize) {
        return Math.round(coordinate / gridSize) * gridSize;
    }
    
    /**
     * Check if a point is within a certain distance of another point
     * 
     * @param x1 X-coordinate of first point
     * @param y1 Y-coordinate of first point
     * @param x2 X-coordinate of second point
     * @param y2 Y-coordinate of second point
     * @param threshold Distance threshold
     * @return True if points are within threshold distance
     */
    public static boolean isNear(double x1, double y1, double x2, double y2, double threshold) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return (dx * dx + dy * dy) <= threshold * threshold;
    }
    
    /**
     * Check if a point is near a line segment
     * 
     * @param px Point X-coordinate
     * @param py Point Y-coordinate
     * @param x1 Line segment start X-coordinate
     * @param y1 Line segment start Y-coordinate
     * @param x2 Line segment end X-coordinate
     * @param y2 Line segment end Y-coordinate
     * @param threshold Distance threshold
     * @return True if point is within threshold distance of line segment
     */
    public static boolean isNearLineSegment(double px, double py, double x1, double y1, double x2, double y2, double threshold) {
        // Calculate the squared length of the line segment
        double lineLength2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        
        if (lineLength2 == 0) {
            // Line segment is actually a point
            return isNear(px, py, x1, y1, threshold);
        }
        
        // Calculate the projection of the point onto the line
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / lineLength2;
        
        if (t < 0) {
            // Closest point is beyond the start of the segment
            return isNear(px, py, x1, y1, threshold);
        } else if (t > 1) {
            // Closest point is beyond the end of the segment
            return isNear(px, py, x2, y2, threshold);
        } else {
            // Closest point is on the segment
            double projX = x1 + t * (x2 - x1);
            double projY = y1 + t * (y2 - y1);
            return isNear(px, py, projX, projY, threshold);
        }
    }
    
    /**
     * Calculate the distance from a point to a line segment
     * 
     * @param px Point X-coordinate
     * @param py Point Y-coordinate
     * @param x1 Line segment start X-coordinate
     * @param y1 Line segment start Y-coordinate
     * @param x2 Line segment end X-coordinate
     * @param y2 Line segment end Y-coordinate
     * @return Distance from point to line segment
     */
    public static double distanceToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        // Calculate the squared length of the line segment
        double lineLength2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        
        if (lineLength2 == 0) {
            // Line segment is actually a point
            double dx = px - x1;
            double dy = py - y1;
            return Math.sqrt(dx * dx + dy * dy);
        }
        
        // Calculate the projection of the point onto the line
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / lineLength2;
        
        if (t < 0) {
            // Closest point is beyond the start of the segment
            double dx = px - x1;
            double dy = py - y1;
            return Math.sqrt(dx * dx + dy * dy);
        } else if (t > 1) {
            // Closest point is beyond the end of the segment
            double dx = px - x2;
            double dy = py - y2;
            return Math.sqrt(dx * dx + dy * dy);
        } else {
            // Closest point is on the segment
            double projX = x1 + t * (x2 - x1);
            double projY = y1 + t * (y2 - y1);
            double dx = px - projX;
            double dy = py - projY;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}
