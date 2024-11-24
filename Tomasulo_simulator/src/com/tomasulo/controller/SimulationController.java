package com.tomasulo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.tomasulo.model.*;
import java.util.*;
import java.io.IOException;

public class SimulationController {
    @FXML private TableView<InstructionEntry> instructionTable;
    @FXML private TableView<ReservationStation> addSubTable;
    @FXML private TableView<ReservationStation> mulDivTable;
    @FXML private TableView<LoadBuffer> loadBufferTable;
    @FXML private TableView<StoreBuffer> storeBufferTable;
    @FXML private GridPane registerFileGrid;
    @FXML private Label cycleLabel;
    @FXML private TextArea codeInput;
    
    private int currentCycle = 0;
    private List<InstructionEntry> instructions = new ArrayList<>();
    private Map<String, Integer> latencies = new HashMap<>();
    private Cache cache;
    private RegisterFile registerFile;
    private List<ReservationStation> addSubStations = new ArrayList<>();
    private List<ReservationStation> mulDivStations = new ArrayList<>();
    private List<LoadBuffer> loadBuffers = new ArrayList<>();
    private List<StoreBuffer> storeBuffers = new ArrayList<>();
    private Map<String, Integer> cacheParams = new HashMap<>();
    private Map<String, Integer> bufferSizes = new HashMap<>();
    
    @FXML
    public void initialize() {
        setupTables();
        setupInitialValues();
    }
    
    private void setupTables() {
        // Setup instruction table columns
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
        
        // Setup reservation station tables
        setupReservationStationTable(addSubTable);
        setupReservationStationTable(mulDivTable);
        
        // Setup load buffer table
        setupLoadBufferTable();
        
        // Setup store buffer table
        setupStoreBufferTable();
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
        // Set up default latencies
        latencies.put("ADD", 2);
        latencies.put("SUB", 2);
        latencies.put("MUL", 10);
        latencies.put("DIV", 40);
        latencies.put("L.D", 2);
        latencies.put("S.D", 2);
        latencies.put("ADDI", 1);
        latencies.put("SUBI", 1);
        
        // Initialize cache
        cache = new Cache(32, 4, 1, 10); // 32 blocks, 4 words per block, 1 cycle hit, 10 cycles miss
        
        // Initialize register file
        registerFile = new RegisterFile(32);
        
        // Initialize reservation stations
        for (int i = 0; i < 3; i++) {
            addSubStations.add(new ReservationStation("Add" + (i+1)));
            mulDivStations.add(new ReservationStation("Mul" + (i+1)));
        }
        
        // Initialize load/store buffers
        for (int i = 0; i < 3; i++) {
            loadBuffers.add(new LoadBuffer("Load" + (i+1)));
            storeBuffers.add(new StoreBuffer("Store" + (i+1)));
        }
        
        updateDisplay();
    }
    
    @FXML
    private void handleStep() {
        currentCycle++;
        executeOneCycle();
        updateDisplay();
    }
    
    @FXML
    private void handleRun() {
        while (!isSimulationComplete()) {
            handleStep();
        }
    }
    
    @FXML
    private void handleReset() {
        currentCycle = 0;
        instructions.clear();
        setupInitialValues();
    }
    
    @FXML
    private void handleLoadInstructions() {
        String[] lines = codeInput.getText().split("\n");
        instructions.clear();
        for (int i = 0; i < lines.length; i++) {
            instructions.add(new InstructionEntry(lines[i].trim(), i+1));
        }
        updateDisplay();
    }
    
    private void executeOneCycle() {
        // Issue stage
        if (!instructions.isEmpty() && canIssueInstruction(instructions.get(0))) {
            issueInstruction(instructions.get(0));
        }
        
        // Execute stage
        executeInstructions();
        
        // Write Result stage
        writeResults();
        
        // Handle bus contention
        resolveBusContention();
        
        // Update cache status
        updateCache();
    }
    
