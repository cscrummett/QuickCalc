package com.quickcalc.models;

/**
 * Model class representing a beam support
 */
public class Support {
    
    /**
     * Enum for support types
     */
    public enum Type {
        PINNED,  // Restrains translation in all directions, allows rotation
        ROLLER,  // Restrains translation in vertical direction only, allows rotation
        FIXED    // Restrains translation and rotation in all directions
    }
    
    private double position; // Position along the beam in feet
    private Type type;       // Type of support
    
    /**
     * Constructor
     * 
     * @param position Position along the beam in feet
     * @param type Type of support
     */
    public Support(double position, Type type) {
        this.position = position;
        this.type = type;
    }
    
    /**
     * Get the position of the support
     * 
     * @return Position in feet
     */
    public double getPosition() {
        return position;
    }
    
    /**
     * Set the position of the support
     * 
     * @param position Position in feet
     */
    public void setPosition(double position) {
        this.position = position;
    }
    
    /**
     * Get the type of the support
     * 
     * @return Support type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Set the type of the support
     * 
     * @param type Support type
     */
    public void setType(Type type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return type + " support at " + position + " ft";
    }
}
