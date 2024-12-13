package com.tomasulo.model;

import java.util.HashMap;
import java.util.Map;

public class RegisterFile {
    private final Map<String, String> values;
    private final Map<String, String> status;

    public RegisterFile(int size) {
        values = new HashMap<>();
        status = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String regName = "F" + i;
            values.put(regName, "0.0");
            status.put(regName, "");
        }
    }

    public String getValue(String register) {
        return values.getOrDefault(register, "0.0");
    }

    public void setValue(String register, String value) {
        values.put(register, value);
    }

    public String getStatus(String register) {
        return status.getOrDefault(register, "");
    }

    public void setStatus(String register, String statusValue) {
        status.put(register, statusValue);
    }

    public void clearStatus(String register) {
        status.put(register, "");
    }
}