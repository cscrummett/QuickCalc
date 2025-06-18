package com.quickcalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;

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
            
            // Get screen dimensions to size window appropriately
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();
            
            // Calculate window size as 80% of screen size, with reasonable limits
            double windowWidth = Math.min(Math.max(screenWidth * 0.8, 800), 1200);
            double windowHeight = Math.min(Math.max(screenHeight * 0.8, 600), 800);
            
            // Set up the primary stage with proper window decorations
            primaryStage.setTitle("QuickCalc - Structural Beam Analysis");
            primaryStage.initStyle(StageStyle.DECORATED); // Ensure window decorations are shown
            primaryStage.setResizable(true); // Allow window resizing
            primaryStage.setMaximized(false); // Start windowed, not maximized
            
            // Set minimum window size
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Center the window on screen
            primaryStage.setX((screenWidth - windowWidth) / 2);
            primaryStage.setY((screenHeight - windowHeight) / 2);
            
            primaryStage.setScene(new Scene(root, windowWidth, windowHeight));
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // For now, if FXML fails to load, show a simple scene
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();
            double windowWidth = Math.min(Math.max(screenWidth * 0.8, 800), 1200);
            double windowHeight = Math.min(Math.max(screenHeight * 0.8, 600), 800);
            
            Scene fallbackScene = createFallbackScene();
            primaryStage.setTitle("QuickCalc - Structural Beam Analysis");
            primaryStage.initStyle(StageStyle.DECORATED); // Ensure window decorations are shown
            primaryStage.setResizable(true); // Allow window resizing
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setX((screenWidth - windowWidth) / 2);
            primaryStage.setY((screenHeight - windowHeight) / 2);
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