    private boolean canIssueInstruction(InstructionEntry instruction) {
        // Check if there's a free reservation station or buffer for this instruction
        String[] parts = instruction.getInstruction().split(" ");
        String op = parts[0];
        
        if (op.equals("ADD") || op.equals("SUB") || op.equals("ADDI") || op.equals("SUBI")) {
            return addSubStations.stream().anyMatch(rs -> !rs.isBusy());
        } else if (op.equals("MUL") || op.equals("DIV")) {
            return mulDivStations.stream().anyMatch(rs -> !rs.isBusy());
        } else if (op.equals("L.D")) {
            return loadBuffers.stream().anyMatch(lb -> !lb.isBusy());
        } else if (op.equals("S.D")) {
            return storeBuffers.stream().anyMatch(sb -> !sb.isBusy());
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
    
    private void issueToAddSubStation(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        ReservationStation rs = addSubStations.stream().filter(s -> !s.isBusy()).findFirst().get();
        rs.setBusy(true);
        rs.setOperation(op);
        rs.setVj(registerFile.getValue(src1));
        rs.setVk(src2 != null ? registerFile.getValue(src2) : "");
        rs.setQj(registerFile.getStatus(src1));
        rs.setQk(src2 != null ? registerFile.getStatus(src2) : "");
        rs.setCycles(latencies.get(op));
        registerFile.setStatus(dest, rs.getName());
    }
    
    private void issueToMulDivStation(InstructionEntry instruction, String op, String dest, String src1, String src2) {
        ReservationStation rs = mulDivStations.stream().filter(s -> !s.isBusy()).findFirst().get();
        rs.setBusy(true);
        rs.setOperation(op);
        rs.setVj(registerFile.getValue(src1));
        rs.setVk(registerFile.getValue(src2));
        rs.setQj(registerFile.getStatus(src1));
        rs.setQk(registerFile.getStatus(src2));
        rs.setCycles(latencies.get(op));
        registerFile.setStatus(dest, rs.getName());
    }
    
    private void issueToLoadBuffer(InstructionEntry instruction, String dest, String address) {
        LoadBuffer lb = loadBuffers.stream().filter(b -> !b.isBusy()).findFirst().get();
        lb.setBusy(true);
        lb.setAddress(Integer.parseInt(address));
        registerFile.setStatus(dest, lb.getName());
    }
    
    private void issueToStoreBuffer(InstructionEntry instruction, String src, String address) {
        StoreBuffer sb = storeBuffers.stream().filter(b -> !b.isBusy()).findFirst().get();
        sb.setBusy(true);
        sb.setAddress(Integer.parseInt(address));
        sb.setValue(Double.parseDouble(registerFile.getValue(src)));
        sb.setQ(registerFile.getStatus(src));
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
                if (rs.getCycles() > 0) {
                    rs.setCycles(rs.getCycles() - 1);
                }
                if (rs.getCycles() == 0) {
                    // Mark as ready to write result
                    rs.setReadyToWrite(true);
                }
            }
        }
    }
    
    private void executeLoadBuffers() {
        for (LoadBuffer lb : loadBuffers) {
            if (lb.isBusy() && !lb.isReadyToWrite()) {
                int accessTime = cache.getAccessTime(lb.getAddress());
                if (lb.getCycles() == 0) {
                    lb.setCycles(accessTime);
                }
                if (lb.getCycles() > 0) {
                    lb.setCycles(lb.getCycles() - 1);
                }
                if (lb.getCycles() == 0) {
                    // Mark as ready to write result
                    lb.setReadyToWrite(true);
                }
            }
        }
    }
    
    private void executeStoreBuffers() {
        for (StoreBuffer sb : storeBuffers) {
            if (sb.isBusy() && sb.getQ().isEmpty()) {
                int accessTime = cache.getAccessTime(sb.getAddress());
                if (sb.getCycles() == 0) {
                    sb.setCycles(accessTime);
                }
                if (sb.getCycles() > 0) {
                    sb.setCycles(sb.getCycles() - 1);
                }
                if (sb.getCycles() == 0) {
                    // Mark as ready to write result
                    sb.setReadyToWrite(true);
                }
            }
        }
    }
    
    private void writeResults() {
        List<String> resultsToWrite = new ArrayList<>();
        
        // Collect all results ready to write
        collectReadyResults((List<? extends ExecutionUnit>) addSubStations, resultsToWrite);
        collectReadyResults((List<? extends ExecutionUnit>) mulDivStations, resultsToWrite);
        collectReadyResults((List<? extends ExecutionUnit>) loadBuffers, resultsToWrite);
        collectReadyResults((List<? extends ExecutionUnit>) storeBuffers, resultsToWrite);
        
        // Write results
        for (String result : resultsToWrite) {
            writeResult(result);
        }
    }
    
    private void collectReadyResults(List<? extends ExecutionUnit> units, List<String> results) {
        for (ExecutionUnit unit : units) {
            if (unit.isReadyToWrite()) {
                results.add(unit.getName());
            }
        }
    }
    
    private void writeResult(String unitName) {
        ExecutionUnit unit = findExecutionUnit(unitName);
        if (unit != null) {
            // Update register file and other waiting stations
            updateDependentUnits(unit);
            
            // Clear the unit
            unit.clear();
        }
    }
    
    
    private void updateDependentUnits(ExecutionUnit completedUnit) {
        // Update register file
        registerFile.clearStatus(completedUnit.getName());
        
        // Update waiting reservation stations
        updateWaitingUnits((List<? extends ExecutionUnit>) addSubStations, completedUnit);
        updateWaitingUnits((List<? extends ExecutionUnit>) mulDivStations, completedUnit);
        
        // Update waiting store buffers
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
    
    @FXML
    private void handleConfigure() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConfigurationView.fxml"));
            Parent root = loader.load();
            
            ConfigurationController configController = loader.getController();
            configController.setLatencies(latencies);
            configController.setCacheParams(Map.of(
                "size", cache.getSize(),
                "blockSize", cache.getBlockSize(),
                "hitLatency", cache.getHitLatency(),
                "missLatency", cache.getMissLatency()
            ));
            configController.setBufferSizes(Map.of(
                "addSub", addSubStations.size(),
                "mulDiv", mulDivStations.size(),
                "load", loadBuffers.size(),
                "store", storeBuffers.size()
            ));
            
            Stage stage = new Stage();
            stage.setTitle("Configuration");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // After the dialog is closed, update the simulator with new values
            updateSimulatorConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private interface ExecutionUnit {
        String getName();
        boolean isReadyToWrite();
        void clear();
        String getQj();
        String getQk();
        void setVj(String value);
        void setVk(String value);
        void setQj(String value);
        void setQk(String value);
        String getResult();
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
}