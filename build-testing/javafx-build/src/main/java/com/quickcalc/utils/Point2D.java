package com.quickcalc.utils;

/**
 * Utility class for 2D point operations
 * This extends the functionality of JavaFX's Point2D with additional methods
 */
public class Point2D {
    
    private double x;
    private double y;
    
    /**
     * Constructor
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get the X-coordinate
     * 
     * @return X-coordinate
     */
    public double getX() {
        return x;
    }
    
    /**
     * Get the Y-coordinate
     * 
     * @return Y-coordinate
     */
    public double getY() {
        return y;
    }
    
    /**
     * Set the X-coordinate
     * 
     * @param x New X-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }
    
    /**
     * Set the Y-coordinate
     * 
     * @param y New Y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * Add another point to this point
     * 
     * @param other Point to add
     * @return New point representing the sum
     */
    public Point2D add(Point2D other) {
        return new Point2D(x + other.x, y + other.y);
    }
    
    /**
     * Subtract another point from this point
     * 
     * @param other Point to subtract
     * @return New point representing the difference
     */
    public Point2D subtract(Point2D other) {
        return new Point2D(x - other.x, y - other.y);
    }
    
    /**
     * Multiply this point by a scalar
     * 
     * @param scalar Scalar value
     * @return New point representing the product
     */
    public Point2D multiply(double scalar) {
        return new Point2D(x * scalar, y * scalar);
    }
    
    /**
     * Calculate the distance to another point
     * 
     * @param other Other point
     * @return Distance between the points
     */
    public double distance(Point2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate the magnitude (length) of this point as a vector
     * 
     * @return Magnitude
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * Normalize this point as a vector (make its magnitude 1)
     * 
     * @return New normalized point
     */
    public Point2D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            return new Point2D(0, 0);
        }
        return new Point2D(x / mag, y / mag);
    }
    
    /**
     * Convert to JavaFX Point2D
     * 
     * @return JavaFX Point2D
     */
    public javafx.geometry.Point2D toJavaFXPoint2D() {
        return new javafx.geometry.Point2D(x, y);
    }
    
    /**
     * Create from JavaFX Point2D
     * 
     * @param point JavaFX Point2D
     * @return New Point2D
     */
    public static Point2D fromJavaFXPoint2D(javafx.geometry.Point2D point) {
        return new Point2D(point.getX(), point.getY());
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
