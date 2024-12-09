package com.tomasulo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.tomasulo.controller.SimulationController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationController controller = new SimulationController();
        primaryStage.setTitle("Tomasulo Algorithm Simulator");
        primaryStage.setScene(controller.getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}