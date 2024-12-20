package com.tomasulo.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.scene.Scene;

import com.tomasulo.model.*;

import java.util.*;

import static java.lang.Float.parseFloat;

public class SimulationController {
    private Scene scene;
    private BorderPane root;
    private VBox configView;
    private BorderPane simView;
    private final ConfigurationController config = new ConfigurationController();
    private TableView<InstructionEntry> instructionTable;
    private TableView<ReservationStation> addSubTable;
    private TableView<ReservationStation> mulDivTable;
    private TableView<LoadBuffer> loadBufferTable;
    private TableView<StoreBuffer> storeBufferTable;
    private TableView<ReservationStation> intAddSubTable; // Integer Add/Sub table
    private TableView<ReservationStation> intMulDivTable; // Integer Mul/Div table
    private TableView<LoadBuffer> intLoadBufferTable; // Integer Load buffer table
    private TableView<StoreBuffer> intStoreBufferTable; // Integer Store buffer table
    private GridPane registerFileGrid;
    private GridPane intRegisterFileGrid; // Integer register file grid
    private TableView<BranchStation> branchTable;
    private RegisterFile<Integer> intRegisterFile;
    private RegisterFile<Double> registerFile;
    private Label cycleLabel;
    private TextArea codeInput;
    private int currentCycle = 0;
    private int currentInstruction = 0;
    private int branchCurrentInstruction;
    private List<InstructionEntry> instructions = new ArrayList<>();
    private Map<String, Integer> operations = new HashMap<>();
    private List<ReservationStation> addSubStations = new ArrayList<>();
    private List<ReservationStation> mulDivStations = new ArrayList<>();
    private List<LoadBuffer> loadBuffers = new ArrayList<>();
    private List<StoreBuffer> storeBuffers = new ArrayList<>();
    private List<ReservationStation> intAddSubStations = new ArrayList<>(); // Integer Add/Sub stations
    private List<ReservationStation> intMulDivStations = new ArrayList<>(); // Integer Mul/Div stations
    private List<LoadBuffer> intLoadBuffers = new ArrayList<>(); // Integer Load buffers
    private List<StoreBuffer> intStoreBuffers = new ArrayList<>(); // Integer Store buffers
    private List<BranchStation> branchStations = new ArrayList<>(); // Branch stations
    private Map<String, Integer> cacheParams = new HashMap<>();
    private Map<String, Integer> bufferSizes = new HashMap<>();
    private int loop;
    private Memory memory;
    private TableView<Memory.MemoryEntry> memoryTable;
    private Cache cache;
    private TableView<Cache.CacheEntry> cacheTable;
    private int instructionCount = 0;
    private int loopStartIndex = -1;
    private int loopEndIndex = -1;
    private boolean lastBranchTaken = false;
    Button stepButton;

    public SimulationController() {
        root = new BorderPane();
        Button switcher = new Button("Switch View");
        switcher.setOnAction(e -> switchView());
        configView = config.createConfigView();
        root.setTop(switcher);
        root.setCenter(configView);
        scene = new Scene(root, 1200, 800);
    }

    public Scene getScene() {
        return scene;
    }

    public void switchView() {
        if (root.getCenter() == configView) {
            memory = config.getMemory();
            registerFile = config.getRegisterFile();
            intRegisterFile = config.getIntRegisterFile();
            //print all the memory
//            memory.getMemoryEntries().forEach(entry -> {
//                System.out.println("HERE"+" Address: " + entry.getAddress() + ", Value: " + entry.getHexValue());
//            });
            printRegisterState();
            simView = this.createView();
            root.setCenter(simView);
        } else root.setCenter(configView);
    }
    private void printRegisterState() {
        System.out.println("Float Register State:");
        for (int i = 0; i < 32; i++) {
            System.out.println("F" + i + ": " + registerFile.getValue("F" + i));
        }
        System.out.println("Integer Register State:");
        for (int i = 0; i < 32; i++) {
            System.out.println("R" + i + ": " + intRegisterFile.getValue("R" + i));
        }
    }

    public BorderPane createView() {
        BorderPane root = new BorderPane();

        // Top
        HBox topBox = new HBox(10);
        cycleLabel = new Label("Cycle 0");
        cycleLabel.setStyle("-fx-font-size: 18px;");
        stepButton = new Button("Step");
        stepButton.setOnAction(e -> handleStep());
//        Button resetButton = new Button("Reset");
//        resetButton.setOnAction(e -> handleReset());
        topBox.getChildren().addAll(cycleLabel, stepButton);
        root.setTop(topBox);

        // Left
        VBox leftBox = new VBox(10);
        codeInput = new TextArea();
        codeInput.setPrefRowCount(10);
        codeInput.setPrefColumnCount(30);
        Button loadButton = new Button("Load Instructions");
        loadButton.setOnAction(e -> handleLoadInstructions());
        leftBox.getChildren().addAll(codeInput, loadButton);
        root.setLeft(leftBox);

        // Center
        VBox centerBox = new VBox(10);
        instructionTable = new TableView<>();
        setupInstructionTable();

        HBox stationsBox = new HBox(10);
        VBox addSubBox = new VBox(5);
        addSubTable = new TableView<>();
        setupReservationStationTable(addSubTable);
        addSubBox.getChildren().addAll(new Label("Add/Sub Reservation Stations"), addSubTable);

        VBox mulDivBox = new VBox(5);
        mulDivTable = new TableView<>();
        setupReservationStationTable(mulDivTable);
        mulDivBox.getChildren().addAll(new Label("Mul/Div Reservation Stations"), mulDivTable);

        stationsBox.getChildren().addAll(addSubBox, mulDivBox);


        VBox branchBox = new VBox(5);
        branchTable = new TableView<>();
        setupBranchStationTable(branchTable);
        branchBox.getChildren().addAll(new Label("Branch Stations"), branchTable);

        // Add branchBox to your layout
        stationsBox.getChildren().add(branchBox);

        HBox buffersBox = new HBox(10);
        VBox loadBox = new VBox(5);
        loadBufferTable = new TableView<>();
        setupLoadBufferTable(loadBufferTable);
        loadBox.getChildren().addAll(new Label("Load Buffers"), loadBufferTable);

        VBox storeBox = new VBox(5);
        storeBufferTable = new TableView<>();
        setupStoreBufferTable(storeBufferTable);
        storeBox.getChildren().addAll(new Label("Store Buffers"), storeBufferTable);

        buffersBox.getChildren().addAll(loadBox, storeBox);

        centerBox.getChildren().addAll(instructionTable, stationsBox, buffersBox);

        // Integer-specific tables
        HBox intStationsBox = new HBox(10);
        VBox intAddSubBox = new VBox(5);
        intAddSubTable = new TableView<>();
        setupReservationStationTable(intAddSubTable);
        intAddSubBox.getChildren().addAll(new Label("Integer Add/Sub Reservation Stations"), intAddSubTable);

        VBox intMulDivBox = new VBox(5);
        intMulDivTable = new TableView<>();
        setupReservationStationTable(intMulDivTable);
        intMulDivBox.getChildren().addAll(new Label("Integer Mul/Div Reservation Stations"), intMulDivTable);

        intStationsBox.getChildren().addAll(intAddSubBox, intMulDivBox);

        HBox intBuffersBox = new HBox(10);
        VBox intLoadBox = new VBox(5);
        intLoadBufferTable = new TableView<>();
        setupLoadBufferTable(intLoadBufferTable);
        intLoadBox.getChildren().addAll(new Label("Integer Load Buffers"), intLoadBufferTable);

        VBox intStoreBox = new VBox(5);
        intStoreBufferTable = new TableView<>();
        setupStoreBufferTable(intStoreBufferTable);
        intStoreBox.getChildren().addAll(new Label("Integer Store Buffers"), intStoreBufferTable);

        intBuffersBox.getChildren().addAll(intLoadBox, intStoreBox);

        centerBox.getChildren().addAll(intStationsBox, intBuffersBox);
        root.setCenter(centerBox);

        // Right
        VBox rightBox = new VBox(5);
//        registerFileGrid = new GridPane();
//        registerFileGrid.setHgap(5);
//        registerFileGrid.setVgap(5);
//
//        // Integer register file grid
//        intRegisterFileGrid = new GridPane();
//        intRegisterFileGrid.setHgap(5);
//        intRegisterFileGrid.setVgap(5);


        setupRegisterFile(rightBox);

//        rightBox.getChildren().addAll(new Label("Register File"), registerFileGrid, new Label("Integer Register File"), intRegisterFileGrid);
        root.setRight(rightBox);

        setupInitialValues();
        setupMemoryTable(leftBox);
        setupCacheTable(leftBox);

        return root;
    }
    //------------------------All Setups--------------------------------
    private void handleLoadInstructions() {
        String[] lines = codeInput.getText().split("\n");
        instructions.clear();

        // First pass: load instructions and look for loop labels
        String loopStartLabel = null;
        String loopEndLabel = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            instructions.add(new InstructionEntry(line, loop, line));

            if (line.contains(":") && !line.startsWith("BEQ") && !line.startsWith("BNE")) {
                loopStartLabel = line.split(":")[0].trim();
            }

            if ((line.startsWith("BEQ") || line.startsWith("BNE")) && loopStartLabel != null) {
                String[] parts = line.split(" ");
                if (parts.length > 3 && parts[3].equals(loopStartLabel)) {
                    loopEndLabel = loopStartLabel;
                    System.out.println("Loop detected: " + loopStartLabel);
                    // Mark the loop boundaries
                    markLoopInstructions(findLabelIndex(loopStartLabel), i);
                }
            }
        }

