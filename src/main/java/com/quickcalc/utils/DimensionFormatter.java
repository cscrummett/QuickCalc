package com.quickcalc.utils;

/**
 * Utility class for formatting dimensions in different units and formats
 */
public class DimensionFormatter {
    
    /**
     * Enum for different dimension display formats
     */
    public enum DisplayFormat {
        DECIMAL_FEET,    // 5.5'
        FEET_INCHES      // 5' 6"
    }
    
    private static DisplayFormat currentFormat = DisplayFormat.FEET_INCHES; // Default to feet-inches
    
    /**
     * Format a dimension value based on the current display format
     * 
     * @param decimalFeet The dimension in decimal feet
     * @return Formatted string representation
     */
    public static String formatDimension(double decimalFeet) {
        switch (currentFormat) {
            case DECIMAL_FEET:
                return String.format("%.2f'", decimalFeet);
            case FEET_INCHES:
                return formatFeetInches(decimalFeet);
            default:
                return String.format("%.2f'", decimalFeet);
        }
    }
    
    /**
     * Format a coordinate pair for display
     * 
     * @param x X-coordinate in decimal feet
     * @param y Y-coordinate in decimal feet
     * @return Formatted coordinate string
     */
    public static String formatCoordinates(double x, double y) {
        return String.format("Position: (%s, %s)", formatDimension(x), formatDimension(y));
    }
    
    /**
     * Convert decimal feet to feet and inches format
     * 
     * @param decimalFeet The dimension in decimal feet
     * @return Formatted string like "5' 6"" or "0' 3"" or "12' 0""
     */
    private static String formatFeetInches(double decimalFeet) {
        // Handle negative values
        boolean isNegative = decimalFeet < 0;
        double absoluteFeet = Math.abs(decimalFeet);
        
        // Extract whole feet
        int feet = (int) absoluteFeet;
        
        // Calculate remaining inches (12 inches per foot)
        double remainingFeet = absoluteFeet - feet;
        double totalInches = remainingFeet * 12.0;
        
        // Round to nearest 1/4 inch for practical construction accuracy
        int quarters = (int) Math.round(totalInches * 4.0);
        int inches = quarters / 4;
        int remainingQuarters = quarters % 4;
        
        // Handle case where rounding pushes us to the next foot
        if (inches >= 12) {
            feet += inches / 12;
            inches = inches % 12;
        }
        
        StringBuilder result = new StringBuilder();
        if (isNegative) {
            result.append("-");
        }
        
        result.append(feet).append("'");
        
        if (inches > 0 || remainingQuarters > 0) {
            result.append(" ");
            if (inches > 0) {
                result.append(inches);
            }
            
            // Add fractional inches if needed
            if (remainingQuarters > 0) {
                String fraction = formatFraction(remainingQuarters, 4);
                if (inches > 0) {
                    result.append(" ").append(fraction);
                } else {
                    result.append(fraction);
                }
            }
            result.append("\"");
        } else {
            result.append(" 0\"");
        }
        
        return result.toString();
    }
    
    /**
     * Format a fraction in simplest form
     * 
     * @param numerator The numerator
     * @param denominator The denominator
     * @return Simplified fraction string
     */
    private static String formatFraction(int numerator, int denominator) {
        // Find GCD to simplify fraction
        int gcd = gcd(numerator, denominator);
        int simpleNum = numerator / gcd;
        int simpleDen = denominator / gcd;
        
        if (simpleDen == 1) {
            return String.valueOf(simpleNum);
        } else {
            return simpleNum + "/" + simpleDen;
        }
    }
    
    /**
     * Calculate greatest common divisor
     * 
     * @param a First number
     * @param b Second number
     * @return GCD of a and b
     */
    private static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    /**
     * Get the current display format
     * 
     * @return Current display format
     */
    public static DisplayFormat getCurrentFormat() {
        return currentFormat;
    }
    
    /**
     * Set the display format
     * 
     * @param format New display format
     */
    public static void setDisplayFormat(DisplayFormat format) {
        currentFormat = format;
    }
}