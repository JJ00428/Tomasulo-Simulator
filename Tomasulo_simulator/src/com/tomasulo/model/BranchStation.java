package com.tomasulo.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BranchStation extends ReservationStation {
    private final StringProperty target;
    public BranchStation(String name) {
        super(name);
        this.target = new SimpleStringProperty("");
    }

    public String getTarget() { return target.get(); }
    public void setTarget(String value) { target.set(value); }
    public StringProperty targetProperty() { return target; }
}

