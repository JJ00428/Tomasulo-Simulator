package com.tomasulo.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Memory {
    private byte[] memory;
    private final int size;
    private ObservableList<MemoryEntry> memoryEntries;

    public Memory(int sizeInBytes) {
        this.size = sizeInBytes;
        this.memory = new byte[sizeInBytes];
        initializeMemoryEntries();
    }

    private void initializeMemoryEntries() {
        memoryEntries = FXCollections.observableArrayList();
        for (int i = 0; i < size; i++) {
            memoryEntries.add(new MemoryEntry(i, memory[i]));
        }
    }

    // Single byte operations
    public byte readByte(int address) {
        validateAddress(address);
        return memory[address];
    }

    public void writeByte(int address, byte value) {
        validateAddress(address);
        memory[address] = value;
        memoryEntries.get(address).setValue(value);
    }

    // Word operations (4 bytes)
    public int readWord(int address) {
        validateWordAddress(address);
        return ((memory[address] & 0xFF) << 24) |
                ((memory[address + 1] & 0xFF) << 16) |
                ((memory[address + 2] & 0xFF) << 8) |
                (memory[address + 3] & 0xFF);
    }

    public void writeWord(int address, int value) {
        validateWordAddress(address);
        memory[address] = (byte) (value >>> 24);
        memory[address + 1] = (byte) (value >>> 16);
        memory[address + 2] = (byte) (value >>> 8);
        memory[address + 3] = (byte) value;

        // Update display entries
        for (int i = 0; i < 4; i++) {
            memoryEntries.get(address + i).setValue(memory[address + i]);
        }
    }

    // Half word operations (2 bytes)
    public short readHalfWord(int address) {
        validateHalfWordAddress(address);
        return (short) (((memory[address] & 0xFF) << 8) |
                (memory[address + 1] & 0xFF));
    }

    public void writeHalfWord(int address, short value) {
        validateHalfWordAddress(address);
        memory[address] = (byte) (value >>> 8);
        memory[address + 1] = (byte) value;

        // Update display entries
        memoryEntries.get(address).setValue(memory[address]);
        memoryEntries.get(address + 1).setValue(memory[address + 1]);
    }

    // Float operations (4 bytes)
    public float readFloat(int address) {
        validateWordAddress(address);
        int bits = readWord(address);
        return Float.intBitsToFloat(bits);
    }

    public void writeFloat(int address, float value) {
        validateWordAddress(address);
        int bits = Float.floatToIntBits(value);
        writeWord(address, bits);
    }

    // Double operations (8 bytes)
    public double readDouble(int address) {
        validateDoubleAddress(address);
        long bits = ((long) readWord(address) << 32) |
                (readWord(address + 4) & 0xFFFFFFFFL);
        return Double.longBitsToDouble(bits);
    }

    public void writeDouble(int address, double value) {
        validateDoubleAddress(address);
        long bits = Double.doubleToLongBits(value);
        writeWord(address, (int) (bits >>> 32));
        writeWord(address + 4, (int) bits);
    }

    // Address validation methods
    private void validateAddress(int address) {
        if (address < 0 || address >= size) {
            throw new IllegalArgumentException("Invalid memory address: " + address);
        }
    }

    private void validateWordAddress(int address) {
        validateAddress(address);
        validateAddress(address + 3);
        if (address % 4 != 0) {
            throw new IllegalArgumentException("Word address must be aligned to 4 bytes: " + address);
        }
    }

    private void validateHalfWordAddress(int address) {
        validateAddress(address);
        validateAddress(address + 1);
        if (address % 2 != 0) {
            throw new IllegalArgumentException("Half word address must be aligned to 2 bytes: " + address);
        }
    }

    private void validateDoubleAddress(int address) {
        validateAddress(address);
        validateAddress(address + 7);
        if (address % 8 != 0) {
            throw new IllegalArgumentException("Double address must be aligned to 8 bytes: " + address);
        }
    }

    // Utility methods
    public void clear() {
        for (int i = 0; i < size; i++) {
            writeByte(i, (byte) 0);
        }
    }

    public int getSize() {
        return size;
    }

    public ObservableList<MemoryEntry> getMemoryEntries() {
        return memoryEntries;
    }

    // Inner class for memory display
    public static class MemoryEntry {
        private final SimpleIntegerProperty address;
        private final SimpleStringProperty hexValue;
        private final SimpleStringProperty ascii;

        public MemoryEntry(int address, byte value) {
            this.address = new SimpleIntegerProperty(address);
            this.hexValue = new SimpleStringProperty(String.format("%02X", value & 0xFF));
            this.ascii = new SimpleStringProperty(isPrintable(value) ? String.valueOf((char)value) : ".");
        }

        private boolean isPrintable(byte b) {
            return b >= 32 && b <= 126;
        }

        public void setValue(byte value) {
            hexValue.set(String.format("%02X", value & 0xFF));
            ascii.set(isPrintable(value) ? String.valueOf((char)value) : ".");
        }

        // Getters for JavaFX properties
        public int getAddress() { return address.get(); }
        public String getHexValue() { return hexValue.get(); }
        public String getAscii() { return ascii.get(); }

        // Property getters for TableView
        public SimpleIntegerProperty addressProperty() { return address; }
        public SimpleStringProperty hexValueProperty() { return hexValue; }
        public SimpleStringProperty asciiProperty() { return ascii; }
    }
}