package com.tomasulo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.tomasulo.controller.SimulationController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationController controller = new SimulationController();
        Scene scene = new Scene(controller.createView(), 1200, 800);
        primaryStage.setTitle("Tomasulo Algorithm Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}