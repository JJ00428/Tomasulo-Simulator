package com.tomasulo.controller;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.tomasulo.model.*;

import java.lang.reflect.Array;
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
    private GridPane registerFileGrid;
    private Label cycleLabel;
    private TextArea codeInput;

    private int currentCycle = 0;
    private int currentInstruction = 0;
    private List<InstructionEntry> instructions = new ArrayList<>();
    private Map<String, Integer> operations = new HashMap<>();
    private Cache cache;
    private RegisterFile registerFile;
    private List<ReservationStation> addSubStations = new ArrayList<>();
    private List<ReservationStation> mulDivStations = new ArrayList<>();
    private List<LoadBuffer> loadBuffers = new ArrayList<>();
    private List<StoreBuffer> storeBuffers = new ArrayList<>();
    private Map<String, Integer> cacheParams = new HashMap<>();
    private Map<String, Integer> bufferSizes = new HashMap<>();
    public SimulationController(){
        root = new BorderPane();
        configView = config.createConfigView();
        simView = this.createView();
        Button switcher = new Button("Switch View");
        switcher.setOnAction(e -> switchView());
        root.setTop(switcher);
        root.setCenter(configView);
        scene = new Scene(root, 1200, 800);
    }
    public Scene getScene(){
        return scene;
    }
    public void switchView(){
        if(root.getCenter() == configView){
            root.setCenter(simView);
            handleReset();
        }
        else
            root.setCenter(configView);
    }

    public BorderPane createView() {
        BorderPane root = new BorderPane();

        // Top
        HBox topBox = new HBox(10);
        cycleLabel = new Label("Cycle 0");
        cycleLabel.setStyle("-fx-font-size: 18px;");
        Button stepButton = new Button("Step");
        stepButton.setOnAction(e -> handleStep());
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> handleReset());
        topBox.getChildren().addAll(cycleLabel, stepButton, resetButton);
        root.setTop(topBox);

        // Left
        VBox leftBox = new VBox(10);
        codeInput = new TextArea();
        codeInput.setPrefRowCount(20);
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

        HBox buffersBox = new HBox(10);
        VBox loadBox = new VBox(5);
        loadBufferTable = new TableView<>();
        setupLoadBufferTable();
        loadBox.getChildren().addAll(new Label("Load Buffers"), loadBufferTable);

        VBox storeBox = new VBox(5);
        storeBufferTable = new TableView<>();
        setupStoreBufferTable();
        storeBox.getChildren().addAll(new Label("Store Buffers"), storeBufferTable);

        buffersBox.getChildren().addAll(loadBox, storeBox);

        centerBox.getChildren().addAll(instructionTable, stationsBox, buffersBox);
        root.setCenter(centerBox);

        // Right
        VBox rightBox = new VBox(10);
        registerFileGrid = new GridPane();
        registerFileGrid.setHgap(5);
        registerFileGrid.setVgap(5);
        rightBox.getChildren().addAll(new Label("Register File"), registerFileGrid);
        root.setRight(rightBox);

        setupInitialValues();

        return root;
    }

    private void setupInstructionTable() {
        TableColumn<InstructionEntry, Integer> iterationCol = new TableColumn<>("Iteration #");
        TableColumn<InstructionEntry, String> instructionCol = new TableColumn<>("Instruction");
        TableColumn<InstructionEntry, Integer> issueCol = new TableColumn<>("Issue");
        TableColumn<InstructionEntry, Integer> executeCol = new TableColumn<>("Execute");
        TableColumn<InstructionEntry, Integer> writeCol = new TableColumn<>("Write Result");

        iterationCol.setCellValueFactory(cellData -> cellData.getValue().iterationProperty().asObject());
        instructionCol.setCellValueFactory(cellData -> cellData.getValue().instructionProperty());
        issueCol.setCellValueFactory(cellData -> cellData.getValue().issueTimeProperty().asObject());
        executeCol.setCellValueFactory(cellData -> cellData.getValue().executeTimeProperty().asObject());
        writeCol.setCellValueFactory(cellData -> cellData.getValue().writeTimeProperty().asObject());

        instructionTable.getColumns().addAll(iterationCol, instructionCol, issueCol, executeCol, writeCol);
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

    private void setupLoadBufferTable() {
        TableColumn<LoadBuffer, String> nameCol = new TableColumn<>("Name");
        TableColumn<LoadBuffer, Boolean> busyCol = new TableColumn<>("Busy");
        TableColumn<LoadBuffer, Integer> addressCol = new TableColumn<>("Address");

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        busyCol.setCellValueFactory(cellData -> cellData.getValue().busyProperty());
        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty().asObject());

        loadBufferTable.getColumns().addAll(nameCol, busyCol, addressCol);
    }

    private void setupStoreBufferTable() {
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

        storeBufferTable.getColumns().addAll(nameCol, busyCol, addressCol, valueCol, qCol);
    }

    private void setupInitialValues() {
        // Set up default operations
        operations.put("ADD", 2);
        operations.put("SUB", 2);
        operations.put("MUL", 10);
        operations.put("DIV", 40);
        operations.put("L.D", 2);
        operations.put("S.D", 2);
        operations.put("ADDI", 1);
        operations.put("SUBI", 1);
        if(!config.operations.isEmpty())
            operations = config.operations;

        // Initialize cache
        if(config.cacheParams.isEmpty())
            cache = new Cache(32, 4, 1, 10); //32 blocks,4 words per block,1 cycle hit,10 cycles miss
        else
            cache = new Cache(config.cacheParams.get("size"),
                    config.cacheParams.get("blockSize"),
                    config.cacheParams.get("hitLatency"),
                    config.cacheParams.get("missLatency")
            );


        // Initialize register file
        registerFile = new RegisterFile(32);

        addSubStations.clear();
        mulDivStations.clear();
        loadBuffers.clear();
        storeBuffers.clear();


        int numAddSub = config.bufferSizes.getOrDefault("addSub", 3);
        int numMulDiv = config.bufferSizes.getOrDefault("mulDiv", 3);
        int numLoad = config.bufferSizes.getOrDefault("load", 3);
        int numStore = config.bufferSizes.getOrDefault("store", 3);
        // Initialize reservation stations
        for (int i = 0; i < numAddSub; i++) {
            addSubStations.add(new ReservationStation("Add" + (i+1)));
        }
        for (int i = 0; i < numMulDiv; i++) {
            mulDivStations.add(new ReservationStation("Mul" + (i+1)));
        }

        // Initialize load/store buffers
        for (int i = 0; i < numLoad; i++) {
            loadBuffers.add(new LoadBuffer("Load" + (i+1)));
        }
        for (int i = 0; i < numStore; i++) {
            storeBuffers.add(new StoreBuffer("Store" + (i+1)));
        }

        updateDisplay();
    }

    private void handleStep() {
        currentCycle++;
        executeOneCycle();
        updateDisplay();
    }

    private void handleReset() {
        currentCycle = 0;
        currentInstruction = 0;
        instructions.clear();
        setupInitialValues();
        updateDisplay();
    }


    private void handleLoadInstructions() {
        String[] lines = codeInput.getText().split("\n");
        instructions.clear();
        for (int i = 0; i < lines.length; i++) {
            instructions.add(new InstructionEntry(lines[i].trim(), i+1));
        }
        currentInstruction = 0;
        updateDisplay();
    }

    private void executeOneCycle() {
        //Issue
        //if there are instructions to issue, and there's a place for the one in turn, then issue it
        if(currentInstruction< instructions.size()) {
            if (!instructions.isEmpty() && canIssueInstruction(instructions.get(currentInstruction))) {
                issueInstruction(instructions.get(currentInstruction));
                currentInstruction++;
            }
        }

        //Execute
        executeInstructions();

        //Write
        writeResults();

        //if 2 writes happen at the same time
        resolveBusContention();

        // Update cache status
        updateCache();

    }

    private boolean hasAvailableStation(List<ReservationStation> stations) {
        for (ReservationStation station : stations) {
            if (!station.isBusy()) {
                return true;
            }
        }
        return false;
    }



    private boolean canIssueInstruction(InstructionEntry instruction) {
        //to get tpe of opperation to know which rs or buffer to check
        String[] parts = instruction.getInstruction().split(" ");
        String op = parts[0];
        String dest = parts[1].replace(",", "");
        String src1 = parts[2].replace(",", "");
        String src2 = parts.length > 3 ? parts[3] : null;


        //is there an empty reservation station or buffer for this instruction?
        if (op.equals("ADD") || op.equals("SUB") || op.equals("ADDI") || op.equals("SUBI")) {
            return hasAvailableStation(addSubStations);
        } else if (op.equals("MUL") || op.equals("DIV")) {
            return hasAvailableStation(mulDivStations);
        } else if (op.equals("L.D")) {
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
        } else if (op.equals("S.D")) {
            int effectiveAddress = calculateEffectiveAddress(dest);
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
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private void issueInstruction(InstructionEntry instruction) {
        String[] parts = instruction.getInstruction().split(" ");
        String op = parts[0];
        String dest = parts[1].replace(",", "");
        String src1 = parts[2].replace(",", "");
        String src2 = parts.length > 3 ? parts[3] : null;

        if (op.equals("ADD") || op.equals("SUB") || op.equals("ADDI") || op.equals("SUBI")) {
            issueToAddSubStation(instruction, op, dest, src1, src2);
        } else if (op.equals("MUL") || op.equals("DIV")) {
            issueToMulDivStation(instruction, op, dest, src1, src2);
        } else if (op.equals("L.D")) {
            issueToLoadBuffer(instruction, dest, src1);
        } else if (op.equals("S.D")) {
            issueToStoreBuffer(instruction, src1, dest);
        }
        
        instruction.setIssueTime(currentCycle);
    }

    private ReservationStation firstAvailableStation(List<ReservationStation> stations) {
        for (ReservationStation station : stations) {
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


    private void issueToAddSubStation(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        ReservationStation rs = firstAvailableStation(addSubStations);

        if (rs == null) return;

        rs.setInstruction(instruction);
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

    private int calculateEffectiveAddress(String addressString) {
        if (addressString.contains("(")) {
            String[] parts = addressString.split("[()]");
            int offset = Integer.parseInt(parts[0]); // Offset remains an integer
            String register = parts[1];

            // Convert register value to double and cast to int
            double registerValue = Double.parseDouble(registerFile.getValue(register));
            int baseAddress = (int) Math.floor(registerValue);

            return baseAddress + offset;
        } else {
            return Integer.parseInt(addressString); // For standalone addresses
        }
    }



    private void issueToLoadBuffer(InstructionEntry instruction, String dest, String addressString) {
        LoadBuffer lb = firstAvailableLoadBuffer(loadBuffers);
        if (lb == null) return;
        lb.setInstruction(instruction);
        lb.setBusy(true);
        int effectiveAddress = calculateEffectiveAddress(addressString);
        lb.setAddress(effectiveAddress);
        registerFile.setStatus(dest, lb.getName());
        instruction.setIssueTime(currentCycle);
    }

    private void issueToStoreBuffer(InstructionEntry instruction, String src, String addressString) {
        StoreBuffer sb = firstAvailableStoreBuffer(storeBuffers);
        if (sb == null) return;
        sb.setInstruction(instruction);
        sb.setBusy(true);
        int effectiveAddress = calculateEffectiveAddress(addressString);
        sb.setAddress(effectiveAddress);
        sb.setValue(Double.parseDouble(registerFile.getValue(src)));
        sb.setQ(registerFile.getStatus(src));
        instruction.setIssueTime(currentCycle);
    }
    
    private void executeInstructions() {
        executeReservationStations(addSubStations);
        executeReservationStations(mulDivStations);
        executeLoadBuffers();
        executeStoreBuffers();
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
    
    private void executeLoadBuffers() {
        for (LoadBuffer lb : loadBuffers) {
            if (lb.isBusy() && !lb.isReadyToWrite()) {
                if (lb.getInstruction().getIssueTime() == currentCycle) {
                    continue; //skip
                }
                int accessTime = cache.getAccessTime(lb.getAddress());
                if (lb.getCycles() == 0) {
                    lb.setCycles(accessTime);
                }
                if (lb.getCycles() > 0) {
                    lb.setCycles(lb.getCycles() - 1);
                }
                if (lb.getCycles() == 0 && lb.getInstruction().getExecuteTime() == -1) {
                    lb.setReadyToWrite(true);
                    lb.getInstruction().setExecuteTime(currentCycle);
                }
            }
        }
    }
    
    private void executeStoreBuffers() {
        for (StoreBuffer sb : storeBuffers) {
            if (sb.isBusy() && sb.getQ().isEmpty()) {
                if (sb.getInstruction().getIssueTime() == currentCycle) {
                    continue; //skip
                }
                int accessTime = cache.getAccessTime(sb.getAddress());
                if (sb.getCycles() == 0) {
                    sb.setCycles(accessTime);
                }
                if (sb.getCycles() > 0) {
                    sb.setCycles(sb.getCycles() - 1);
                }
                if (sb.getCycles() == 0 && sb.getInstruction().getExecuteTime() == -1) {
                    sb.setReadyToWrite(true);
                    sb.getInstruction().setExecuteTime(currentCycle);
                }
            }
        }
    }

    private void performOperation(ReservationStation rs) {
        String operation = rs.getOperation();
        double vj = Double.parseDouble(rs.getVj());
        double vk = Double.parseDouble(rs.getVk());
        double result = 0;

        switch (operation) {
            case "ADD":
            case "ADDI":
                result = vj + vk;
                break;
            case "SUB":
            case "SUBI":
                result = vj - vk;
                break;
            case "MUL":
                result = vj * vk;
                break;
            case "DIV":
                if (vk != 0) {
                    result = vj / vk;
                } else {
                    // Handle division by zero
                    System.err.println("Error: Division by zero");
                    result = Double.NaN;
                }
                break;
            default:
                System.err.println("Unknown operation: " + operation);
                result = Double.NaN;
        }

        rs.setResult(result);
    }


    private void writeResults() {
        List<String> resultsToWrite = new ArrayList<>();
        
        //get all results ready to write
        collectReadyResults(addSubStations, resultsToWrite);
        collectReadyResults(mulDivStations, resultsToWrite);
        collectReadyResults(loadBuffers, resultsToWrite);
        collectReadyResults(storeBuffers, resultsToWrite);
        
        //write them
        for (String result : resultsToWrite) {
            writeResult(result);
        }
    }


    private void collectReadyResults(List<? extends ExecutionUnit> units, List<String> results) {
        for (ExecutionUnit unit : units) {
            if (unit.isReadyToWrite()) {
                if (unit.getInstruction().getExecuteTime() == currentCycle) {
                    continue; //skip
                }
                results.add(unit.getName());
            }
        }
    }

    private void writeResult(String unitName) {
        ExecutionUnit unit = findExecutionUnit(unitName);
        if (unit != null) {
            //give value to all that needs them/ put on bus
            updateDependentUnits(unit);

            unit.getInstruction().setWriteTime(currentCycle);

            //clear the unit
            unit.clear();
        }
    }
    private ExecutionUnit findExecutionUnit(String name) {
        for (ReservationStation unit : addSubStations) {
            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
        }
        for (ReservationStation unit : mulDivStations) {
            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
        }
        for (LoadBuffer unit : loadBuffers) {
            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
        }
        for (StoreBuffer unit : storeBuffers) {
            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
        }
        return null;
    }



    
    
    private void updateDependentUnits(ExecutionUnit completedUnit) {
        //update register file
        registerFile.clearStatus(completedUnit.getName());
        
        //give value to all rs that need it
        updateWaitingUnits(addSubStations, completedUnit);
        updateWaitingUnits(mulDivStations, completedUnit);
        
        //give value to all store that need it
        for (StoreBuffer sb : storeBuffers) {
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
    
    private void resolveBusContention() {
        // Implement bus contention resolution logic
        // For example, prioritize based on instruction order or unit type
    }
    
    private void updateCache() {
        // Update cache state based on memory accesses
    }
    
    private void updateDisplay() {
        cycleLabel.setText("Cycle " + currentCycle);
        
        // Update instruction table
        instructionTable.setItems(FXCollections.observableArrayList(instructions));
        
        // Update reservation station tables
        addSubTable.setItems(FXCollections.observableArrayList(addSubStations));
        mulDivTable.setItems(FXCollections.observableArrayList(mulDivStations));
        
        // Update load/store buffer tables
        loadBufferTable.setItems(FXCollections.observableArrayList(loadBuffers));
        storeBufferTable.setItems(FXCollections.observableArrayList(storeBuffers));
        
        // Update register file display
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
    }
    
    private boolean isSimulationComplete() {
        return instructions.isEmpty() &&
                addSubStations.stream().noneMatch(ReservationStation::isBusy) &&
                mulDivStations.stream().noneMatch(ReservationStation::isBusy) &&
                loadBuffers.stream().noneMatch(LoadBuffer::isBusy) &&
                storeBuffers.stream().noneMatch(StoreBuffer::isBusy);
    }

    private void updateSimulatorConfiguration() {
        // Update cache
        cache = new Cache(
                cacheParams.get("size"),
                cacheParams.get("blockSize"),
                cacheParams.get("hitLatency"),
                cacheParams.get("missLatency")
        );

        // Update buffer sizes
        updateBufferSizes(addSubStations, bufferSizes.get("addSub"));
        updateBufferSizes(mulDivStations, bufferSizes.get("mulDiv"));
        updateBufferSizes(loadBuffers, bufferSizes.get("load"));
        updateBufferSizes(storeBuffers, bufferSizes.get("store"));

        // Latencies are already updated in the map

        // Refresh the display
        updateDisplay();
    }

    private <T> void updateBufferSizes(List<T> buffer, int newSize) {
        while (buffer.size() > newSize) {
            buffer.remove(buffer.size() - 1);
        }
        while (buffer.size() < newSize) {
            if (buffer == addSubStations || buffer == mulDivStations) {
                ((List<ReservationStation>)buffer).add(new ReservationStation("RS" + (buffer.size() + 1)));
            } else if (buffer == loadBuffers) {
                ((List<LoadBuffer>)buffer).add(new LoadBuffer("LB" + (buffer.size() + 1)));
            } else if (buffer == storeBuffers) {
                ((List<StoreBuffer>)buffer).add(new StoreBuffer("SB" + (buffer.size() + 1)));
            }
        }
    }

//    private interface ExecutionUnit {
//        String getName();
//        boolean isReadyToWrite();
//        void clear();
//        String getQj();
//        String getQk();
//        void setVj(String value);
//        void setVk(String value);
//        void setQj(String value);
//        void setQk(String value);
//        String getResult();
//        boolean isBusy();
//    }



}