        instructionCount = instructions.size();
        currentInstruction = 0;

        System.out.println("Instructions loaded: " + instructionCount);

        // Notification
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instructions Loaded");
        alert.setHeaderText(null);
        alert.setContentText("Instructions have been successfully loaded." +
                (loopEndLabel != null ? " Loop detected." : ""));
        alert.showAndWait();
    }
    private void setupInstructionTable() {
        TableColumn<InstructionEntry, Integer> iterationCol = new TableColumn<>("Iteration #");
        TableColumn<InstructionEntry, String> instructionCol = new TableColumn<>("Instruction");
        TableColumn<InstructionEntry, String> codeCol = new TableColumn<>("Code"); // New column for code
        TableColumn<InstructionEntry, Integer> issueCol = new TableColumn<>("Issue");
        TableColumn<InstructionEntry, Integer> executeCol = new TableColumn<>("Execute");
        TableColumn<InstructionEntry, Integer> writeCol = new TableColumn<>("Write Result");

        iterationCol.setCellValueFactory(cellData -> cellData.getValue().iterationProperty().asObject());
        instructionCol.setCellValueFactory(cellData -> cellData.getValue().instructionProperty());
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode())); // Set code column
        issueCol.setCellValueFactory(cellData -> cellData.getValue().issueTimeProperty().asObject());
        executeCol.setCellValueFactory(cellData -> cellData.getValue().executeTimeProperty().asObject());
        writeCol.setCellValueFactory(cellData -> cellData.getValue().writeTimeProperty().asObject());

        instructionTable.getColumns().addAll(iterationCol, instructionCol, codeCol, issueCol, executeCol, writeCol);
    }
    private void setupReservationStationTable(TableView<ReservationStation> table) {
        TableColumn<ReservationStation, String> nameCol = new TableColumn<>("Name");
        TableColumn<ReservationStation, Boolean> busyCol = new TableColumn<>("Busy");
        TableColumn<ReservationStation, String> opCol = new TableColumn<>("Op");
        TableColumn<ReservationStation, String> vjCol = new TableColumn<>("Vj");
        TableColumn<ReservationStation, String> vkCol = new TableColumn<>("Vk");
        TableColumn<ReservationStation, String> qjCol = new TableColumn<>("Qj");
        TableColumn<ReservationStation, String> qkCol = new TableColumn<>("Qk");
        TableColumn<ReservationStation, Integer> cyclesCol = new TableColumn<>("Cycles");

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        busyCol.setCellValueFactory(cellData -> cellData.getValue().busyProperty());
        opCol.setCellValueFactory(cellData -> cellData.getValue().operationProperty());
        vjCol.setCellValueFactory(cellData -> cellData.getValue().vjProperty());
        vkCol.setCellValueFactory(cellData -> cellData.getValue().vkProperty());
        qjCol.setCellValueFactory(cellData -> cellData.getValue().qjProperty());
        qkCol.setCellValueFactory(cellData -> cellData.getValue().qkProperty());
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty().asObject());

        table.getColumns().addAll(nameCol, busyCol, opCol, vjCol, vkCol, qjCol, qkCol, cyclesCol);
    }
    private void setupBranchStationTable(TableView<BranchStation> table) {
        TableColumn<BranchStation, String> nameCol = new TableColumn<>("Name");
        TableColumn<BranchStation, Boolean> busyCol = new TableColumn<>("Busy");
        TableColumn<BranchStation, String> opCol = new TableColumn<>("Op");
        TableColumn<BranchStation, String> vjCol = new TableColumn<>("Vj");
        TableColumn<BranchStation, String> vkCol = new TableColumn<>("Vk");
        TableColumn<BranchStation, String> qjCol = new TableColumn<>("Qj");
        TableColumn<BranchStation, String> qkCol = new TableColumn<>("Qk");
        TableColumn<BranchStation, Integer> cyclesCol = new TableColumn<>("Cycles");

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        busyCol.setCellValueFactory(cellData -> cellData.getValue().busyProperty());
        opCol.setCellValueFactory(cellData -> cellData.getValue().operationProperty());
        vjCol.setCellValueFactory(cellData -> cellData.getValue().vjProperty());
        vkCol.setCellValueFactory(cellData -> cellData.getValue().vkProperty());
        qjCol.setCellValueFactory(cellData -> cellData.getValue().qjProperty());
        qkCol.setCellValueFactory(cellData -> cellData.getValue().qkProperty());
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty().asObject());

        table.getColumns().addAll(nameCol, busyCol, opCol, vjCol, vkCol, qjCol, qkCol, cyclesCol);
    }
    private void setupLoadBufferTable(TableView<LoadBuffer> table) {
        TableColumn<LoadBuffer, String> nameCol = new TableColumn<>("Name");
        TableColumn<LoadBuffer, Boolean> busyCol = new TableColumn<>("Busy");
        TableColumn<LoadBuffer, Integer> addressCol = new TableColumn<>("Address");

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        busyCol.setCellValueFactory(cellData -> cellData.getValue().busyProperty());
        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty().asObject());

        table.getColumns().addAll(nameCol, busyCol, addressCol);
    }
    private void setupStoreBufferTable(TableView<StoreBuffer> table) {
        TableColumn<StoreBuffer, String> nameCol = new TableColumn<>("Name");
        TableColumn<StoreBuffer, Boolean> busyCol = new TableColumn<>("Busy");
        TableColumn<StoreBuffer, Integer> addressCol = new TableColumn<>("Address");
        TableColumn<StoreBuffer, Double> valueCol = new TableColumn<>("Value");
        TableColumn<StoreBuffer, String> qCol = new TableColumn<>("Q");

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        busyCol.setCellValueFactory(cellData -> cellData.getValue().busyProperty());
        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty().asObject());
        valueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty().asObject());
        qCol.setCellValueFactory(cellData -> cellData.getValue().qProperty());

        table.getColumns().addAll(nameCol, busyCol, addressCol, valueCol, qCol);
    }
    private void setupMemoryTable(VBox leftBox) {
        memoryTable = new TableView<>();

        // Disable table resizing
        memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Memory.MemoryEntry, Integer> addressCol = new TableColumn<>("Address");
        TableColumn<Memory.MemoryEntry, String> hexValueCol = new TableColumn<>("Value (Hex)");
        TableColumn<Memory.MemoryEntry, String> asciiCol = new TableColumn<>("ASCII");

        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty().asObject());
        hexValueCol.setCellValueFactory(cellData -> cellData.getValue().hexValueProperty());
        asciiCol.setCellValueFactory(cellData -> cellData.getValue().asciiProperty());

        // Set column widths
        addressCol.setPrefWidth(30);
        hexValueCol.setPrefWidth(40);
        asciiCol.setPrefWidth(50);

        // Set the table's preferred width to match columns
        memoryTable.setPrefWidth(120); // Sum of column widths

        memoryTable.getColumns().addAll(addressCol, hexValueCol, asciiCol);
        // print mem
        memory.getMemoryEntries().forEach(entry -> {
            System.out.println("HERE2"+" Address: " + entry.getAddress() + ", Value: " + entry.getHexValue());
        });
        memoryTable.setItems(memory.getMemoryEntries());

        memoryTable.setFixedCellSize(20);
        memoryTable.setPrefHeight(200);

        ScrollPane memoryScroll = new ScrollPane(memoryTable);
        memoryScroll.setFitToWidth(true);
        memoryScroll.setPrefViewportHeight(200);

        VBox memoryBox = new VBox(5);
        memoryBox.setPrefWidth(120);
        Label memoryLabel = new Label("Memory");
        memoryLabel.setStyle("-fx-font-weight: bold;");
        memoryBox.getChildren().addAll(memoryLabel, memoryScroll);

        leftBox.getChildren().add(memoryBox);
    }
    private void setupCacheTable(VBox leftBox) {
        cacheTable = new TableView<>();

        // Disable table resizing
        cacheTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Cache.CacheEntry, Integer> blockIndexCol = new TableColumn<>("blockIndex");
        TableColumn<Cache.CacheEntry, String> validCol = new TableColumn<>("valid");
        TableColumn<Cache.CacheEntry, String> tagCol = new TableColumn<>("Tag");
        TableColumn<Cache.CacheEntry, String> hexValuesCol = new TableColumn<>("values (Hex)");

        blockIndexCol.setCellValueFactory(cellData -> cellData.getValue().blockIndexProperty().asObject());
        validCol.setCellValueFactory(cellData -> cellData.getValue().validProperty().asObject().asString());
        tagCol.setCellValueFactory(cellData -> cellData.getValue().tagProperty().asObject().asString());
        hexValuesCol.setCellValueFactory(cellData -> cellData.getValue().hexValuesProperty());

        // Set column widths
        blockIndexCol.setPrefWidth(30);
        validCol.setPrefWidth(30);
        tagCol.setPrefWidth(30);
        hexValuesCol.setPrefWidth(50);

        // Set the table's preferred width to match columns
        cacheTable.setPrefWidth(120); // Sum of column widths

        cacheTable.getColumns().addAll(blockIndexCol, validCol, tagCol, hexValuesCol);
        cacheTable.setItems(cache.getCacheEntries());

        cacheTable.setFixedCellSize(20);
        cacheTable.setPrefHeight(200);

        ScrollPane cacheScroll = new ScrollPane(cacheTable);
        cacheScroll.setFitToWidth(true);
        cacheScroll.setPrefViewportHeight(200);

        VBox cacheBox = new VBox(5);
        cacheBox.setPrefWidth(120);
        Label cacheLabel = new Label("Cache");
        cacheLabel.setStyle("-fx-font-weight: bold;");
        cacheBox.getChildren().addAll(cacheLabel, cacheScroll);

        leftBox.getChildren().add(cacheBox);
    }
    private void setupRegisterFile(VBox rightBox) {
        registerFileGrid = new GridPane();
        registerFileGrid.setHgap(5);
        registerFileGrid.setVgap(5);

        // Integer register file grid
        intRegisterFileGrid = new GridPane();
        intRegisterFileGrid.setHgap(5);
        intRegisterFileGrid.setVgap(5);

        // Floating-point register file
        VBox registerBox = new VBox(5);
        registerBox.setPrefWidth(120);
        Label registerLabel = new Label("Register File");
        registerLabel.setStyle("-fx-font-weight: bold;");
        ScrollPane registerScroll = new ScrollPane(registerFileGrid);
        registerScroll.setFitToWidth(true);
        registerScroll.setPrefViewportHeight(200);
        registerBox.getChildren().addAll(registerLabel, registerScroll);

        // Integer register file
        VBox intRegisterBox = new VBox(5);
        intRegisterBox.setPrefWidth(120);
        Label intRegisterLabel = new Label("Integer Register File");
        intRegisterLabel.setStyle("-fx-font-weight: bold;");
        ScrollPane intRegisterScroll = new ScrollPane(intRegisterFileGrid);
        intRegisterScroll.setFitToWidth(true);
        intRegisterScroll.setPrefViewportHeight(200);
        intRegisterBox.getChildren().addAll(intRegisterLabel, intRegisterScroll);

        rightBox.getChildren().addAll(registerBox, intRegisterBox);
    }




    private void setupInitialValues() {
        loop = 1;
        // Set up default operations
        operations.put("DADDI", 2);
        operations.put("DSUBI", 1);
        operations.put("ADD.D", 2);
        operations.put("ADD.S", 2);
        operations.put("SUB.D", 2);
        operations.put("SUB.S", 2);
        operations.put("MUL.D", 10);
        operations.put("MUL.S", 10);
        operations.put("DIV.D", 15);
        operations.put("DIV.S", 15);
        operations.put("BEQ", 2);
        operations.put("BNE", 2);
        if (!config.operations.isEmpty()) operations = config.operations;
//        for (Map.Entry<String, Integer> entry : operations.entrySet()) {
//            System.out.println("Instruction: " + entry.getKey() + " " + entry.getValue());
//        }

        // Initialize cache
        if (config.cacheParams.isEmpty()) {
            cache = new Cache(32, 4, 1, 10, memory);
        } else {
            cache = new Cache(
                    config.cacheParams.get("size"),
                    config.cacheParams.get("blockSize"),
                    config.cacheParams.get("hitLatency"),
                    config.cacheParams.get("missLatency"),
                    memory
            );
        }
        // Initialize memory

        if (memory == null) {
            memory = new Memory(1024); // 1024 bytes of memory
        }

        // Initialize register files
        registerFile = config.getRegisterFile();
        intRegisterFile = config.getIntRegisterFile();

        addSubStations.clear();
        mulDivStations.clear();
        loadBuffers.clear();
        storeBuffers.clear();
        intAddSubStations.clear();
        intMulDivStations.clear();
        intLoadBuffers.clear();
        intStoreBuffers.clear();
        branchStations.clear();

        int numAddSub = config.bufferSizes.getOrDefault("addSub", 3);
        int numMulDiv = config.bufferSizes.getOrDefault("mulDiv", 3);
        int numLoad = config.bufferSizes.getOrDefault("load", 3);
        int numStore = config.bufferSizes.getOrDefault("store", 3);
        int numIntAddSub = config.bufferSizes.getOrDefault("intAddSub", 2);
        int numIntMulDiv = config.bufferSizes.getOrDefault("intMulDiv", 2);
        int numIntLoad = config.bufferSizes.getOrDefault("intLoad", 2);
        int numIntStore = config.bufferSizes.getOrDefault("intStore", 2);
        int numBranch = config.bufferSizes.getOrDefault("branch", 1);

        // Initialize floating-point reservation stations
        for (int i = 0; i < numAddSub; i++) {
            addSubStations.add(new ReservationStation("Add" + (i + 1)));
        }
        for (int i = 0; i < numMulDiv; i++) {
            mulDivStations.add(new ReservationStation("Mul" + (i + 1)));
        }

        // Initialize integer reservation stations
        for (int i = 0; i < numIntAddSub; i++) {
            intAddSubStations.add(new ReservationStation("IntAdd" + (i + 1)));
        }
        for (int i = 0; i < numIntMulDiv; i++) {
            intMulDivStations.add(new ReservationStation("IntMul" + (i + 1)));
        }

        // Initialize load/store buffers
        for (int i = 0; i < numLoad; i++) {
            loadBuffers.add(new LoadBuffer("Load" + (i + 1)));
        }
        for (int i = 0; i < numStore; i++) {
            storeBuffers.add(new StoreBuffer("Store" + (i + 1)));
        }

        // Initialize Integer load/store buffers
        for (int i = 0; i < numIntLoad; i++) {
            intLoadBuffers.add(new LoadBuffer("IntLoad" + (i + 1)));
        }
        for (int i = 0; i < numIntStore; i++) {
            intStoreBuffers.add(new StoreBuffer("IntStore" + (i + 1)));
        }

        // Initialize branch stations
        for (int i = 0; i < numBranch; i++) {
            branchStations.add(new BranchStation("Branch" + (i + 1)));
        }

        updateDisplay();
    }

    //-------------------Actions Handlers-------------------
    private void handleStep() {
        currentCycle++;
        executeOneCycle();
        updateDisplay();
    }
    private void handleReset() {
        currentCycle = 0;
        currentInstruction = 0;
        instructions.clear();
        memory.clear();
        registerFile.clearAll();
        intRegisterFile.clearAll();
        cache.clear();
        instructionTable.getItems().clear();
        setupInitialValues();
        updateDisplay();
        stepButton.setDisable(false);
    }

    //-------------------Scene Updates-------------------
    private void updateDependentUnits(ExecutionUnit completedUnit) {
        //update register file
        registerFile.clearStatus(completedUnit.getName(), completedUnit.getResult());
//        registerFile.setValue(completedUnit.getName(),completedUnit.getResult());

        intRegisterFile.clearStatus(completedUnit.getName(), completedUnit.getResult());
//        intRegisterFile.setValue(completedUnit.getName(),completedUnit.getResult());


        //give value to all rs that need it
        updateWaitingUnits(addSubStations, completedUnit);
        updateWaitingUnits(mulDivStations, completedUnit);
        updateWaitingUnits(intAddSubStations, completedUnit);
        updateWaitingUnits(intMulDivStations, completedUnit);
        updateWaitingUnits(branchStations, completedUnit);

        //give value to all store that need it
        for (StoreBuffer sb : storeBuffers) {
            if (sb.getQ().equals(completedUnit.getName())) {
                sb.setQ("");
                sb.setValue(Double.parseDouble(completedUnit.getResult()));
            }
        }

        for (StoreBuffer sb : intStoreBuffers) {
            if (sb.getQ().equals(completedUnit.getName())) {
                sb.setQ("");
                sb.setValue(Double.parseDouble(completedUnit.getResult()));
            }
        }
    }
    private void updateWaitingUnits(List<? extends ExecutionUnit> units, ExecutionUnit completedUnit) {
        for (ExecutionUnit unit : units) {
            if (unit.getQj().equals(completedUnit.getName())) {
                unit.setVj(completedUnit.getResult());
                unit.setQj("");
            }
            if (unit.getQk().equals(completedUnit.getName())) {
                unit.setVk(completedUnit.getResult());
                unit.setQk("");
            }
        }
    }
    private void updateDisplay() {
        cycleLabel.setText("Cycle " + currentCycle);

        // Update the reservation station and buffer tables
        addSubTable.setItems(FXCollections.observableArrayList(addSubStations));
        mulDivTable.setItems(FXCollections.observableArrayList(mulDivStations));
        loadBufferTable.setItems(FXCollections.observableArrayList(loadBuffers));
        storeBufferTable.setItems(FXCollections.observableArrayList(storeBuffers));

        // Update integer reservation stations and buffers
        intAddSubTable.setItems(FXCollections.observableArrayList(intAddSubStations));
        intMulDivTable.setItems(FXCollections.observableArrayList(intMulDivStations));
        intLoadBufferTable.setItems(FXCollections.observableArrayList(intLoadBuffers));
        intStoreBufferTable.setItems(FXCollections.observableArrayList(intStoreBuffers));

        // Add branch stations update if there's a table for it in your UI
        branchTable.setItems(FXCollections.observableArrayList(branchStations));

        updateRegisterFileDisplay();
    }
    private void updateRegisterFileDisplay() {
        registerFileGrid.getChildren().clear();
        for (int i = 0; i < 32; i++) {
            Label nameLabel = new Label("F" + i);
            Label valueLabel = new Label(registerFile.getValue("F" + i));
            Label statusLabel = new Label(registerFile.getStatus("F" + i));
            registerFileGrid.add(nameLabel, 0, i);
            registerFileGrid.add(valueLabel, 1, i);
            registerFileGrid.add(statusLabel, 2, i);
        }

        // Update integer register file display
        intRegisterFileGrid.getChildren().clear();
        for (int i = 0; i < 32; i++) {
            Label nameLabel = new Label("R" + i);
            Label valueLabel = new Label(intRegisterFile.getValue("R" + i));
            Label statusLabel = new Label(intRegisterFile.getStatus("R" + i));


            intRegisterFileGrid.add(nameLabel, 0, i);
            intRegisterFileGrid.add(valueLabel, 1, i);
            intRegisterFileGrid.add(statusLabel, 2, i);
        }

    }

    //----------------First Available Station----------------
    private ReservationStation firstAvailableStation(List<ReservationStation> stations) {
        for (ReservationStation station : stations) {
            if (!station.isBusy()) {
                return station;
            }
        }
        return null;
    }
    private BranchStation firstAvailableBranchStation(List<BranchStation> stations) {
        for (BranchStation station : stations) {
            if (!station.isBusy()) {
                return station;
            }
        }
        return null;
    }
    private LoadBuffer firstAvailableLoadBuffer(List<LoadBuffer> buffers) {
        for (LoadBuffer buffer : buffers) {
            if (!buffer.isBusy()) {
                return buffer;
            }
        }
        return null;
    }
    private StoreBuffer firstAvailableStoreBuffer(List<StoreBuffer> buffers) {
        for (StoreBuffer buffer : buffers) {
            if (!buffer.isBusy()) {
                return buffer;
            }
        }
        return null;
    }
    private boolean hasAvailableStation(List<ReservationStation> stations) {
        for (ReservationStation station : stations) {
            if (!station.isBusy()) {
                return true;
            }
        }
        return false;
    }
    //----------------Branch Instructions----------------
    private boolean isBranchInstruction(String op) {
        return op.equals("BEQ") || op.equals("BNE");
    }
    private void markLoopInstructions(int startIndex, int endIndex) {
        loopStartIndex = startIndex;
        loopEndIndex = endIndex;
        for (int i = 0; i < instructions.size(); i++) {
            InstructionEntry instruction = instructions.get(i);
            instruction.setInLoop(i >= startIndex && i <= endIndex);
        }
        System.out.println("Loop marked from " + startIndex + " to " + endIndex);
    }
    private boolean hasAvailableBranchStation(List<BranchStation> stations) {
        for (BranchStation station : stations) {
            if (!station.isBusy()) {
                return true;
            }
        }
        return false;
    }
    private void issueToBranch(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        BranchStation rs = firstAvailableBranchStation(branchStations);
        if (rs == null) return;

        rs.setInstruction(instruction);
        rs.setBusy(true);
        rs.setOperation(op);

        // Set up source operands for comparison
        rs.setVj(intRegisterFile.getValue(dest));
        rs.setVk(src1 != null ? intRegisterFile.getValue(src1) : "");
        rs.setQj(intRegisterFile.getStatus(dest));
        rs.setQk(src1 != null ? intRegisterFile.getStatus(src1) : "");

        rs.setCycles(operations.get(op));
        rs.setTarget(src2);

        instruction.setIssueTime(currentCycle);
        System.out.println("Branch instruction issued: " + instruction.getInstruction());
    }

    //--------------Instruction Issue----------------
    private boolean canIssueInstruction(InstructionEntry instruction) {
        //to get tpe of opperation to know which rs or buffer to check
        String[] parts = instruction.getInstruction().split(" ");
        String op = parts[0];
                    if (!(op.equals("BNE") || op.equals("BEQ"))) {
                        op = op.replaceAll(".*:", "");
                    }
        // Use a regular expression to remove any label followed by a colon
        if (!(op.equals("BNE") || op.equals("BEQ"))) {
            op = op.replaceAll(".*:", "");
        }
        String dest = parts[1].replace(",", "");
        String src1 = parts[2].replace(",", "");
        String src2 = parts.length > 3 ? parts[3] : null;
        System.out.println("Dest: " + dest);
        System.out.println("src: " + src1);


        //is there an empty reservation station or buffer for this instruction?
        if (op.equals("ADD.D") || op.equals("ADD.S") || op.equals("SUB.D") || op.equals("SUB.S")) {
            return hasAvailableStation(addSubStations);
        } else if (op.equals("ADD") || op.equals("SUB") || op.equals("ADDI") || op.equals("SUBI") || op.equals("DADDI") || op.equals("DSUBI")) {
            return hasAvailableStation(intAddSubStations);
        } else if (op.equals("MUL.D") || op.equals("MUL.S") || op.equals("DIV.D") || op.equals("DIV.S")) {
            return hasAvailableStation(mulDivStations);
        } else if (op.equals("MUL") || op.equals("DIV")) {
            return hasAvailableStation(intMulDivStations);
        } else if (op.equals("LD") || op.equals("LW") || op.equals("L.S") || op.equals("L.D")) {
            int effectiveAddress = calculateEffectiveAddress(src1);
            for (StoreBuffer sb : storeBuffers) {
                if (sb.isBusy() && sb.getAddress() == effectiveAddress) {
                    return false; //RAW
                }
            }
            for (LoadBuffer buffer : loadBuffers) {
                if (!buffer.isBusy()) {
                    return true;
                }
            }
            return false;
        } else if (op.equals("SD") || op.equals("SW") || op.equals("S.S") || op.equals("S.D")) {
            int effectiveAddress = calculateEffectiveAddress(src1); // src is the address for stores
            for (LoadBuffer lb : loadBuffers) {
                if (lb.isBusy() && lb.getAddress() == effectiveAddress) {
                    return false;//WAR
                }
            }
            for (StoreBuffer buffer : storeBuffers) {
                if (buffer.isBusy() && buffer.getAddress() == effectiveAddress) {
                    return false; //WAW
                }
            }
            for (StoreBuffer buffer : storeBuffers) {
                if (!buffer.isBusy()) {
                    return hasAvailableBranchStation(branchStations);
                }
            }
            return false;
        }

        if (op.equals("BNE") || op.equals("BEQ")) {
            return hasAvailableBranchStation(branchStations);
        }
        return false;
    }
    private int calculateEffectiveAddress(String addressString) {
        if (addressString.contains("(")) {
            String[] parts = addressString.split("[()]");
            int offset = Integer.parseInt(parts[0]); // Offset remains an integer
            String register = parts[1];

            if (!register.startsWith("R")) {
                System.out.println("Invalid register for address calculation: " + register);
                // Convert register value to double and cast to int
                double registerValue = Double.parseDouble(registerFile.getValue(register));
                int baseAddress = (int) Math.floor(registerValue);

                return baseAddress + offset;
            } else {
                int baseAddress = (int) Double.parseDouble(registerFile.getValue(register));
                System.out.println("Base Address: " + baseAddress);
                System.out.println("Offset: " + offset);
                return baseAddress + offset;
            }
        } if (addressString.startsWith("R")) {

            double registerValue = Double.parseDouble(intRegisterFile.getValue(addressString));
            return (int) Math.floor(registerValue);
        } else {
            System.out.println(addressString);
            return Integer.parseInt(addressString); // For standalone addresses
        }
    }
    private void issueToAddSubStation(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        ReservationStation rs = firstAvailableStation(addSubStations);

        if (rs == null) return;

        rs.setInstruction(instruction);
        System.out.println(rs);
        rs.setBusy(true);
        rs.setOperation(op);
        rs.setVj(registerFile.getValue(src1));
        rs.setVk(src2 != null ? registerFile.getValue(src2) : "");
        rs.setQj(registerFile.getStatus(src1));
        rs.setQk(src2 != null ? registerFile.getStatus(src2) : "");
        rs.setCycles(operations.get(op));
        registerFile.setStatus(dest, rs.getName());
        instruction.setIssueTime(currentCycle);
    }
    private void issueToMulDivStation(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        ReservationStation rs = firstAvailableStation(mulDivStations);
        rs.setInstruction(instruction);
        rs.setBusy(true);
        rs.setOperation(op);
        rs.setVj(registerFile.getValue(src1));
        rs.setVk(registerFile.getValue(src2));
        rs.setQj(registerFile.getStatus(src1));
        rs.setQk(registerFile.getStatus(src2));
        rs.setCycles(operations.get(op));
        registerFile.setStatus(dest, rs.getName());
        instruction.setIssueTime(currentCycle);
    }
    private void issueToIntAddSubStation(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        ReservationStation rs = firstAvailableStation(intAddSubStations);

        if (rs == null) return;

        rs.setInstruction(instruction);
        rs.setBusy(true);
        rs.setOperation(op);
        rs.setVj(intRegisterFile.getValue(src1));
        rs.setVk(src2);
        rs.setQj(intRegisterFile.getStatus(src1));
        rs.setQk(src2 != null ? intRegisterFile.getStatus(src2) : "");
        rs.setCycles(operations.get(op));
        intRegisterFile.setStatus(dest, rs.getName());
        instruction.setIssueTime(currentCycle);
    }
    private void issueToIntMulDivStation(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        ReservationStation rs = firstAvailableStation(intMulDivStations);

        if (rs == null) return;

        rs.setInstruction(instruction);
        rs.setBusy(true);
        rs.setOperation(op);
        rs.setVj(intRegisterFile.getValue(src1));
        rs.setVk(src2);
        rs.setQj(intRegisterFile.getStatus(src1));
        rs.setQk(intRegisterFile.getStatus(src2));
        rs.setCycles(operations.get(op));
        intRegisterFile.setStatus(dest, rs.getName());
        instruction.setIssueTime(currentCycle);
    }
    private void issueToLoadBuffer(InstructionEntry instruction, String dest, String addressString, LoadBuffer lb) {
        if (lb == null) return;
        lb.setInstruction(instruction);
        lb.setBusy(true);
        int effectiveAddress = calculateEffectiveAddress(addressString);
        lb.setAddress(effectiveAddress);
        registerFile.setStatus(dest, lb.getName());
        instruction.setIssueTime(currentCycle);
    }
    private void issueToStoreBuffer(InstructionEntry instruction, String src, String addressString, StoreBuffer sb) {
        if (sb == null) return;
        sb.setInstruction(instruction);
        sb.setBusy(true);
        int effectiveAddress = calculateEffectiveAddress(addressString);
        sb.setAddress(effectiveAddress);
        sb.setValue(Double.parseDouble(registerFile.getValue(src)));
        sb.setQ(registerFile.getStatus(src));
        instruction.setIssueTime(currentCycle);
    }
    private void issueInstruction(InstructionEntry instruction1) {
        instructions.add(new InstructionEntry(instruction1.getInstruction(), loop, null));
        InstructionEntry instruction = instructions.get(instructions.size() - 1);
        String[] parts = instruction.getInstruction().split(" ");
        String op = parts[0];
                    if (!(op.equals("BNE") || op.equals("BEQ"))) {
                        op = op.replaceAll(".*:", "");
                    }
        op = op.replaceAll(".*:", "");
        String dest = parts[1].replace(",", "");
        String src1 = parts.length > 2 ? parts[2].replace(",", "") : "";
        String src2 = parts.length > 3 ? parts[3].replace(",", "") : "";
        System.out.println("Dest: " + dest);
        System.out.println("src: " + src1);
        System.out.println("src2: " + src2);

        switch (op) {
            case "ADD", "SUB", "ADDI", "SUBI", "DADDI", "DSUBI" ->
                    issueToIntAddSubStation(instruction, op, dest, src1, src2);
            case "MUL", "DIV" -> issueToIntMulDivStation(instruction, op, dest, src1, src2);
            case "ADD.D", "ADD.S", "SUB.D", "SUB.S" -> issueToAddSubStation(instruction, op, dest, src1, src2);
            case "MUL.D", "MUL.S", "DIV.D", "DIV.S" -> issueToMulDivStation(instruction, op, dest, src1, src2);
            case "L.D", "L.S" -> {
                LoadBuffer lb = firstAvailableLoadBuffer(loadBuffers);
                issueToLoadBuffer(instruction, dest, src1, lb);
            }
            case "LW", "LD" -> {
                LoadBuffer lb = firstAvailableLoadBuffer(intLoadBuffers);
                issueToLoadBuffer(instruction, dest, src1, lb);
            }
            case "S.D", "S.S" -> {
                StoreBuffer sb = firstAvailableStoreBuffer(storeBuffers);
                issueToStoreBuffer(instruction, dest, src1, sb); // src1 is the address for store

            }
            case "SW", "SD" -> {
                StoreBuffer sb = firstAvailableStoreBuffer(intStoreBuffers);
                issueToStoreBuffer(instruction, dest, src1, sb); // src1 is the address for store

            }
            case "BEQ", "BNE" -> issueToBranch(instruction, op, dest, src1, src2);
        }

        // Set issue time for the instruction
        instruction.setIssueTime(currentCycle);

        // Add the issued instruction to the display table
//        if (!instructionTable.getItems().contains(instruction)) {
        instructionTable.getItems().add(instruction);
//        }

        updateDisplay();
    }


    //--------------Instruction Execution----------------
    private void performOperation(ReservationStation rs) {
        String operation = rs.getOperation();
        double result = 0;

        try {
            // Special handling for branch instructions
            if (operation.equals("BEQ") || operation.equals("BNE")) {
                // Branch handling remains the same
                double vj = parseFloat(rs.getVj());
                double vk = parseFloat(rs.getVk());
                result = (vj == vk) ? 1 : 0;
                rs.setResult(Double.parseDouble(String.valueOf(result)));
                return;
            }

            // Regular arithmetic operations
            double vj = parseFloat(rs.getVj());
            double vk = parseFloat(rs.getVk());

            // Get the destination register from the instruction
            String[] parts = rs.getInstruction().getInstruction().split(" ");
            String destReg = parts[1].replace(",", "");

            switch (operation) {
                // Integer operations
                case "ADD":
                case "DADDI":
                    result = vj + vk;
                    if (operation.equals("ADD") ) {
                        result = (int) result;
//                        intRegisterFile.setValue(destReg, String.valueOf((int)result));
                    } else {
//                        intRegisterFile.setValue(destReg, String.valueOf(result));
                    }
                    break;

                case "SUB":
                case "DSUBI":
                    result = vj - vk;
                    if (operation.equals("SUB")) {
                        result = (int) result;
//                        intRegisterFile.setValue(destReg, String.valueOf((int)result));
                    } else {
//                        intRegisterFile.setValue(destReg, String.valueOf(result));
                    }
                    break;

                // Floating point operations
                case "ADD.D":
                case "ADD.S":
                    result = vj + vk;
                    registerFile.setValue(destReg, String.format("%.2f", result));
                    break;

                case "SUB.D":
                case "SUB.S":
                    result = vj - vk;
                    registerFile.setValue(destReg, String.format("%.2f", result));
                    break;

                case "MUL":
                case "MUL.D":
                case "MUL.S":
                    result = vj * vk;
                    if (operation.equals("MUL")) {
                        result = (int) result;
//                        intRegisterFile.setValue(destReg, String.valueOf((int)result));
                    } else {
//                        registerFile.setValue(destReg, String.format("%.2f", result));
                    }
                    break;

                case "DIV":
                case "DIV.D":
                case "DIV.S":
                    if (vk != 0) {
                        result = vj / vk;
                        if (operation.equals("DIV")) {
                            result = (int) result;
                            intRegisterFile.setValue(destReg, String.valueOf((int)result));
                        } else {
                            registerFile.setValue(destReg, String.format("%.2f", result));
                        }
                    } else {
                        System.err.println("Error: Division by zero");
                        result = Double.NaN;
                    }
                    break;

                default:
                    System.err.println("Unknown operation: " + operation);
                    result = Double.NaN;
            }

            // Set the result in the reservation station
            if (operation.endsWith(".D") || operation.endsWith(".S")) {
                rs.setResult(Double.parseDouble(String.format("%.2f", result)));
            } else {
                rs.setResult(Double.parseDouble(String.valueOf((int) result)));
            }

        } catch (NumberFormatException e) {
            System.err.println("Error parsing operands for " + operation);
            rs.setResult(Double.parseDouble("NaN"));
        }
    }
    private void executeReservationStations(List<ReservationStation> stations) {
        for (ReservationStation rs : stations) {
            if (rs.isBusy() && rs.getQj().isEmpty() && rs.getQk().isEmpty()) {
                if (rs.getInstruction().getIssueTime() == currentCycle) {
                    continue; //skip
                }
                if (rs.getCycles() > 0) {
                    rs.setCycles(rs.getCycles() - 1);
                }
                if (rs.getCycles() == 0 && rs.getInstruction().getExecuteTime() == -1) {
                    rs.setReadyToWrite(true);
                    rs.getInstruction().setExecuteTime(currentCycle);
                    performOperation(rs);
                }
            }
        }
    }
    private void executeBranchStations(List<BranchStation> stations) {
        for (BranchStation rs : stations) {
            if (rs.isBusy() && rs.getQj().isEmpty() && rs.getQk().isEmpty()) {
                if (rs.getInstruction().getIssueTime() == currentCycle) {
                    continue; //skip
                }
                if (rs.getCycles() > 0) {
                    rs.setCycles(rs.getCycles() - 1);
                }
                if (rs.getCycles() == 0 && rs.getInstruction().getExecuteTime() == -1) {
                    rs.setReadyToWrite(true);
                    rs.getInstruction().setExecuteTime(currentCycle);
                    performOperation(rs);
                }
            }
        }
    }
    private void executeLoadBuffers() {
        for (LoadBuffer lb : loadBuffers) {
            if (lb.isBusy() && !lb.isReadyToWrite()) {
                if (lb.getInstruction().getIssueTime() == currentCycle) {
                    continue;
                }
                if(cache.requestWord(lb.getAddress(), lb.getName())){
                    InstructionEntry instruction = lb.getInstruction();
                    String[] parts = instruction.getInstruction().split(" ");
                    String op = parts[0];
                    if (!(op.equals("BNE") || op.equals("BEQ"))) {
                        op = op.replaceAll(".*:", "");
                    }
                    String destReg = parts[1].replace(",", "");

                    double result;
                    if(op.equals("L.D")) {
                        result = cache.readDouble(lb.getAddress());
                        registerFile.setValue(destReg, String.format("%.2f", result));
                    } else if(op.equals("L.S")) {
                        result = cache.readFloat(lb.getAddress());
                        registerFile.setValue(destReg, String.format("%.2f", result));
                    } else {
                        throw new RuntimeException("Invalid operation: " + op);
                    }

                    lb.setResult(result);
                    lb.setReadyToWrite(true);
                    lb.getInstruction().setExecuteTime(currentCycle);
                }
            }
        }

        // Integer load buffer
        for (LoadBuffer lb : intLoadBuffers) {
            if (lb.isBusy() && !lb.isReadyToWrite()) {
                if (lb.getInstruction().getIssueTime() == currentCycle) {
                    continue;
                }
                if(cache.requestWord(lb.getAddress(), lb.getName())){
                    InstructionEntry instruction = lb.getInstruction();
                    String[] parts = instruction.getInstruction().split(" ");
                    String op = parts[0];
                    if (!(op.equals("BNE") || op.equals("BEQ"))) {
                        op = op.replaceAll(".*:", "");
                    }
                    String destReg = parts[1].replace(",", "");

                    double result;
                    if(op.equals("LD")) {
                        result = cache.readLong(lb.getAddress());
                        intRegisterFile.setValue(destReg, String.valueOf((long)result));
                    } else if(op.equals("LW")) {
                        result = cache.readWord(lb.getAddress());
                        intRegisterFile.setValue(destReg, String.valueOf((int)result));
                    } else {
                        throw new RuntimeException("Invalid operation: " + op);
                    }

                    lb.setResult(result);
                    lb.setReadyToWrite(true);
                    lb.getInstruction().setExecuteTime(currentCycle);
                }
            }
        }
    }
    private void executeStoreBuffers() {
        for (StoreBuffer sb : storeBuffers) {
            if (sb.isBusy() && sb.getQ().isEmpty() && !sb.isReadyToWrite()) {
                if (sb.getInstruction().getIssueTime() == currentCycle) {
                    continue;
                }
                if(cache.requestWord(sb.getAddress(), sb.getName())){
                    InstructionEntry instruction = sb.getInstruction();
                    String[] parts = instruction.getInstruction().split(" ");
                    String op = parts[0];
                    if (!(op.equals("BNE") || op.equals("BEQ"))) {
                        op = op.replaceAll(".*:", "");
                    }
                    String srcReg = parts[1].replace(",", "");

                    double value = sb.getValue();
                    if(op.equals("S.D")) {
                        cache.writeDouble(sb.getAddress(), value);
                        registerFile.setValue(srcReg, String.format("%.2f", value));
                    } else if(op.equals("S.S")) {
                        cache.writeFloat(sb.getAddress(), (float) value);
                        registerFile.setValue(srcReg, String.format("%.2f", value));
                    } else {
                        throw new RuntimeException("Invalid operation: " + op);
                    }

                    sb.setReadyToWrite(true);
                    sb.getInstruction().setExecuteTime(currentCycle);
                }
            }
        }

        for (StoreBuffer sb : intStoreBuffers) {
            if (sb.isBusy() && sb.getQ().isEmpty() && !sb.isReadyToWrite()) {
                if (sb.getInstruction().getIssueTime() == currentCycle) {
                    continue;
                }
                if(cache.requestWord(sb.getAddress(), sb.getName())){
                    InstructionEntry instruction = sb.getInstruction();
                    String[] parts = instruction.getInstruction().split(" ");
                    String op = parts[0];
                    if (!(op.equals("BNE") || op.equals("BEQ"))) {
                        op = op.replaceAll(".*:", "");
                    }
                    String srcReg = parts[1].replace(",", "");

                    double value = sb.getValue();
                    if(op.equals("SD")) {
                        cache.writeLong(sb.getAddress(), (long) value);
                        intRegisterFile.setValue(srcReg, String.valueOf((long)value));
                    } else if(op.equals("SW")) {
                        cache.writeWord(sb.getAddress(), (int) value);
                        intRegisterFile.setValue(srcReg, String.valueOf((int)value));
                    } else {
                        throw new RuntimeException("Invalid operation: " + op);
                    }

                    sb.setReadyToWrite(true);
                    sb.getInstruction().setExecuteTime(currentCycle);
                }
            }
        }
    }
    private void executeInstructions() {
        executeReservationStations(addSubStations);
        executeReservationStations(mulDivStations);
        executeLoadBuffers();
        executeStoreBuffers();

        executeReservationStations(intAddSubStations);
        executeReservationStations(intMulDivStations);

        executeBranchStations(branchStations);
    }
    private void executeOneCycle() {
        if (currentInstruction < getTotalInstructionsCount()) {

            if (!instructions.isEmpty() && canIssueInstruction(instructions.get(currentInstruction))) {
                InstructionEntry instruction = instructions.get(currentInstruction);

                // Calculate actual instruction index considering loops
                int actualIndex = currentInstruction;
                if (instruction.isInLoop() && loop > 1) {
                    actualIndex = loopStartIndex +
                            (currentInstruction - loopStartIndex) %
                                    (loopEndIndex - loopStartIndex + 1);
                }

                instruction = instructions.get(actualIndex);
                issueInstruction(instruction);

                String[] parts = instruction.getInstruction().split(" ");
                String op = parts[0];
                    if (!(op.equals("BNE") || op.equals("BEQ"))) {
                        op = op.replaceAll(".*:", "");
                    }
                if (isBranchInstruction(op)) {
                    branchCurrentInstruction = currentInstruction;
                } else {
                    currentInstruction++;
                }
            }
        }
        executeInstructions();
        writeResults();
        System.out.println("Current Instruction: " + currentInstruction);
        if (isProgramComplete()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Program Complete");
            alert.setHeaderText(null);
            alert.setContentText("Program execution has completed.\nTotal cycles: " + currentCycle);
            alert.showAndWait();
            stepButton.setDisable(true);
        }
    }
    //--------------Branches & Loops --------------
    private int getAddressFromLabel(String label) {
        for (int i = 0; i < instructions.size(); i++) {
            // Use a regular expression to remove any label followed by a colon
            String instruction = instructions.get(i).getInstruction();
            if (instruction.contains(":")) {
                String[] parts = instruction.split(":");
                if (parts.length > 0 && parts[0].trim().equals(label.trim())) {
                    return i;
                }
            }
        }
        return -1; // Return -1 if the label is not found
    }
    private void doBranch(BranchStation branchStation) {
        String op = branchStation.getOperation();
        String target = branchStation.getTarget();
        String src1 = branchStation.getVj();
        String src2 = branchStation.getVk();
        System.out.println("src1: " + "R"+src1);
        System.out.println("src2: " + "R"+src2);

        // Convert target to an address or instruction index
        int targetAddress = getAddressFromLabel(target);

        if (targetAddress == -1) {
            System.err.println("Error: Label " + target + " not found.");
            return;
        }

        currentInstruction = branchCurrentInstruction;
        boolean conditionMet = false;


        System.out.println("Branching: 🌳🌳");
        System.out.println(op);
        System.out.println(op.length());
        // Evaluate branch condition
        if (op.equals("BEQ")) {
            conditionMet = src1.equals(src2);
        } else if (op.equals("BNE")) {
            System.out.println("First val: " + intRegisterFile.getValue(src1));
            System.out.println("Second val: " + intRegisterFile.getValue(src2));
            conditionMet = !src1.equals(src2);
        }

        if (conditionMet) {
            System.out.println("Branch taken to instruction " + targetAddress);
            // Update loop counter and instruction pointer
            if (targetAddress <= currentInstruction) {
                // Only increment loop counter if branching backwards (loop behavior)
                loop++;
                System.out.println("Loop iteration: " + loop);
            }
            currentInstruction = targetAddress;
            lastBranchTaken = true;
        } else {
            System.out.println("Branch not taken");
            currentInstruction++;
            lastBranchTaken = false;
        }

        // Mark instruction completion
        branchStation.getInstruction().setWriteTime(currentCycle);
        branchStation.clear();
    }
    private int findLabelIndex(String label) {
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i).getInstruction().startsWith(label + ":")) {
                return i;
            }
        }
        return -1;
    }


    //------------Status----------------
    private int getTotalInstructionsCount() {
        if (loopStartIndex == -1 || loopEndIndex == -1) {
            return instructionCount; // No loop marked
        }

        int nonLoopInstructions = instructionCount - (loopEndIndex - loopStartIndex + 1);
        int loopInstructions = (loopEndIndex - loopStartIndex + 1) * loop;
        return nonLoopInstructions + loopInstructions;
    }
    private boolean isProgramComplete() {

        if (currentInstruction < getTotalInstructionsCount()) {
            return false;
        }
        System.out.println("Total Instructions: " + getTotalInstructionsCount());

        for (ReservationStation rs : addSubStations) {
            if (rs.isBusy()) return false;
        }
        for (ReservationStation rs : mulDivStations) {
            if (rs.isBusy()) return false;
        }
        for (ReservationStation rs : intAddSubStations) {
            if (rs.isBusy()) return false;
        }
        for (ReservationStation rs : intMulDivStations) {
            if (rs.isBusy()) return false;
        }

        for (LoadBuffer lb : loadBuffers) {
            if (lb.isBusy()) return false;
        }
        for (LoadBuffer lb : intLoadBuffers) {
            if (lb.isBusy()) return false;
        }

        for (StoreBuffer sb : storeBuffers) {
            if (sb.isBusy()) return false;
        }
        for (StoreBuffer sb : intStoreBuffers) {
            if (sb.isBusy()) return false;
        }

        for (BranchStation bs : branchStations) {
            if (bs.isBusy()) return false;
        }

        return true;
    }

    //-------------------Write Results-------------------
    private void collectReadyUnits(List<? extends ExecutionUnit> units, List<ExecutionUnit> readyUnits) {
        for (ExecutionUnit unit : units) {
            if (unit.isReadyToWrite() && currentCycle != unit.getInstruction().getExecuteTime()) {
                readyUnits.add(unit);
            }
        }
    }
    private void writeResults() {

        if (!branchStations.isEmpty() && branchStations.getFirst().isReadyToWrite()) {
            doBranch(branchStations.getFirst());
        }

        List<ExecutionUnit> readyStoreUnits = new ArrayList<>();
        List<ExecutionUnit> integerReadyStoreUnits = new ArrayList<>();

        for (StoreBuffer unit : storeBuffers) {
            if (unit.isReadyToWrite() && currentCycle != unit.getInstruction().getExecuteTime()) {
                readyStoreUnits.add(unit);
            }
        }

        for (StoreBuffer unit : intStoreBuffers) {
            if (unit.isReadyToWrite() && currentCycle != unit.getInstruction().getExecuteTime()) {
                integerReadyStoreUnits.add(unit);
            }
        }

        for (ExecutionUnit unit : readyStoreUnits) {
            writeBack(unit);
        }

        for (ExecutionUnit unit : integerReadyStoreUnits) {
            writeBack(unit);
        }

        List<ExecutionUnit> readyUnits = new ArrayList<>();
        List<ExecutionUnit> integerReadyUnits = new ArrayList<>();

        //get all units ready to write
        collectReadyUnits(addSubStations, readyUnits);
        collectReadyUnits(mulDivStations, readyUnits);
        collectReadyUnits(loadBuffers, readyUnits);

        collectReadyUnits(intAddSubStations, integerReadyUnits);
        collectReadyUnits(intMulDivStations, integerReadyUnits);
        collectReadyUnits(intLoadBuffers, integerReadyUnits);

        //fix if there are multiple registers ready to write use FIFO
        //Floats
        ExecutionUnit earliestUnit = null;
        int earliestIssueTime = Integer.MAX_VALUE;

        for (ExecutionUnit unit : readyUnits) {
            int issueTime = unit.getInstruction().getIssueTime();
            if (issueTime < earliestIssueTime) {
                earliestIssueTime = issueTime;
                earliestUnit = unit;
            }
        }

        // Write result for the earliest unit (if any)
        if (earliestUnit != null) {
            writeResult(earliestUnit);
        }

        //Integers
        ExecutionUnit integerEarliestUnit = null;
        int integerEarliestIssueTime = Integer.MAX_VALUE;

        for (ExecutionUnit unit : integerReadyUnits) {
            int integerIssueTime = unit.getInstruction().getIssueTime();
            if (integerIssueTime < integerEarliestIssueTime) {
                integerEarliestIssueTime = integerIssueTime;
                integerEarliestUnit = unit;
            }
        }

        if (integerEarliestUnit != null) {
            writeResult(integerEarliestUnit);
        }


    }
    private void writeBack(ExecutionUnit unit) {
        unit.getInstruction().setWriteTime(currentCycle);

        //clear the unit
        unit.clear();
    }
    private void writeResult(ExecutionUnit unit) {
        if (unit != null) {
            System.out.println(unit.getResult());
            //give value to all that needs them/ put on bus
            updateDependentUnits(unit);

            unit.getInstruction().setWriteTime(currentCycle);

            //clear the unit
            unit.clear();
        }
    }

}