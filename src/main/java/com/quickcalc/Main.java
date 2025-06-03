package com.quickcalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for QuickCalc
 * A cross-platform desktop application for structural beam analysis
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting QuickCalc application...");
        try {
            // Load the main view FXML
            System.out.println("Loading FXML file...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            // Get the controller and set the primary stage reference
            System.out.println("Getting controller...");
            com.quickcalc.controllers.MainController controller = loader.getController();
            System.out.println("Controller obtained: " + (controller != null ? "success" : "failed"));
            controller.setPrimaryStage(primaryStage);
            
            // Set up the primary stage
            primaryStage.setTitle("QuickCalc - Structural Beam Analysis");
            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // For now, if FXML fails to load, show a simple scene
            Scene fallbackScene = createFallbackScene();
            primaryStage.setTitle("QuickCalc - Structural Beam Analysis");
            primaryStage.setScene(fallbackScene);
            primaryStage.show();
        }
    }
    
    /**
     * Creates a fallback scene in case the FXML fails to load
     * This is temporary during initial development
     */
    private Scene createFallbackScene() {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(20);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(20));
        
        javafx.scene.control.Label label = new javafx.scene.control.Label("Welcome to QuickCalc");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        javafx.scene.control.Label subtitle = new javafx.scene.control.Label("Structural Beam Analysis Tool");
        subtitle.setStyle("-fx-font-size: 16px;");
        
        root.getChildren().addAll(label, subtitle);
        
        return new Scene(root, 1200, 800);
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
