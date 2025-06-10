package com.quickcalc.models;

/**
 * Observer interface for model changes
 * Observers are notified when the BeamModel state changes
 */
public interface ModelObserver {
    
    /**
     * Called when beam properties change (length, material, section)
     * @param model The updated beam model
     */
    void onBeamPropertiesChanged(BeamModel model);
    
    /**
     * Called when a support is added to the model
     * @param model The updated beam model
     * @param support The support that was added
     */
    void onSupportAdded(BeamModel model, Support support);
    
    /**
     * Called when a support is removed from the model
     * @param model The updated beam model
     * @param support The support that was removed
     */
    void onSupportRemoved(BeamModel model, Support support);
    
    /**
     * Called when a support is modified
     * @param model The updated beam model
     * @param support The support that was modified
     */
    void onSupportModified(BeamModel model, Support support);
    
    /**
     * Called when a load is added to the model
     * @param model The updated beam model
     * @param load The load that was added
     */
    void onLoadAdded(BeamModel model, Load load);
    
    /**
     * Called when a load is removed from the model
     * @param model The updated beam model
     * @param load The load that was removed
     */
    void onLoadRemoved(BeamModel model, Load load);
    
    /**
     * Called when a load is modified
     * @param model The updated beam model
     * @param load The load that was modified
     */
    void onLoadModified(BeamModel model, Load load);
    
    /**
     * Called when the entire model is reset
     * @param model The new beam model
     */
    void onModelReset(BeamModel model);
}