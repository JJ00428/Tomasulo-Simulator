package com.tomasulo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Map;

public class ConfigurationController {
    @FXML private TextField addLatency;
    @FXML private TextField subLatency;
    @FXML private TextField mulLatency;
    @FXML private TextField divLatency;
    @FXML private TextField loadLatency;
    @FXML private TextField storeLatency;
    @FXML private TextField cacheSize;
    @FXML private TextField blockSize;
    @FXML private TextField hitLatency;
    @FXML private TextField missLatency;
    @FXML private TextField addSubStations;
    @FXML private TextField mulDivStations;
    @FXML private TextField loadBuffers;
    @FXML private TextField storeBuffers;

    private Map<String, Integer> latencies;
    private Map<String, Integer> cacheParams;
    private Map<String, Integer> bufferSizes;

    public void setLatencies(Map<String, Integer> latencies) {
        this.latencies = latencies;
        addLatency.setText(latencies.get("ADD").toString());
        subLatency.setText(latencies.get("SUB").toString());
        mulLatency.setText(latencies.get("MUL").toString());
        divLatency.setText(latencies.get("DIV").toString());
        loadLatency.setText(latencies.get("L.D").toString());
        storeLatency.setText(latencies.get("S.D").toString());
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

    @FXML
    private void handleSave() {
        updateLatencies();
        updateCacheParams();
        updateBufferSizes();
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void updateLatencies() {
        latencies.put("ADD", Integer.parseInt(addLatency.getText()));
        latencies.put("SUB", Integer.parseInt(subLatency.getText()));
        latencies.put("MUL", Integer.parseInt(mulLatency.getText()));
        latencies.put("DIV", Integer.parseInt(divLatency.getText()));
        latencies.put("L.D", Integer.parseInt(loadLatency.getText()));
        latencies.put("S.D", Integer.parseInt(storeLatency.getText()));
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
        Stage stage = (Stage) addLatency.getScene().getWindow();
        stage.close();
    }
}