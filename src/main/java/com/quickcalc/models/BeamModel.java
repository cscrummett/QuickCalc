package com.quickcalc.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a beam with its properties, supports, and loads
 */
public class BeamModel {
    
    private double length; // Length of the beam in feet
    private String material; // Material type (e.g., "Steel", "Wood")
    private String section; // Section type (e.g., "W10x12", "2x8")
    
    private List<Support> supports = new ArrayList<>();
    private List<Load> loads = new ArrayList<>();
    
    /**
     * Default constructor with a 20-foot beam
     */
    public BeamModel() {
        this(20.0); // Default 20-foot beam
    }
    
    /**
     * Constructor with specified length
     * 
     * @param length Length of the beam in feet
     */
    public BeamModel(double length) {
        this.length = length;
        this.material = "Steel"; // Default material
        this.section = "W10x12"; // Default section
        
        // Add default supports at the ends
        supports.add(new Support(0.0, Support.Type.PINNED));
        supports.add(new Support(length, Support.Type.ROLLER));
    }
    
    /**
     * Get the length of the beam
     * 
     * @return Length in feet
     */
    public double getLength() {
        return length;
    }
    
    /**
     * Set the length of the beam
     * 
     * @param length Length in feet
     */
    public void setLength(double length) {
        this.length = length;
    }
    
    /**
     * Get the material of the beam
     * 
     * @return Material name
     */
    public String getMaterial() {
        return material;
    }
    
    /**
     * Set the material of the beam
     * 
     * @param material Material name
     */
    public void setMaterial(String material) {
        this.material = material;
    }
    
    /**
     * Get the section of the beam
     * 
     * @return Section designation
     */
    public String getSection() {
        return section;
    }
    
    /**
     * Set the section of the beam
     * 
     * @param section Section designation
     */
    public void setSection(String section) {
        this.section = section;
    }
    
    /**
     * Get the list of supports
     * 
     * @return List of supports
     */
    public List<Support> getSupports() {
        return supports;
    }
    
    /**
     * Add a support to the beam
     * 
     * @param support Support to add
     */
    public void addSupport(Support support) {
        supports.add(support);
    }
    
    /**
     * Remove a support from the beam
     * 
     * @param support Support to remove
     * @return true if removed, false if not found
     */
    public boolean removeSupport(Support support) {
        return supports.remove(support);
    }
    
    /**
     * Get the list of loads
     * 
     * @return List of loads
     */
    public List<Load> getLoads() {
        return loads;
    }
    
    /**
     * Add a load to the beam
     * 
     * @param load Load to add
     */
    public void addLoad(Load load) {
        loads.add(load);
    }
    
    /**
     * Remove a load from the beam
     * 
     * @param load Load to remove
     * @return true if removed, false if not found
     */
    public boolean removeLoad(Load load) {
        return loads.remove(load);
    }
    
    /**
     * Clear all loads from the beam
     */
    public void clearLoads() {
        loads.clear();
    }
    
    /**
     * Reset the beam to default state
     * 
     * @param newLength New length for the beam
     */
    public void reset(double newLength) {
        this.length = newLength;
        this.material = "Steel";
        this.section = "W10x12";
        
        supports.clear();
        loads.clear();
        
        // Add default supports at the ends
        supports.add(new Support(0.0, Support.Type.PINNED));
        supports.add(new Support(length, Support.Type.ROLLER));
    }
}
