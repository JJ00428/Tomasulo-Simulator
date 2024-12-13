package com.tomasulo.model;

import java.util.HashMap;
import java.util.Map;

public class RegisterFile<T extends Number> {
    private final Map<String, String> values;
    private final Map<String, String> status;
    private boolean isInt;
    public RegisterFile(int size, boolean isInt) {
        values = new HashMap<>();
        status = new HashMap<>();
        this.isInt = isInt;
        for (int i = 0; i < size; i++) {
            String regName = "F" + i;
            values.put(regName, "0.0");
            if(isInt){
                regName = "R" + i;
                values.put(regName, "0");
            }

            status.put(regName, "");
        }
    }
    public void clearAll(){
        for (int i = 0; i < values.size(); i++) {
            String regName = "F" + i;
            values.put(regName, "0.0");
            if(isInt){
                regName = "R" + i;
                values.put(regName, "0");
            }

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

    public void clearStatus(String tag, String value) {
        for (Map.Entry<String, String> entry : status.entrySet()) {
            if (entry.getValue().equals(tag)) {
                entry.setValue("");
                if(isInt){
                    int tmp = (int)Double.parseDouble(value);
                    value = Integer.toString(tmp);
                }
                values.put(entry.getKey(), value);
            }
        }

    }
}