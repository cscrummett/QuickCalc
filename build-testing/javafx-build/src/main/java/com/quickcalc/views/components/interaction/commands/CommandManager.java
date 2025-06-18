package com.quickcalc.views.components.interaction.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages command execution and provides undo/redo functionality
 */
public class CommandManager {
    
    private final List<Command> executedCommands = new ArrayList<>();
    private int currentCommandIndex = -1;
    
    /**
     * Execute a command and add it to the history
     */
    public void executeCommand(Command command) {
        // Remove any commands after the current position (redo history)
        while (executedCommands.size() > currentCommandIndex + 1) {
            executedCommands.remove(executedCommands.size() - 1);
        }
        
        // Execute the command
        command.execute();
        
        // Add to history
        executedCommands.add(command);
        currentCommandIndex++;
        
        System.out.println("Executed command: " + command.getDescription());
    }
    
    /**
     * Undo the last command
     */
    public boolean undo() {
        if (canUndo()) {
            Command command = executedCommands.get(currentCommandIndex);
            command.undo();
            currentCommandIndex--;
            System.out.println("Undid command: " + command.getDescription());
            return true;
        }
        return false;
    }
    
    /**
     * Redo the next command
     */
    public boolean redo() {
        if (canRedo()) {
            currentCommandIndex++;
            Command command = executedCommands.get(currentCommandIndex);
            command.execute();
            System.out.println("Redid command: " + command.getDescription());
            return true;
        }
        return false;
    }
    
    /**
     * Check if undo is possible
     */
    public boolean canUndo() {
        return currentCommandIndex >= 0;
    }
    
    /**
     * Check if redo is possible
     */
    public boolean canRedo() {
        return currentCommandIndex < executedCommands.size() - 1;
    }
    
    /**
     * Clear command history
     */
    public void clearHistory() {
        executedCommands.clear();
        currentCommandIndex = -1;
    }
    
    /**
     * Get command history for debugging
     */
    public List<String> getCommandHistory() {
        List<String> history = new ArrayList<>();
        for (int i = 0; i < executedCommands.size(); i++) {
            String prefix = (i == currentCommandIndex) ? "→ " : "  ";
            history.add(prefix + executedCommands.get(i).getDescription());
        }
        return history;
    }
}