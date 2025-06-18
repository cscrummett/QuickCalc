package com.quickcalc.services;

import com.quickcalc.models.BeamModel;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

public class FileService {
    
    private final Stage primaryStage;
    private File currentFile;
    
    public FileService(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public Optional<File> showOpenDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Beam Project");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("QuickCalc Projects", "*.json")
        );
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        return Optional.ofNullable(selectedFile);
    }
    
    public Optional<File> showSaveDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Beam Project");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("QuickCalc Projects", "*.json")
        );
        
        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        return Optional.ofNullable(selectedFile);
    }
    
    public boolean save(BeamModel beamModel) {
        if (currentFile != null) {
            return saveToFile(beamModel, currentFile);
        } else {
            return saveAs(beamModel);
        }
    }
    
    public boolean saveAs(BeamModel beamModel) {
        Optional<File> file = showSaveDialog();
        if (file.isPresent()) {
            currentFile = file.get();
            return saveToFile(beamModel, currentFile);
        }
        return false;
    }
    
    public Optional<BeamModel> open() {
        Optional<File> file = showOpenDialog();
        if (file.isPresent()) {
            currentFile = file.get();
            return loadFromFile(currentFile);
        }
        return Optional.empty();
    }
    
    private boolean saveToFile(BeamModel beamModel, File file) {
        System.out.println("Save to file: " + file.getAbsolutePath());
        return true;
    }
    
    private Optional<BeamModel> loadFromFile(File file) {
        System.out.println("Load from file: " + file.getAbsolutePath());
        return Optional.empty();
    }
    
    public File getCurrentFile() {
        return currentFile;
    }
    
    public void setCurrentFile(File file) {
        this.currentFile = file;
    }
    
    public boolean hasCurrentFile() {
        return currentFile != null;
    }
    
    public void clearCurrentFile() {
        this.currentFile = null;
    }
}