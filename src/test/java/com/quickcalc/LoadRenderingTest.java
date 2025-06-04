package com.quickcalc;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;
import com.quickcalc.views.components.BeamCanvas;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test application to demonstrate beam load rendering
 * This is a standalone test and does not affect the main application
 */
public class LoadRenderingTest extends Application {

    private BeamModel beamModel;
    private BeamCanvas beamCanvas;

    @Override
    public void start(Stage primaryStage) {
        // Create the main layout
        BorderPane root = new BorderPane();
        
        // Create a container for the beam canvas
        VBox canvasContainer = new VBox();
        canvasContainer.setStyle("-fx-background-color: white;");
        canvasContainer.setFocusTraversable(true);
        
        // Initialize the beam model with a 20-foot beam
        beamModel = new BeamModel(20.0);
        
        // Create the beam canvas
        beamCanvas = new BeamCanvas(800, 400);
        beamCanvas.setBeamModel(beamModel);
        
        // Add the canvas to the container with growth constraints
        canvasContainer.getChildren().add(beamCanvas);
        VBox.setVgrow(beamCanvas, Priority.ALWAYS);
        beamCanvas.widthProperty().bind(canvasContainer.widthProperty());
        beamCanvas.heightProperty().bind(canvasContainer.heightProperty());
        
        // Create buttons for adding different types of loads
        Button addPointLoadBtn = new Button("Add Point Load");
        Button addDistributedLoadBtn = new Button("Add Distributed Load");
        Button addMomentBtn = new Button("Add Moment");
        Button addVaryingLoadBtn = new Button("Add Varying Load");
        Button resetBtn = new Button("Reset Beam");
        
        // Set up button actions
        addPointLoadBtn.setOnAction(e -> {
            beamModel.addLoad(new Load(10.0, -5.0, Load.Type.POINT));
            beamCanvas.draw();
        });
        
        addDistributedLoadBtn.setOnAction(e -> {
            beamModel.addLoad(new Load(5.0, 15.0, -2.0));
            beamCanvas.draw();
        });
        
        addMomentBtn.setOnAction(e -> {
            beamModel.addLoad(new Load(15.0, 10.0, Load.Type.MOMENT));
            beamCanvas.draw();
        });

        addVaryingLoadBtn.setOnAction(e -> {
            // Varying load (triangular in this case): 0 at 2ft to -4kip/ft at 12ft
            beamModel.addLoad(new Load(2.0, 12.0, 0.0, -4.0)); 
            beamCanvas.draw();
        });
        
        resetBtn.setOnAction(e -> {
            beamModel.reset(20.0);
            beamModel.setLength(20.0);
            // Add default supports
            beamModel.addSupport(new Support(0.0, Support.Type.PINNED));
            beamModel.addSupport(new Support(20.0, Support.Type.ROLLER));
            beamCanvas.draw();
        });
        
        // Create a button container
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addPointLoadBtn, addDistributedLoadBtn, addMomentBtn, addVaryingLoadBtn, resetBtn);
        buttonBox.setStyle("-fx-padding: 10;");
        
        // Add components to the root layout
        root.setCenter(canvasContainer);
        root.setBottom(buttonBox);
        
        // Create the scene
        Scene scene = new Scene(root, 800, 500);
        
        // Set up the stage
        primaryStage.setTitle("Beam Load Rendering Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Add default supports
        beamModel.addSupport(new Support(0.0, Support.Type.PINNED));
        beamModel.addSupport(new Support(20.0, Support.Type.ROLLER));
        
        // Initial draw
        beamCanvas.draw();
        
        // Request focus for key events
        canvasContainer.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
