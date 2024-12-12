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
    private RegisterFile intRegisterFile;
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
    private List<ReservationStation> intAddSubStations = new ArrayList<>(); // Integer Add/Sub stations
    private List<ReservationStation> intMulDivStations = new ArrayList<>(); // Integer Mul/Div stations
    private List<LoadBuffer> intLoadBuffers = new ArrayList<>(); // Integer Load buffers
    private List<StoreBuffer> intStoreBuffers = new ArrayList<>(); // Integer Store buffers
    private List<BranchStation> branchStations = new ArrayList<>(); // Branch stations
    private Map<String, Integer> cacheParams = new HashMap<>();
    private Map<String, Integer> bufferSizes = new HashMap<>();
    private int loop;

    public SimulationController() {
        root = new BorderPane();
        configView = config.createConfigView();
        simView = this.createView();
        Button switcher = new Button("Switch View");
        switcher.setOnAction(e -> switchView());
        root.setTop(switcher);
        root.setCenter(configView);
        scene = new Scene(root, 1200, 800);
    }

    public Scene getScene() {
        return scene;
    }

    public void switchView() {
        if (root.getCenter() == configView) {
            root.setCenter(simView);
            handleReset();
        } else
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


        VBox branchBox = new VBox(5);
        branchTable = new TableView<>();
        setupBranchStationTable(branchTable);
        branchBox.getChildren().addAll(new Label("Branch Stations"), branchTable);

        // Add branchBox to your layout
        stationsBox.getChildren().add(branchBox);

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
        setupLoadBufferTable();
        intLoadBox.getChildren().addAll(new Label("Integer Load Buffers"), intLoadBufferTable);

        VBox intStoreBox = new VBox(5);
        intStoreBufferTable = new TableView<>();
        setupStoreBufferTable();
        intStoreBox.getChildren().addAll(new Label("Integer Store Buffers"), intStoreBufferTable);

        intBuffersBox.getChildren().addAll(intLoadBox, intStoreBox);

        centerBox.getChildren().addAll(intStationsBox, intBuffersBox);
        root.setCenter(centerBox);

        // Right
        HBox rightBox = new HBox(10);
        registerFileGrid = new GridPane();
        registerFileGrid.setHgap(5);
        registerFileGrid.setVgap(5);

        // Integer register file grid
        intRegisterFileGrid = new GridPane();
        intRegisterFileGrid.setHgap(5);
        intRegisterFileGrid.setVgap(5);

        rightBox.getChildren().addAll(new Label("Register File"), registerFileGrid, new Label("Integer Register File"), intRegisterFileGrid);
        root.setRight(rightBox);

        setupInitialValues();

        return root;
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
        loop = 1;
        // Set up default operations
        operations.put("ADD", 2);
        operations.put("SUB", 2);
        operations.put("MUL", 10);
        operations.put("DIV", 40);
        operations.put("L.D", 2);
        operations.put("S.D", 2);
        operations.put("ADDI", 1);
        operations.put("SUBI", 1);
        operations.put("DADDI", 1);
        operations.put("DSUBI", 1);
        operations.put("ADD.D", 2);
        operations.put("ADD.S", 2);
        operations.put("SUB.D", 2);
        operations.put("SUB.S", 2);
        operations.put("MUL.D", 10);
        operations.put("MUL.S", 10);
        operations.put("DIV.D", 40);
        operations.put("DIV.S", 40);
        operations.put("LW", 2);
        operations.put("LD", 2);
        operations.put("L.S", 2);
        operations.put("SW", 2);
        operations.put("SD", 2);
        operations.put("S.S", 2);
        operations.put("BEQ", 1);
        operations.put("BNE", 1);
        if (!config.operations.isEmpty()) operations = config.operations;

        // Initialize cache
        if (config.cacheParams.isEmpty())
            cache = new Cache(32, 4, 1, 10); //32 blocks,4 words per block,1 cycle hit,10 cycles miss
        else
            cache = new Cache(config.cacheParams.get("size"),
                    config.cacheParams.get("blockSize"),
                    config.cacheParams.get("hitLatency"),
                    config.cacheParams.get("missLatency")
            );

        // Initialize register files
        registerFile = new RegisterFile(32);
        intRegisterFile = new RegisterFile(32);

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
        int numBranch = config.bufferSizes.getOrDefault("branch", 2);

        // Initialize floating-point reservation stations
        for (int i = 0; i < numAddSub; i++) {
            addSubStations.add(new ReservationStation("Add" + (i + 1)));
        }
        for (int i = 0; i < numMulDiv; i++) {
            mulDivStations.add(new ReservationStation("Mul" + (i + 1)));
        }

        // Initialize integer reservation stations
        for (int i = 0; i < numAddSub; i++) {
            intAddSubStations.add(new ReservationStation("IntAdd" + (i + 1)));
        }
        for (int i = 0; i < numMulDiv; i++) {
            intMulDivStations.add(new ReservationStation("IntMul" + (i + 1)));
        }

        // Initialize load/store buffers
        for (int i = 0; i < numLoad; i++) {
            loadBuffers.add(new LoadBuffer("Load" + (i + 1)));
            intLoadBuffers.add(new LoadBuffer("IntLoad" + (i + 1)));
        }
        for (int i = 0; i < numStore; i++) {
            storeBuffers.add(new StoreBuffer("Store" + (i + 1)));
            intStoreBuffers.add(new StoreBuffer("IntStore" + (i + 1)));
        }

        // Initialize branch stations
        for (int i = 0; i < numBranch; i++) {
            branchStations.add(new BranchStation("Branch" + (i + 1)));
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
        instructionTable.getItems().clear();
        setupInitialValues();
        updateDisplay();
    }


    private void handleLoadInstructions() {
        String[] lines = codeInput.getText().split("\n");
        instructions.clear();
        for (int i = 0; i < lines.length; i++) {
            instructions.add(new InstructionEntry(lines[i].trim(), loop, lines[i].trim())); // Initialize iteration to 1
        }
        currentInstruction = 0;

        //to know they've been loaded successfully
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instructions Loaded");
        alert.setHeaderText(null);
        alert.setContentText("Instructions have been successfully loaded.");
        alert.showAndWait();
    }


    private void executeOneCycle() {
        //Issue
        //if there are instructions to issue, and there's a place for the one in turn, then issue it
        if (currentInstruction < instructions.size()) {
            if (!instructions.isEmpty() && canIssueInstruction(instructions.get(currentInstruction))) {
                InstructionEntry instruction = instructions.get(currentInstruction);
                issueInstruction(instructions.get(currentInstruction));
                String[] parts = instruction.getInstruction().split(" ");
                String op = parts[0];
                if (!(op.equals("BNE") || op.equals("BEQ"))) {
                    currentInstruction++;
                }
            }
        }

        //Execute
        executeInstructions();

        //Write
        writeResults();

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
        // Use a regular expression to remove any label followed by a colon
        if (!(op.equals("BNE") || op.equals("BEQ"))) {
            op = op.replaceAll(".*:", "");
        }
        String dest = parts[1].replace(",", "");
        String src1 = parts[2].replace(",", "");
        String src2 = parts.length > 3 ? parts[3] : null;


        //is there an empty reservation station or buffer for this instruction?
        if (op.equals("ADD.D") || op.equals("ADD.S") || op.equals("SUB.D") || op.equals("SUB.S")) {
            return hasAvailableStation(addSubStations);
        } else if (op.equals("ADD") || op.equals("SUB") || op.equals("ADDI") || op.equals("SUBI") ||
                op.equals("DADDI") || op.equals("DSUBI")) {
            return hasAvailableStation(intAddSubStations);
        } else if (op.equals("MUL.D") || op.equals("MUL.S") || op.equals("DIV.D") || op.equals("DIV.S")) {
            return hasAvailableStation(mulDivStations);
        } else if (op.equals("MUL") || op.equals("DIV")){
            return hasAvailableStation(intMulDivStations);
        }else if (op.equals("L.D") || op.equals("LW") || op.equals("L.S")) {
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
        } else if (op.equals("S.D") || op.equals("SW") || op.equals("S.S")) {
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

        if (op.equals("BNE") || op.equals("BEQ")) {
            return true;
        }
        return false;
    }

    private void issueInstruction(InstructionEntry instruction1) {
        instructions.add(new InstructionEntry(instruction1.getInstruction(), loop, null));
        InstructionEntry instruction = instructions.get(instructions.size() - 1);
        String[] parts = instruction.getInstruction().split(" ");
        String op = parts[0];
        op = op.replaceAll(".*:", "");
        String dest = parts[1].replace(",", "");
        String src1 = parts.length > 2 ? parts[2].replace(",", "") : "";
        String src2 = parts.length > 3 ? parts[3].replace(",", "") : "";

        if (op.equals("ADD") || op.equals("SUB") || op.equals("ADDI") || op.equals("SUBI") ||
                op.equals("DADDI") || op.equals("DSUBI")) {
            issueToIntAddSubStation(instruction, op, dest, src1, src2);
        } else if (op.equals("MUL") || op.equals("DIV")) {
            issueToIntMulDivStation(instruction, op, dest, src1, src2);
        } else if (op.equals("ADD.D") || op.equals("ADD.S") || op.equals("SUB.D") || op.equals("SUB.S")) {
            issueToAddSubStation(instruction, op, dest, src1, src2);
        } else if (op.equals("MUL.D") || op.equals("MUL.S") || op.equals("DIV.D") || op.equals("DIV.S")) {
            issueToMulDivStation(instruction, op, dest, src1, src2);
        } else if (op.equals("L.D") || op.equals("LW") || op.equals("L.S")) {
            issueToLoadBuffer(instruction, dest, src1);
        } else if (op.equals("S.D") || op.equals("SW") || op.equals("S.S")) {
            issueToStoreBuffer(instruction, src1, dest);
        } else if (op.equals("BEQ") || op.equals("BNE")) {
            issueToBranch(instruction, op, dest, src1, src2);
        }

        // Set issue time for the instruction
        instruction.setIssueTime(currentCycle);

        // Add the issued instruction to the display table
//        if (!instructionTable.getItems().contains(instruction)) {
        instructionTable.getItems().add(instruction);
//        }

        updateDisplay();
    }


    private void issueToBranch(InstructionEntry instruction, String op, String src1, String src2, String target) {
        // Convert target to an address or instruction index
        int targetAddress = getAddressFromLabel(target);

        if (targetAddress == -1) {
            // Handle the case where the label is not found
            System.err.println("Error: Label " + target + " not found.");
            return;
        }

        // Check the condition
        boolean conditionMet = false;
        if (op.equals("BEQ")) {
            conditionMet = registerFile.getValue(src1).equals(registerFile.getValue(src2));
        } else if (op.equals("BNE")) {
            conditionMet = !registerFile.getValue(src1).equals(registerFile.getValue(src2));
        }

        if (conditionMet) {
            // Set current instruction to the target of the branch
            currentInstruction = targetAddress;

            // Increment the iteration count for the target instruction and re-issue it
//            InstructionEntry loopInstruction = instructions.get(targetAddress);
//            loopInstruction.setIteration(loopInstruction.getIteration() + 1);
            loop++;


        } else {
            // Move to the next instruction
            currentInstruction++;
        }

        // Set issue time for the current instruction
        instruction.setIssueTime(currentCycle);
        updateDisplay();
    }


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


    private void issueToAddSubStation(InstructionEntry instruction, String op, String dest, String src1, String
            src2) {
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

    private void issueToMulDivStation(InstructionEntry instruction, String op, String dest, String src1, String
            src2) {
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
        rs.setVk(src2 != null ? intRegisterFile.getValue(src2) : "");
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
        rs.setVk(intRegisterFile.getValue(src2));
        rs.setQj(intRegisterFile.getStatus(src1));
        rs.setQk(intRegisterFile.getStatus(src2));
        rs.setCycles(operations.get(op));
        intRegisterFile.setStatus(dest, rs.getName());
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
        double vj = parseFloat(rs.getVj());
        double vk = parseFloat(rs.getVk());
        double result = 0;

        switch (operation) {
            case "ADD":
            case "ADDI":
            case "DADDI":
                result = vj + vk;
                break;
            case "SUB":
            case "SUBI":
            case "DSUBI":
                result = vj - vk;
                break;
            case "MUL":
            case "MUL.D":
            case "MUL.S":
                result = vj * vk;
                break;
            case "DIV":
            case "DIV.D":
            case "DIV.S":
                if (vk != 0) {
                    result = vj / vk;
                } else {
                    System.err.println("Error: Division by zero");
                    result = Double.NaN;
                }
                break;
            case "ADD.D":
            case "ADD.S":
                result = vj + vk;
                break;
            case "SUB.D":
            case "SUB.S":
                result = vj - vk;
                break;
            default:
                System.err.println("Unknown operation: " + operation);
                result = Double.NaN;
        }

        rs.setResult(result);
    }


    private void writeResults() {
        List<ExecutionUnit> readyUnits = new ArrayList<>();

        //get all units ready to write
        collectReadyUnits(addSubStations, readyUnits);
        collectReadyUnits(mulDivStations, readyUnits);
        collectReadyUnits(loadBuffers, readyUnits);
        collectReadyUnits(storeBuffers, readyUnits);

        //fix if there are multiple registers ready to write use FIFO
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
    }


    private void collectReadyUnits(List<? extends ExecutionUnit> units, List<ExecutionUnit> readyUnits) {
        for (ExecutionUnit unit : units) {
            if (unit.isReadyToWrite()) {
                readyUnits.add(unit);
            }
        }
    }


    private void writeResult(ExecutionUnit unit) {
//        ExecutionUnit unit = findExecutionUnit(unitName);
        if (unit != null) {
            //give value to all that needs them/ put on bus
            updateDependentUnits(unit);

            unit.getInstruction().setWriteTime(currentCycle);

            //clear the unit
            unit.clear();
        }
    }
//    private ExecutionUnit findExecutionUnit(String name) {
//        for (ReservationStation unit : addSubStations) {
//            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
//        }
//        for (ReservationStation unit : mulDivStations) {
//            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
//        }
//        for (LoadBuffer unit : loadBuffers) {
//            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
//        }
//        for (StoreBuffer unit : storeBuffers) {
//            if (unit.getName().equals(name)) return (ExecutionUnit) unit;
//        }
//        return null;
//    }


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

    private void updateCache() {
        // Update cache state based on memory accesses
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
        // branchTable.setItems(FXCollections.observableArrayList(branchStations));

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

            nameLabel.setStyle("-fx-font-weight: bold;");
            valueLabel.setStyle("-fx-padding: 0 10 0 10;");
            statusLabel.setStyle("-fx-padding: 0 0 0 10;");

            intRegisterFileGrid.add(nameLabel, 0, i);
            intRegisterFileGrid.add(valueLabel, 1, i);
            intRegisterFileGrid.add(statusLabel, 2, i);
        }

        intRegisterFileGrid.setStyle("-fx-grid-lines-visible: true; -fx-padding: 5;");
    }



    private <T> void updateBufferSizes(List<T> buffer, int newSize) {
        while (buffer.size() > newSize) {
            buffer.remove(buffer.size() - 1);
        }
        while (buffer.size() < newSize) {
            if (buffer == addSubStations || buffer == mulDivStations) {
                ((List<ReservationStation>) buffer).add(new ReservationStation("RS" + (buffer.size() + 1)));
            } else if (buffer == loadBuffers) {
                ((List<LoadBuffer>) buffer).add(new LoadBuffer("LB" + (buffer.size() + 1)));
            } else if (buffer == storeBuffers) {
                ((List<StoreBuffer>) buffer).add(new StoreBuffer("SB" + (buffer.size() + 1)));
            }
        }
    }


}