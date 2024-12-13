package com.tomasulo.controller;

import com.tomasulo.model.RegisterFile;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import com.tomasulo.model.Memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationController {
    private TextField addLatency, subLatency, mulLatency, divLatency, loadLatency, storeLatency;
    private TextField intAddLatency, intSubLatency, intMulLatency, intDivLatency, intLoadLatency, intStoreLatency;
    private TextField branchLatency;
    private TextField addSubStations, mulDivStations, loadBuffers, storeBuffers;
    private TextField intAddSubStations, intMulDivStations, intLoadBuffers, intStoreBuffers;
    private TextField branchStation;
    private TextField cacheSize, blockSize, hitLatency, missLatency;
    private Label toaster;
    private TableView<MemoryEntry> memoryTable;
    private TableView<RegisterEntry> registerTable;
    private TableView<RegisterEntry> intRegisterTable;
    private ObservableList<MemoryEntry> memoryData;
    private ObservableList<RegisterEntry> registerData;
    private ObservableList<RegisterEntry> intRegisterData;
    private RegisterFile registerFile;
    private RegisterFile intRegisterFile;
    private Memory memory;
    public Map<String, Integer> operations;
    public Map<String, Integer> cacheParams;
    public Map<String, Integer> bufferSizes;

    public static class RegisterEntry {
        private final SimpleStringProperty register;
        private final SimpleStringProperty value;

        public RegisterEntry(String register, String value) {
            this.register = new SimpleStringProperty(register);
            this.value = new SimpleStringProperty(value);
        }

        public String getRegister() {
            return register.get();
        }

        public void setRegister(String register) {
            this.register.set(register);
        }

        public String getValue() {
            return value.get();
        }

        public void setValue(String value) {
            this.value.set(value);
        }

        public SimpleStringProperty registerProperty() {
            return register;
        }

        public SimpleStringProperty valueProperty() {
            return value;
        }
    }


    public static class MemoryEntry {
        private final SimpleIntegerProperty address;
        private final SimpleStringProperty hexValue;
        private final SimpleStringProperty ascii;
        private byte value;  // Store the actual byte value

        public MemoryEntry(int address, byte value) {
            this.address = new SimpleIntegerProperty(address);
            this.value = value;
            this.hexValue = new SimpleStringProperty(String.format("%02X", value & 0xFF));
            this.ascii = new SimpleStringProperty(isPrintable(value) ? String.valueOf((char) value) : ".");
        }

        private boolean isPrintable(byte b) {
            return b >= 32 && b <= 126;
        }

        public void setValue(byte value) {
            this.value = value;
            hexValue.set(String.format("%02X", value & 0xFF));
            ascii.set(isPrintable(value) ? String.valueOf((char) value) : ".");
        }

        public int getAddress() {
            return address.get();
        }

        public void setAddress(int address) {
            this.address.set(address);
        }

        public byte getValue() {
            return value;
        }

        public String getHexValue() {
            return hexValue.get();
        }

        public String getAscii() {
            return ascii.get();
        }

        public SimpleIntegerProperty addressProperty() {
            return address;
        }

        public SimpleStringProperty hexValueProperty() {
            return hexValue;
        }

        public SimpleStringProperty asciiProperty() {
            return ascii;
        }
    }

    public ConfigurationController() {
        operations = new HashMap<>();
        cacheParams = new HashMap<>();
        bufferSizes = new HashMap<>();
        memoryData = FXCollections.observableArrayList();
        registerData = FXCollections.observableArrayList();
        intRegisterData = FXCollections.observableArrayList();
        memory = new Memory(1024);
        registerFile = new RegisterFile(32, false);
        intRegisterFile = new RegisterFile(32, true);
    }

    private void setupRegisterTable(TableView<RegisterEntry> table, boolean isInteger) {
        table.setEditable(true);

        TableColumn<RegisterEntry, String> registerCol = new TableColumn<>(isInteger ? "Integer Register" : "Float Register");
        TableColumn<RegisterEntry, String> valueCol = new TableColumn<>("Value");

        registerCol.setCellValueFactory(cellData -> cellData.getValue().registerProperty());
        valueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());

        // Make value column editable
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(event -> {
            RegisterEntry entry = event.getRowValue();
            try {
                String newValue = event.getNewValue();
                if (isInteger) {
                    int intValue = Integer.parseInt(newValue);
                    // Add range validation if needed
                    if (intValue < Integer.MIN_VALUE || intValue > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("Value out of range for integer register");
                    }
                } else {
                    double doubleValue = Double.parseDouble(newValue);
                    // Add range validation if needed
                    if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
                        throw new IllegalArgumentException("Invalid floating point value");
                    }
                }
                entry.setValue(newValue);
                updateRegisterFile(entry.getRegister(), newValue, isInteger);
            } catch (NumberFormatException e) {
                table.refresh();
                showError("Invalid number format. Please enter a valid " +
                        (isInteger ? "integer" : "floating-point") + " number.");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });

        table.getColumns().addAll(registerCol, valueCol);
        table.setPrefHeight(200);

        // Initialize register entries
        ObservableList<RegisterEntry> data = isInteger ? intRegisterData : registerData;
        for (int i = 0; i < 32; i++) {
            String prefix = isInteger ? "R" : "F";
            String initialValue = isInteger ? "0" : "0.0";
            data.add(new RegisterEntry(prefix + i, initialValue));
        }
        table.setItems(data);
    }

    private void setupMemoryTable() {
        memoryTable = new TableView<>();
        memoryTable.setEditable(true);

        // Create columns
        TableColumn<MemoryEntry, Integer> addressCol = new TableColumn<>("Address");
        TableColumn<MemoryEntry, String> hexValueCol = new TableColumn<>("Value (Hex)");
        TableColumn<MemoryEntry, String> asciiCol = new TableColumn<>("ASCII");

        // Set cell factories
        addressCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        hexValueCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // Set cell value factories
        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty().asObject());
        hexValueCol.setCellValueFactory(cellData -> cellData.getValue().hexValueProperty());
        asciiCol.setCellValueFactory(cellData -> cellData.getValue().asciiProperty());

        // Set column edit handlers
        addressCol.setOnEditCommit(event -> {
            MemoryEntry entry = event.getRowValue();
            if (isValidAddress(event.getNewValue())) {
                entry.setAddress(event.getNewValue());
            } else {
                memoryTable.refresh();
                showError("Invalid address. Must be between 0 and 1023.");
            }
        });

        hexValueCol.setOnEditCommit(event -> {
            MemoryEntry entry = event.getRowValue();
            try {
                // Convert hex string to byte value
                int value = Integer.parseInt(event.getNewValue(), 16);
                if (isValidValue(value)) {
                    entry.setValue((byte) value);
                } else {
                    memoryTable.refresh();
                    showError("Invalid value. Must be between -128 and 127 for bytes.");
                }
            } catch (NumberFormatException e) {
                memoryTable.refresh();
                showError("Invalid hex value. Please enter a valid hexadecimal number.");
            }
        });

        memoryTable.getColumns().addAll(addressCol, hexValueCol, asciiCol);
        memoryTable.setItems(memoryData);
        memoryTable.setPrefHeight(200);
    }

    private boolean isValidAddress(int address) {
        return address >= 0 && address < 1024;
    }

    private boolean isValidValue(int value) {
        return value >= -128 && value <= 127; // Valid byte range
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox createConfigView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Create HBox for side-by-side layout
        HBox mainContent = new HBox(20);  // 20 pixels spacing between left and right sides

        // Left side - Configuration Fields
        VBox leftSide = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Instruction Operations
        Label instructionLabel = new Label("Instruction Operations");
        instructionLabel.setStyle("-fx-font-weight: bold");
        grid.add(instructionLabel, 0, 0, 2, 1);

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

        // Cache Parameters
        Label cacheLabel = new Label("Cache Parameters");
        cacheLabel.setStyle("-fx-font-weight: bold");
        grid.add(cacheLabel, 0, 7, 2, 1);

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
        Label bufferLabel = new Label("Buffer Sizes");
        bufferLabel.setStyle("-fx-font-weight: bold");
        grid.add(bufferLabel, 0, 12, 2, 1);

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

        Label integerLabel = new Label("Integer Operations");
        integerLabel.setStyle("-fx-font-weight: bold");
        grid.add(integerLabel, 2, 0, 2, 1);

        grid.add(new Label("INT ADD:"), 2, 1);
        intAddLatency = new TextField();
        grid.add(intAddLatency, 3, 1);

        grid.add(new Label("INT SUB:"), 2, 2);
        intSubLatency = new TextField();
        grid.add(intSubLatency, 3, 2);

        grid.add(new Label("INT MUL:"), 2, 3);
        intMulLatency = new TextField();
        grid.add(intMulLatency, 3, 3);

        grid.add(new Label("INT DIV:"), 2, 4);
        intDivLatency = new TextField();
        grid.add(intDivLatency, 3, 4);

        grid.add(new Label("BRANCH:"), 2, 7);
        branchLatency = new TextField();
        grid.add(branchLatency, 3, 7);

        // Integer Buffer Sizes
        grid.add(new Label("Int Add/Sub Stations:"), 2, 13);
        intAddSubStations = new TextField();
        grid.add(intAddSubStations, 3, 13);

        grid.add(new Label("Int Mul/Div Stations:"), 2, 14);
        intMulDivStations = new TextField();
        grid.add(intMulDivStations, 3, 14);

        grid.add(new Label("Int Load Buffers:"), 2, 15);
        intLoadBuffers = new TextField();
        grid.add(intLoadBuffers, 3, 15);

        grid.add(new Label("Int Store Buffers:"), 2, 16);
        intStoreBuffers = new TextField();
        grid.add(intStoreBuffers, 3, 16);

        grid.add(new Label("Branch Station:"), 2, 17);
        branchStation = new TextField();
        grid.add(branchStation, 3, 17);

        leftSide.getChildren().add(grid);

        // Right side - Memory Configuration
        VBox rightSide = new VBox(10);
        rightSide.setPadding(new Insets(0, 0, 0, 20)); // Add padding to separate from left side


        Label memoryLabel = new Label("Memory Configuration");
        memoryLabel.setStyle("-fx-font-weight: bold");
        setupMemoryTable();
        // Register Configuration
        Label registerLabel = new Label("Register Configuration");
        registerLabel.setStyle("-fx-font-weight: bold");
        registerTable = new TableView<>();
        setupRegisterTable(registerTable, false);
        // Integer Register Configuration
        Label intRegisterLabel = new Label("Integer Register Configuration");
        intRegisterLabel.setStyle("-fx-font-weight: bold");
        intRegisterTable = new TableView<>();
        setupRegisterTable(intRegisterTable, true);

        memoryTable.setPrefWidth(300);
        memoryTable.setMinWidth(250);

        memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Add memory control buttons
        HBox memoryControls = new HBox(10);
        HBox registerControls = createRegisterControls();
        memoryControls.setPadding(new Insets(10, 0, 0, 0));
        Button addRowButton = new Button("Add Row");
        Button removeRowButton = new Button("Remove Selected");
        Button clearMemoryButton = new Button("Clear Memory");
        addRowButton.setOnAction(e -> {
            memoryData.add(new MemoryEntry(0, (byte) 0));
        });
        removeRowButton.setOnAction(e -> {
            MemoryEntry selected = memoryTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                memoryData.remove(selected);
            }
        });
        clearMemoryButton.setOnAction(e -> {
            memoryData.clear();
        });
        memoryControls.getChildren().addAll(addRowButton, removeRowButton, clearMemoryButton);
        rightSide.getChildren().addAll(memoryLabel, memoryTable, memoryControls, registerLabel, registerTable, registerControls,
                intRegisterLabel, intRegisterTable);


        mainContent.getChildren().addAll(leftSide, rightSide);

        // Add spacing before the button box
        VBox.setMargin(mainContent, new Insets(0, 0, 10, 0));

        // Add main content to root
        root.getChildren().add(mainContent);

        // Save button at bottom
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleSave());
        toaster = new Label("");
        buttonBox.getChildren().addAll(saveButton, toaster);

        root.getChildren().add(buttonBox);

        return root;
    }

    public void setOperations(Map<String, Integer> operations) {
        this.operations = operations;
        addLatency.setText(operations.get("ADD.D").toString());
        addLatency.setText(operations.get("ADD.S").toString());
        subLatency.setText(operations.get("SUB.D").toString());
        subLatency.setText(operations.get("SUB.S").toString());
        mulLatency.setText(operations.get("MUL.D").toString());
        mulLatency.setText(operations.get("MUL.S").toString());
        divLatency.setText(operations.get("DIV.D").toString());
        divLatency.setText(operations.get("DIV.S").toString());
//        loadLatency.setText(operations.get("L.D").toString());
//        loadLatency.setText(operations.get("L.S").toString());
//        storeLatency.setText(operations.get("S.D").toString());
//        storeLatency.setText(operations.get("S.S").toString());
        intAddLatency.setText(operations.get("DADDI").toString());
        intSubLatency.setText(operations.get("DSUBI").toString());
//        intLoadLatency.setText(operations.get("LW").toString());
//        intLoadLatency.setText(operations.get("LD").toString());
//        intStoreLatency.setText(operations.get("SW").toString());
//        intStoreLatency.setText(operations.get("SD").toString());
        branchLatency.setText(operations.get("BEQ").toString());
        branchLatency.setText(operations.get("BNE").toString());
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
        intAddSubStations.setText(bufferSizes.get("intAddSub").toString());
        intMulDivStations.setText(bufferSizes.get("intMulDiv").toString());
        intLoadBuffers.setText(bufferSizes.get("intLoad").toString());
        intStoreBuffers.setText(bufferSizes.get("intStore").toString());
        branchStation.setText(bufferSizes.get("branch").toString());
    }

    private void handleSave() {
        updateMemory();
        updateRegisterFiles();
        TextField[] allFields = {addLatency, subLatency, mulLatency, divLatency,
                cacheSize, blockSize, hitLatency, missLatency,
                addSubStations, mulDivStations, loadBuffers, storeBuffers,
                intAddLatency, intSubLatency, intMulLatency, intDivLatency, branchLatency,
                intAddSubStations, intMulDivStations, intLoadBuffers, intStoreBuffers, branchStation
        };

        // Check if required fields are filled
        for (TextField field : allFields) {
            if (field.getText() == null || field.getText().isEmpty()) {
                toaster.setText("Please fill in all text fields!");
                toaster.setStyle("-fx-text-fill: red;");
                return;
            }
        }

        try {
            updateOperations();
            updateCacheParams();
            updateBufferSizes();

            // Print debug information
            System.out.println("Configuration saved:");
            System.out.println("Operations: " + operations);
            System.out.println("Cache parameters: " + cacheParams);
            System.out.println("Buffer sizes: " + bufferSizes);
            printMemoryState();
            printRegisterState();

            // Update toaster with success message
            toaster.setText("Configuration successfully saved!");
            toaster.setStyle("-fx-text-fill: green;");

            // Add visual feedback
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, evt -> toaster.setVisible(true)),
                    new KeyFrame(Duration.seconds(3), evt -> toaster.setVisible(false))
            );
            timeline.play();

        } catch (NumberFormatException e) {
            toaster.setText("Please enter valid numbers");
            toaster.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private HBox createRegisterControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));

        Button clearRegistersButton = new Button("Clear Registers");
        clearRegistersButton.setOnAction(e -> {
            registerData.clear();
            intRegisterData.clear();
            for (int i = 0; i < 32; i++) {
                registerData.add(new RegisterEntry("F" + i, "0.0"));
                intRegisterData.add(new RegisterEntry("R" + i, "0"));
            }
        });

        controls.getChildren().add(clearRegistersButton);
        return controls;
    }

    private void updateOperations() {
        operations.put("DADDI", Integer.parseInt(intAddLatency.getText()));
        operations.put("DSUBI", Integer.parseInt(intSubLatency.getText()));
        operations.put("ADD.D", Integer.parseInt(addLatency.getText()));
        operations.put("ADD.S", Integer.parseInt(addLatency.getText()));
        operations.put("SUB.D", Integer.parseInt(subLatency.getText()));
        operations.put("SUB.S", Integer.parseInt(subLatency.getText()));
        operations.put("MUL.D", Integer.parseInt(mulLatency.getText()));
        operations.put("MUL.S", Integer.parseInt(mulLatency.getText()));
        operations.put("DIV.D", Integer.parseInt(divLatency.getText()));
        operations.put("DIV.S", Integer.parseInt(divLatency.getText()));
        operations.put("BEQ", Integer.parseInt(branchLatency.getText()));
        operations.put("BNE", Integer.parseInt(branchLatency.getText()));
    }

    private void updateCacheParams() {
        cacheParams.put("size", Integer.parseInt(cacheSize.getText()));
        cacheParams.put("blockSize", Integer.parseInt(blockSize.getText()));
        cacheParams.put("hitLatency", Integer.parseInt(hitLatency.getText()));
        cacheParams.put("missLatency", Integer.parseInt(missLatency.getText()));
        if (cacheParams.get("size") % cacheParams.get("blockSize") != 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("The size of the cache is not divisible by the block size.\n" +
                    "So a part of the cache will be unused");
            alert.showAndWait();
        }
    }

    private void updateBufferSizes() {
        bufferSizes.put("addSub", Integer.parseInt(addSubStations.getText()));
        bufferSizes.put("mulDiv", Integer.parseInt(mulDivStations.getText()));
        bufferSizes.put("load", Integer.parseInt(loadBuffers.getText()));
        bufferSizes.put("store", Integer.parseInt(storeBuffers.getText()));
        bufferSizes.put("intAddSub", Integer.parseInt(intAddSubStations.getText()));
        bufferSizes.put("intMulDiv", Integer.parseInt(intMulDivStations.getText()));
        bufferSizes.put("intLoad", Integer.parseInt(intLoadBuffers.getText()));
        bufferSizes.put("intStore", Integer.parseInt(intStoreBuffers.getText()));
        bufferSizes.put("branch", Integer.parseInt(branchStation.getText()));
    }

    private void updateRegisterFiles() {
        for (RegisterEntry entry : registerData) {
            registerFile.setValue(entry.getRegister(), entry.getValue());
        }
        for (RegisterEntry entry : intRegisterData) {
            intRegisterFile.setValue(entry.getRegister(), entry.getValue());
        }
    }

    private void printRegisterState() {
        System.out.println("Float Register State:");
        for (RegisterEntry entry : registerData) {
            System.out.println(entry.getRegister() + ": " + entry.getValue());
        }
        System.out.println("Integer Register State:");
        for (RegisterEntry entry : intRegisterData) {
            System.out.println(entry.getRegister() + ": " + entry.getValue());
        }
    }

    private void updateRegisterFile(String register, String value, boolean isInteger) {
        if (isInteger) {
            intRegisterFile.setValue(register, value);
        } else {
            registerFile.setValue(register, value);
        }
    }

    private void updateMemory() {
        try {
            memory.clear();

            for (MemoryEntry entry : memoryData) {
                if (isValidAddress(entry.getAddress())) {
                    memory.writeByte(entry.getAddress(), entry.getValue());
                    System.out.println("Writing value " + entry.getValue() + " to address " + entry.getAddress());
                } else {
                    throw new IllegalArgumentException("Invalid memory configuration at address: " + entry.getAddress());
                }
            }

            System.out.println("Memory updated with " + memoryData.size() + " entries");

            // Verify the written values
            for (MemoryEntry entry : memoryData) {
                byte readValue = memory.readByte(entry.getAddress());
                System.out.println("Verification - Address: " + entry.getAddress() +
                        ", Value: " + readValue +
                        ", Hex: " + String.format("%02X", readValue & 0xFF));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error updating memory: " + e.getMessage());
        }
    }

    public void printMemoryState() {
        System.out.println("Current Memory State:");
        for (int i = 0; i < memory.getSize(); i++) {
            byte value = memory.readByte(i);
            if (value != 0) {
                System.out.println("Address " + i + ": " + value);
            }
        }
    }

    public Memory getMemory() {
        return memory;
    }

    public RegisterFile getRegisterFile() {
        return registerFile;
    }

    public RegisterFile getIntRegisterFile() {
        return intRegisterFile;
    }
}