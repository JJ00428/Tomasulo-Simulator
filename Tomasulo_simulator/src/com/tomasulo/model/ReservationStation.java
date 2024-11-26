package com.tomasulo.model;

import javafx.beans.property.*;

public class ReservationStation implements ExecutionUnit {
    private final StringProperty name;
    private final BooleanProperty busy;
    private final StringProperty operation;
    private final StringProperty vj;
    private final StringProperty vk;
    private final StringProperty qj;
    private final StringProperty qk;
    private final IntegerProperty cycles;
    private final BooleanProperty readyToWrite;
    private double result;

    public ReservationStation(String name) {
        this.name = new SimpleStringProperty(name);
        this.busy = new SimpleBooleanProperty(false);
        this.operation = new SimpleStringProperty("");
        this.vj = new SimpleStringProperty("");
        this.vk = new SimpleStringProperty("");
        this.qj = new SimpleStringProperty("");
        this.qk = new SimpleStringProperty("");
        this.cycles = new SimpleIntegerProperty(0);
        this.readyToWrite = new SimpleBooleanProperty(false);
        this.result = 0.0;
    }

    @Override
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    @Override
    public boolean isBusy() {
        return busy.get();
    }
    public void setBusy(boolean value) { busy.set(value); }
    public BooleanProperty busyProperty() { return busy; }

    public String getOperation() { return operation.get(); }
    public void setOperation(String value) { operation.set(value); }
    public StringProperty operationProperty() { return operation; }

    public String getVj() { return vj.get(); }
    @Override
    public void setVj(String value) { vj.set(value); }
    public StringProperty vjProperty() { return vj; }

    public String getVk() { return vk.get(); }
    @Override
    public void setVk(String value) { vk.set(value); }
    public StringProperty vkProperty() { return vk; }

    @Override
    public String getQj() { return qj.get(); }
    @Override
    public void setQj(String value) { qj.set(value); }
    public StringProperty qjProperty() { return qj; }

    @Override
    public String getQk() { return qk.get(); }
    @Override
    public void setQk(String value) { qk.set(value); }
    public StringProperty qkProperty() { return qk; }

    public int getCycles() { return cycles.get(); }
    public void setCycles(int value) { cycles.set(value); }
    public IntegerProperty cyclesProperty() { return cycles; }

    @Override
    public boolean isReadyToWrite() { return readyToWrite.get(); }
    public void setReadyToWrite(boolean value) { readyToWrite.set(value); }
    public BooleanProperty readyToWriteProperty() { return readyToWrite; }

    public void setResult(double value) { this.result = value; }
    @Override
    public String getResult() { return String.valueOf(result); }

    @Override
    public void clear() {
        setBusy(false);
        setOperation("");
        setVj("");
        setVk("");
        setQj("");
        setQk("");
        setCycles(0);
        setReadyToWrite(false);
        setResult(0.0);
    }


}