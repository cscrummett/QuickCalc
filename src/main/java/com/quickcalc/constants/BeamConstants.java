package com.quickcalc.constants;

/**
 * Engineering constants for beam calculations
 */
public class BeamConstants {
    
    // Unit conversion constants
    public static final double KIPS_TO_POUNDS = 1000.0;
    public static final double POUNDS_TO_KIPS = 0.001;
    public static final double FEET_TO_INCHES = 12.0;
    public static final double INCHES_TO_FEET = 1.0 / 12.0;
    
    // Material properties (modulus of elasticity in ksi)
    public static final double E_STEEL = 29000.0;
    public static final double E_CONCRETE = 3600.0;
    public static final double E_WOOD = 1600.0;
    
    // Safety factors
    public static final double SAFETY_FACTOR_STEEL = 1.67;
    public static final double SAFETY_FACTOR_CONCRETE = 1.5;
    public static final double SAFETY_FACTOR_WOOD = 2.0;
    
    // Default beam properties
    public static final double DEFAULT_BEAM_LENGTH = 20.0; // feet
    public static final double DEFAULT_BEAM_DEPTH = 10.0; // inches
    public static final double DEFAULT_BEAM_WIDTH = 5.0; // inches
    
    // Gravity acceleration (ft/s²)
    public static final double GRAVITY = 32.2;
    
    // Maximum allowable values
    public static final double MAX_BEAM_LENGTH = 200.0; // feet
    public static final double MAX_POINT_LOAD = 100.0; // kips
    public static final double MAX_DISTRIBUTED_LOAD = 10.0; // kips/ft
    public static final double MAX_MOMENT = 1000.0; // kip-ft
    
    // Minimum allowable values
    public static final double MIN_BEAM_LENGTH = 1.0; // feet
}
