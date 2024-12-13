package com.tomasulo.model;

import javafx.beans.property.*;

public class InstructionEntry {
    private final StringProperty instruction;
    private final IntegerProperty iteration;
    private final IntegerProperty issueTime;
    private final IntegerProperty executeTime;
    private final IntegerProperty writeTime;
    private final String code;
    private boolean isInLoop;

    public InstructionEntry(String inst, int itr, String code) {
        this.instruction = new SimpleStringProperty(inst);
        this.iteration = new SimpleIntegerProperty(itr);
        this.issueTime = new SimpleIntegerProperty(-1);
        this.executeTime = new SimpleIntegerProperty(-1);
        this.writeTime = new SimpleIntegerProperty(-1);
        this.code = code;
        this.isInLoop = false;
    }

    public boolean isInLoop() {
        return isInLoop;
    }

    public void setInLoop(boolean inLoop) {
        isInLoop = inLoop;
    }

    public String getInstruction() { return instruction.get(); }
    public void setInstruction(String value) { instruction.set(value); }
    public StringProperty instructionProperty() { return instruction; }

    public int getIteration() { return iteration.get(); }
    public void setIteration(int value) { iteration.set(value); }
    public IntegerProperty iterationProperty() { return iteration; }

    public int getIssueTime() { return issueTime.get(); }
    public void setIssueTime(int value) { issueTime.set(value); }
    public IntegerProperty issueTimeProperty() { return issueTime; }

    public int getExecuteTime() { return executeTime.get(); }
    public void setExecuteTime(int value) { executeTime.set(value); }
    public IntegerProperty executeTimeProperty() { return executeTime; }

    public int getWriteTime() { return writeTime.get(); }
    public void setWriteTime(int value) { writeTime.set(value); }
    public IntegerProperty writeTimeProperty() { return writeTime; }
    public String getCode() { return code;}

    @Override
    public String toString() {
        return "InstructionEntry{" +
                "instruction=" + instruction +
                ", iteration=" + iteration +
                ", issueTime=" + issueTime +
                ", executeTime=" + executeTime +
                ", writeTime=" + writeTime +
                ", code='" + code + '\'' +
                ", isInLoop=" + isInLoop +
                '}';
    }
}