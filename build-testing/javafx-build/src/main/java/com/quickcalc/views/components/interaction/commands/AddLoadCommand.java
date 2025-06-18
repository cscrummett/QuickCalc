package com.quickcalc.views.components.interaction.commands;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;

/**
 * Command to add a load to the beam
 */
public class AddLoadCommand implements Command {
    
    private final BeamModel beamModel;
    private final Load load;
    private final Runnable updateCallback;
    
    public AddLoadCommand(BeamModel beamModel, Load load, Runnable updateCallback) {
        this.beamModel = beamModel;
        this.load = load;
        this.updateCallback = updateCallback;
    }
    
    @Override
    public void execute() {
        beamModel.addLoad(load);
        if (updateCallback != null) {
            updateCallback.run();
        }
        System.out.println("Added load: " + load.getType() + " " + load.getMagnitude() + " at " + load.getPosition());
    }
    
    @Override
    public void undo() {
        beamModel.removeLoad(load);
        if (updateCallback != null) {
            updateCallback.run();
        }
        System.out.println("Removed load: " + load.getType() + " " + load.getMagnitude() + " at " + load.getPosition());
    }
    
    @Override
    public String getDescription() {
        return "Add " + load.getType() + " load (" + load.getMagnitude() + ") at " + load.getPosition();
    }
}