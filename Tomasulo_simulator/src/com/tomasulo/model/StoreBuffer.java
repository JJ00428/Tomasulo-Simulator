package com.tomasulo.model;

import javafx.beans.property.*;

public class StoreBuffer implements ExecutionUnit {
    private final StringProperty name;
    private final BooleanProperty busy;
    private final IntegerProperty address;
    private final DoubleProperty value;
    private final StringProperty q;
    private final IntegerProperty cycles;
    private final BooleanProperty readyToWrite;

    public StoreBuffer(String name) {
        this.name = new SimpleStringProperty(name);
        this.busy = new SimpleBooleanProperty(false);
        this.address = new SimpleIntegerProperty(0);
        this.value = new SimpleDoubleProperty(0.0);
        this.q = new SimpleStringProperty("");
        this.cycles = new SimpleIntegerProperty(0);
        this.readyToWrite = new SimpleBooleanProperty(false);
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

    public double getValue() { return value.get(); }
    public void setValue(double value) { this.value.set(value); }
    public DoubleProperty valueProperty() { return value; }

    public String getQ() { return q.get(); }
    public void setQ(String value) { q.set(value); }
    public StringProperty qProperty() { return q; }

    public int getCycles() { return cycles.get(); }
    public void setCycles(int value) { cycles.set(value); }
    public IntegerProperty cyclesProperty() { return cycles; }

    @Override
    public boolean isReadyToWrite() { return readyToWrite.get(); }
    public void setReadyToWrite(boolean value) { readyToWrite.set(value); }
    public BooleanProperty readyToWriteProperty() { return readyToWrite; }

    @Override
    public void clear() {
        setBusy(false);
        setAddress(0);
        setValue(0.0);
        setQ("");
        setCycles(0);
        setReadyToWrite(false);
    }

    @Override
    public String getResult() { return String.valueOf(getValue()); }
    @Override
    public String getQj() { return getQ(); }
    @Override
    public String getQk() { return ""; }
    @Override
    public void setVj(String value) { setValue(Double.parseDouble(value)); }
    @Override
    public void setVk(String value) { /* Not applicable for StoreBuffer */ }
    @Override
    public void setQj(String value) { setQ(value); }
    @Override
    public void setQk(String value) { /* Not applicable for StoreBuffer */ }
}