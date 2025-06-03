package com.quickcalc.models;

/**
 * Model class representing a beam load
 */
public class Load {
    
    /**
     * Enum for load types
     */
    public enum Type {
        POINT,      // Concentrated point load
        DISTRIBUTED, // Uniformly distributed load
        MOMENT      // Concentrated moment
    }
    
    private double position;     // Position along the beam in feet (start position for distributed loads)
    private double endPosition;  // End position for distributed loads (same as position for point loads)
    private double magnitude;    // Magnitude in kips (for point loads) or kips/ft (for distributed loads)
    private Type type;           // Type of load
    
    /**
     * Constructor for point load or moment
     * 
     * @param position Position along the beam in feet
     * @param magnitude Magnitude in kips (or kip-ft for moments)
     * @param type Type of load
     */
    public Load(double position, double magnitude, Type type) {
        this.position = position;
        this.magnitude = magnitude;
        this.type = type;
        
        // For point loads and moments, end position is the same as start position
        if (type == Type.POINT || type == Type.MOMENT) {
            this.endPosition = position;
        } else {
            throw new IllegalArgumentException("Distributed loads require end position");
        }
    }
    
    /**
     * Constructor for distributed load
     * 
     * @param startPosition Start position along the beam in feet
     * @param endPosition End position along the beam in feet
     * @param magnitude Magnitude in kips/ft
     */
    public Load(double startPosition, double endPosition, double magnitude) {
        this.position = startPosition;
        this.endPosition = endPosition;
        this.magnitude = magnitude;
        this.type = Type.DISTRIBUTED;
    }
    
    /**
     * Get the position of the load
     * 
     * @return Position in feet
     */
    public double getPosition() {
        return position;
    }
    
    /**
     * Set the position of the load
     * 
     * @param position Position in feet
     */
    public void setPosition(double position) {
        this.position = position;
        
        // For point loads and moments, update end position as well
        if (type == Type.POINT || type == Type.MOMENT) {
            this.endPosition = position;
        }
    }
    
    /**
     * Get the end position of the load (for distributed loads)
     * 
     * @return End position in feet
     */
    public double getEndPosition() {
        return endPosition;
    }
    
    /**
     * Set the end position of the load (for distributed loads)
     * 
     * @param endPosition End position in feet
     */
    public void setEndPosition(double endPosition) {
        if (type == Type.DISTRIBUTED) {
            this.endPosition = endPosition;
        } else {
            throw new IllegalStateException("End position only applies to distributed loads");
        }
    }
    
    /**
     * Get the magnitude of the load
     * 
     * @return Magnitude in kips (or kips/ft for distributed loads)
     */
    public double getMagnitude() {
        return magnitude;
    }
    
    /**
     * Set the magnitude of the load
     * 
     * @param magnitude Magnitude in kips (or kips/ft for distributed loads)
     */
    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
    
    /**
     * Get the type of the load
     * 
     * @return Load type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Set the type of the load
     * 
     * @param type Load type
     */
    public void setType(Type type) {
        this.type = type;
    }
    
    /**
     * Get the length of the load (for distributed loads)
     * 
     * @return Length in feet (0 for point loads)
     */
    public double getLength() {
        if (type == Type.DISTRIBUTED) {
            return endPosition - position;
        } else {
            return 0.0;
        }
    }
    
    @Override
    public String toString() {
        if (type == Type.POINT) {
            return magnitude + " kip point load at " + position + " ft";
        } else if (type == Type.DISTRIBUTED) {
            return magnitude + " kip/ft distributed load from " + position + " ft to " + endPosition + " ft";
        } else { // MOMENT
            return magnitude + " kip-ft moment at " + position + " ft";
        }
    }
}
