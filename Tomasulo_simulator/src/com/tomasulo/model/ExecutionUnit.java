package com.tomasulo.model;

public interface ExecutionUnit {
    String getName();
    boolean isReadyToWrite();

    InstructionEntry getInstruction();

    void clear();
    String getQj();
    String getQk();

    boolean isBusy();

    void setVj(String value);
    void setVk(String value);
    void setQj(String value);
    void setQk(String value);
    String getResult();
}