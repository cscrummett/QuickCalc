package com.quickcalc.utils;

/**
 * Utility class for coordinate transformation between screen and engineering coordinates
 * Handles zooming, panning, and coordinate system conversion
 */
public class ViewTransform {
    
    private double scaleX;        // Scale factor for X-axis (pixels per engineering unit)
    private double scaleY;        // Scale factor for Y-axis (pixels per engineering unit)
    private double translateX;    // Translation for X-axis (pixels)
    private double translateY;    // Translation for Y-axis (pixels)
    private boolean flipY;        // Whether to flip the Y-axis (engineering Y+ up, screen Y+ down)
    
    /**
     * Constructor with default values
     */
    public ViewTransform() {
        this.scaleX = 20.0;       // 20 pixels per foot
        this.scaleY = 20.0;       // 20 pixels per foot
        this.translateX = 50.0;   // 50 pixels padding from left
        this.translateY = 300.0;  // 300 pixels from top (beam centerline)
        this.flipY = true;        // Flip Y-axis (engineering Y+ up, screen Y+ down)
    }
    
    /**
     * Constructor with specified values
     * 
     * @param scaleX Scale factor for X-axis
     * @param scaleY Scale factor for Y-axis
     * @param translateX Translation for X-axis
     * @param translateY Translation for Y-axis
     * @param flipY Whether to flip the Y-axis
     */
    public ViewTransform(double scaleX, double scaleY, double translateX, double translateY, boolean flipY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translateX = translateX;
        this.translateY = translateY;
        this.flipY = flipY;
    }
    
    /**
     * Convert engineering coordinates to screen coordinates
     * 
     * @param engineeringX X-coordinate in engineering units (feet)
     * @param engineeringY Y-coordinate in engineering units (feet)
     * @return Point in screen coordinates (pixels)
     */
    public Point2D engineeringToScreen(double engineeringX, double engineeringY) {
        double screenX = engineeringX * scaleX + translateX;
        double screenY;
        
        if (flipY) {
            screenY = translateY - engineeringY * scaleY;
        } else {
            screenY = engineeringY * scaleY + translateY;
        }
        
        return new Point2D(screenX, screenY);
    }
    
    /**
     * Convert engineering point to screen point
     * 
     * @param engineeringPoint Point in engineering coordinates
     * @return Point in screen coordinates
     */
    public Point2D engineeringToScreen(Point2D engineeringPoint) {
        return engineeringToScreen(engineeringPoint.getX(), engineeringPoint.getY());
    }
    
    /**
     * Convert screen coordinates to engineering coordinates
     * 
     * @param screenX X-coordinate in screen units (pixels)
     * @param screenY Y-coordinate in screen units (pixels)
     * @return Point in engineering coordinates (feet)
     */
    public Point2D screenToEngineering(double screenX, double screenY) {
        double engineeringX = (screenX - translateX) / scaleX;
        double engineeringY;
        
        if (flipY) {
            engineeringY = (translateY - screenY) / scaleY;
        } else {
            engineeringY = (screenY - translateY) / scaleY;
        }
        
        return new Point2D(engineeringX, engineeringY);
    }
    
    /**
     * Convert screen point to engineering point
     * 
     * @param screenPoint Point in screen coordinates
     * @return Point in engineering coordinates
     */
    public Point2D screenToEngineering(Point2D screenPoint) {
        return screenToEngineering(screenPoint.getX(), screenPoint.getY());
    }
    
    /**
     * Zoom the view centered on a specific point
     * 
     * @param centerX Center X-coordinate in screen coordinates
     * @param centerY Center Y-coordinate in screen coordinates
     * @param zoomFactor Zoom factor (> 1 for zoom in, < 1 for zoom out)
     */
    public void zoom(double centerX, double centerY, double zoomFactor) {
        // Convert center point to engineering coordinates before zoom
        Point2D centerEngineering = screenToEngineering(centerX, centerY);
        
        // Apply zoom factor to scales
        scaleX *= zoomFactor;
        scaleY *= zoomFactor;
        
        // Adjust translation to keep the center point fixed
        Point2D newCenterScreen = engineeringToScreen(centerEngineering);
        translateX += (centerX - newCenterScreen.getX());
        translateY += (centerY - newCenterScreen.getY());
    }
    
    /**
     * Pan the view
     * 
     * @param deltaX X-axis pan amount in screen coordinates
     * @param deltaY Y-axis pan amount in screen coordinates
     */
    public void pan(double deltaX, double deltaY) {
        translateX += deltaX;
        translateY += deltaY;
    }
    
    /**
     * Reset the transform to fit a beam of specified length
     * 
     * @param beamLength Beam length in feet
     * @param canvasWidth Canvas width in pixels
     * @param canvasHeight Canvas height in pixels
     * @param paddingX Horizontal padding in pixels
     * @param paddingY Vertical padding in pixels
     */
    public void fitBeam(double beamLength, double canvasWidth, double canvasHeight, double paddingX, double paddingY) {
        // Calculate scale to fit beam horizontally
        scaleX = (canvasWidth - 2 * paddingX) / beamLength;
        scaleY = scaleX; // Keep aspect ratio 1:1
        
        // Set translation to center beam
        translateX = paddingX;
        translateY = canvasHeight / 2;
    }
    
    // Getters and setters
    
    public double getScaleX() {
        return scaleX;
    }
    
    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }
    
    public double getScaleY() {
        return scaleY;
    }
    
    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }
    
    public double getTranslateX() {
        return translateX;
    }
    
    public void setTranslateX(double translateX) {
        this.translateX = translateX;
    }
    
    public double getTranslateY() {
        return translateY;
    }
    
    public void setTranslateY(double translateY) {
        this.translateY = translateY;
    }
    
    public boolean isFlipY() {
        return flipY;
    }
    
    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
}
