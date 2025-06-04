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
        DISTRIBUTED, // Uniformly or Variably distributed load (UDL, triangular, trapezoidal)
        MOMENT      // Concentrated moment
    }
    
    private double position;     // Position along the beam in feet (start position for distributed loads)
    private double endPosition;  // End position for distributed loads (same as position for point loads)
    private double magnitude;    // Magnitude in kips (for point loads) or kips/ft (for distributed loads)
    private double magnitudeEnd; // End magnitude in kips/ft for distributed/triangular loads
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
    public Load(double startPosition, double endPosition, double magnitude) { // Constructor for UDL (Uniformly Distributed Load)
        if (startPosition == endPosition) {
            throw new IllegalArgumentException("Start and end positions cannot be the same for a distributed load.");
        }
        this.position = startPosition;
        this.endPosition = endPosition;
        this.magnitude = magnitude;
        this.type = Type.DISTRIBUTED;
        this.magnitudeEnd = magnitude; // For UDL, start and end magnitudes are the same
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
    public double getMagnitude() { // For distributed/triangular, this is start magnitude
        return magnitude;
    }
    
    /**
     * Set the magnitude of the load
     * 
     * @param magnitude Magnitude in kips (or kips/ft for distributed loads)
     */
    public void setMagnitude(double magnitude) { // For distributed/triangular, this sets start magnitude
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
     * Get the end magnitude of the load (for distributed/triangular loads)
     * 
     * @return End magnitude in kips/ft
     */
    public double getMagnitudeEnd() {
        return magnitudeEnd;
    }

    /**
     * Set the end magnitude of the load (for distributed/triangular loads)
     * 
     * @param magnitudeEnd End magnitude in kips/ft
     */
    public void setMagnitudeEnd(double magnitudeEnd) {
        if (type == Type.DISTRIBUTED) {
            this.magnitudeEnd = magnitudeEnd;
        } else {
            throw new IllegalStateException("End magnitude only applies to distributed loads");
        }
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
     * Constructor for uniformly or variably distributed load (UDL, triangular, trapezoidal).
     * For a UDL, startMagnitude and endMagnitude should be the same.
     * For a triangular load, one of the magnitudes should be zero.
     * 
     * @param startPosition Start position along the beam in feet
     * @param endPosition End position along the beam in feet
     * @param startMagnitude Start magnitude in kips/ft
     * @param endMagnitude End magnitude in kips/ft (can be same as startMagnitude for UDL)
     */
    public Load(double startPosition, double endPosition, double startMagnitude, double endMagnitude) {
        if (startPosition == endPosition) {
            throw new IllegalArgumentException("Start and end positions cannot be the same for a distributed load.");
        }
        this.position = startPosition;
        this.endPosition = endPosition;
        this.magnitude = startMagnitude;
        this.magnitudeEnd = endMagnitude;
        this.type = Type.DISTRIBUTED;
    }
    
    /**
     * Get the length of the load (for distributed loads)
     * 
     * @return Length in feet (0 for point loads)
     */
    public double getLength() {
        if (type == Type.DISTRIBUTED) {
            return Math.abs(endPosition - position); // Use Math.abs for safety, though start should be < end
        } else {
            return 0.0;
        }
    }
    
    @Override
    public String toString() {
        if (type == Type.POINT) {
            return magnitude + " kip point load at " + position + " ft";
        } else if (type == Type.DISTRIBUTED) {
            if (magnitude == magnitudeEnd) {
                return magnitude + " kip/ft distributed load from " + position + " ft to " + endPosition + " ft";
            } else if (magnitude == 0 || magnitudeEnd == 0) {
                return String.format("%.2f to %.2f kip/ft triangular load from %.2f ft to %.2f ft", 
                                     magnitude, magnitudeEnd, position, endPosition);
            } else {
                return String.format("%.2f to %.2f kip/ft trapezoidal load from %.2f ft to %.2f ft", 
                                     magnitude, magnitudeEnd, position, endPosition);
            }
        } else if (type == Type.MOMENT) {
            return magnitude + " kip-ft moment at " + position + " ft";
        } else { 
            throw new IllegalStateException("Unknown load type");
        }
    }
}
