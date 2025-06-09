package com.quickcalc.views.components.interaction.commands;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Support;

/**
 * Command to add a support to the beam
 */
public class AddSupportCommand implements Command {
    
    private final BeamModel beamModel;
    private final Support support;
    private final Runnable updateCallback;
    
    public AddSupportCommand(BeamModel beamModel, Support support, Runnable updateCallback) {
        this.beamModel = beamModel;
        this.support = support;
        this.updateCallback = updateCallback;
    }
    
    @Override
    public void execute() {
        beamModel.addSupport(support);
        if (updateCallback != null) {
            updateCallback.run();
        }
        System.out.println("Added support: " + support.getType() + " at " + support.getPosition());
    }
    
    @Override
    public void undo() {
        beamModel.removeSupport(support);
        if (updateCallback != null) {
            updateCallback.run();
        }
        System.out.println("Removed support: " + support.getType() + " at " + support.getPosition());
    }
    
    @Override
    public String getDescription() {
        return "Add " + support.getType() + " support at " + support.getPosition();
    }
}