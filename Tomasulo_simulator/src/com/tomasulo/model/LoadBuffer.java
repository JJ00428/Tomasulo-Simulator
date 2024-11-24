package com.tomasulo.model;

import javafx.beans.property.*;

public class LoadBuffer implements ExecutionUnit {
    private final StringProperty name;
    private final BooleanProperty busy;
    private final IntegerProperty address;
    private final IntegerProperty cycles;
    private final BooleanProperty readyToWrite;
    private double result;

    public LoadBuffer(String name) {
        this.name = new SimpleStringProperty(name);
        this.busy = new SimpleBooleanProperty(false);
        this.address = new SimpleIntegerProperty(0);
        this.cycles = new SimpleIntegerProperty(0);
        this.readyToWrite = new SimpleBooleanProperty(false);
        this.result = 0.0;
    }

    @Override
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public boolean isBusy() { return busy.get(); }
    public void setBusy(boolean value) { busy.set(value); }
    public BooleanProperty busyProperty() { return busy; }

    public int getAddress() { return address.get(); }
    public void setAddress(int value) { address.set(value); }
    public IntegerProperty addressProperty() { return address; }

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
        setAddress(0);
        setCycles(0);
        setReadyToWrite(false);
        setResult(0.0);
    }

    @Override
    public String getQj() { return ""; }
    @Override
    public String getQk() { return ""; }
    @Override
    public void setVj(String value) { /* Not applicable for LoadBuffer */ }
    @Override
    public void setVk(String value) { /* Not applicable for LoadBuffer */ }
    @Override
    public void setQj(String value) { /* Not applicable for LoadBuffer */ }
    @Override
    public void setQk(String value) { /* Not applicable for LoadBuffer */ }
}