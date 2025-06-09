package com.quickcalc.views.components.interaction.commands;

/**
 * Base interface for all commands using the Command pattern
 */
public interface Command {
    
    /**
     * Execute the command
     */
    void execute();
    
    /**
     * Undo the command (for future undo/redo functionality)
     */
    void undo();
    
    /**
     * Get a description of this command for logging/debugging
     */
    String getDescription();
}