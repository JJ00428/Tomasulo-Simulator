package com.tomasulo.controller;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.Map;

public class ConfigurationController {
    private TextField addLatency, subLatency, mulLatency, divLatency, loadLatency, storeLatency;
    private TextField cacheSize, blockSize, hitLatency, missLatency;
    private TextField addSubStations, mulDivStations, loadBuffers, storeBuffers;

    private Map<String, Integer> operations;
    private Map<String, Integer> cacheParams;
    private Map<String, Integer> bufferSizes;

    public VBox createConfigView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Instruction Operations
        grid.add(new Label("Instruction Operations"), 0, 0, 2, 1);
        grid.add(new Label("ADD:"), 0, 1);
        addLatency = new TextField();
        grid.add(addLatency, 1, 1);

        grid.add(new Label("SUB:"), 0, 2);
        subLatency = new TextField();
        grid.add(subLatency, 1, 2);

        grid.add(new Label("MUL:"), 0, 3);
        mulLatency = new TextField();
        grid.add(mulLatency, 1, 3);

        grid.add(new Label("DIV:"), 0, 4);
        divLatency = new TextField();
        grid.add(divLatency, 1, 4);

        grid.add(new Label("LOAD:"), 0, 5);
        loadLatency = new TextField();
        grid.add(loadLatency, 1, 5);

        grid.add(new Label("STORE:"), 0, 6);
        storeLatency = new TextField();
        grid.add(storeLatency, 1, 6);

        // Cache Parameters
        grid.add(new Label("Cache Parameters"), 0, 7, 2, 1);
        grid.add(new Label("Cache Size:"), 0, 8);
        cacheSize = new TextField();
        grid.add(cacheSize, 1, 8);

        grid.add(new Label("Block Size:"), 0, 9);
        blockSize = new TextField();
        grid.add(blockSize, 1, 9);

        grid.add(new Label("Hit Latency:"), 0, 10);
        hitLatency = new TextField();
        grid.add(hitLatency, 1, 10);

        grid.add(new Label("Miss Latency:"), 0, 11);
        missLatency = new TextField();
        grid.add(missLatency, 1, 11);

        // Buffer Sizes
        grid.add(new Label("Buffer Sizes"), 0, 12, 2, 1);
        grid.add(new Label("Add/Sub Stations:"), 0, 13);
        addSubStations = new TextField();
        grid.add(addSubStations, 1, 13);

        grid.add(new Label("Mul/Div Stations:"), 0, 14);
        mulDivStations = new TextField();
        grid.add(mulDivStations, 1, 14);

        grid.add(new Label("Load Buffers:"), 0, 15);
        loadBuffers = new TextField();
        grid.add(loadBuffers, 1, 15);

        grid.add(new Label("Store Buffers:"), 0, 16);
        storeBuffers = new TextField();
        grid.add(storeBuffers, 1, 16);

        root.getChildren().add(grid);

        // Buttons
        HBox buttonBox = new HBox(10);
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleSave());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> handleCancel());
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        root.getChildren().add(buttonBox);

        return root;
    }

    public void setOperations(Map<String, Integer> operations) {
        this.operations = operations;
        addLatency.setText(operations.get("ADD").toString());
        subLatency.setText(operations.get("SUB").toString());
        mulLatency.setText(operations.get("MUL").toString());
        divLatency.setText(operations.get("DIV").toString());
        loadLatency.setText(operations.get("L.D").toString());
        storeLatency.setText(operations.get("S.D").toString());
    }

    public void setCacheParams(Map<String, Integer> cacheParams) {
        this.cacheParams = cacheParams;
        cacheSize.setText(cacheParams.get("size").toString());
        blockSize.setText(cacheParams.get("blockSize").toString());
        hitLatency.setText(cacheParams.get("hitLatency").toString());
        missLatency.setText(cacheParams.get("missLatency").toString());
    }

    public void setBufferSizes(Map<String, Integer> bufferSizes) {
        this.bufferSizes = bufferSizes;
        addSubStations.setText(bufferSizes.get("addSub").toString());
        mulDivStations.setText(bufferSizes.get("mulDiv").toString());
        loadBuffers.setText(bufferSizes.get("load").toString());
        storeBuffers.setText(bufferSizes.get("store").toString());
    }

    private void handleSave() {
        updateOperations();
        updateCacheParams();
        updateBufferSizes();
        closeDialog();
    }

    private void handleCancel() {
        closeDialog();
    }

    private void updateOperations() {
        operations.put("ADD", Integer.parseInt(addLatency.getText()));
        operations.put("SUB", Integer.parseInt(subLatency.getText()));
        operations.put("MUL", Integer.parseInt(mulLatency.getText()));
        operations.put("DIV", Integer.parseInt(divLatency.getText()));
        operations.put("L.D", Integer.parseInt(loadLatency.getText()));
        operations.put("S.D", Integer.parseInt(storeLatency.getText()));
    }

    private void updateCacheParams() {
        cacheParams.put("size", Integer.parseInt(cacheSize.getText()));
        cacheParams.put("blockSize", Integer.parseInt(blockSize.getText()));
        cacheParams.put("hitLatency", Integer.parseInt(hitLatency.getText()));
        cacheParams.put("missLatency", Integer.parseInt(missLatency.getText()));
    }

    private void updateBufferSizes() {
        bufferSizes.put("addSub", Integer.parseInt(addSubStations.getText()));
        bufferSizes.put("mulDiv", Integer.parseInt(mulDivStations.getText()));
        bufferSizes.put("load", Integer.parseInt(loadBuffers.getText()));
        bufferSizes.put("store", Integer.parseInt(storeBuffers.getText()));
    }

    private void closeDialog() {
        addLatency.getScene().getWindow().hide();
    }
}