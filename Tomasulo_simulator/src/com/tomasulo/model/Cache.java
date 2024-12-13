package com.tomasulo.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.ScatterChart;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Cache {
    // TODO: have minimum size for cache block to fit double word
    private final int size;
    private final int blockSize;
    private final int hitLatency;
    private final int missLatency;
    //    private final Map<Integer, CacheBlock> blocks;
    private final CacheBlock[] blocks;
    private int indexBits;
    private final Memory memory;
    private ObservableList<CacheEntry> cacheEntries;

    public Cache(int size, int blockSize, int hitLatency, int missLatency, Memory memory) {
        this.size = size;
        this.blockSize = blockSize;
        this.hitLatency = hitLatency;
        this.missLatency = missLatency;
        int numBlocks = size / blockSize;
        this.blocks = new CacheBlock[numBlocks];
        this.memory = memory;
        // think about case where indexBits are zero (size == blockSize)
        // think about case where number of blocks is not a power of 2 (shouldn't be a case)
        while((1 << indexBits) < numBlocks){
            indexBits++;
        }
        for(int i = 0; i < blocks.length; i++){
            blocks[i] = new CacheBlock(blockSize);
        }
        initializeMemoryEntries();
        // if size isn't divisible by blockSize, the remainder of the division is discarded
        // Therefore part of the cache is unused
    }
    private void initializeMemoryEntries() {
        cacheEntries = FXCollections.observableArrayList();
        for (int i = 0; i < blocks.length; i++) {
            cacheEntries.add(new CacheEntry(i, blocks[i]));
        }
    }
    private boolean isBusy(int address){
        int temp = address / blockSize;
        int blockIndex = temp % (1 << indexBits);
        return blocks[blockIndex].busy;
    }
    private boolean isOccupier(int address, String occupierID){
        int temp = address / blockSize;
        int blockIndex = temp % (1 << indexBits);
        return !blocks[blockIndex].busy || Objects.equals(occupierID, blocks[blockIndex].occupier);
    }
    private void setOccupier(int address, String occupierID){
        int temp = address / blockSize;
        int blockIndex = temp % (1 << indexBits);
        blocks[blockIndex].busy = true;
        blocks[blockIndex].occupier = occupierID;
        blocks[blockIndex].remainingCycles = getAccessTime(address);
    }
    private void unOccupy(int address){
        int temp = address / blockSize;
        int blockIndex = temp % (1 << indexBits);
        blocks[blockIndex].busy = false;
    }
    public boolean isHit(int address) {
        // optimize later
        int temp = address / blockSize;
        int blockIndex = temp % (1 << indexBits);
        int tag = temp / (1 << indexBits);
        return blocks[blockIndex].valid && blocks[blockIndex].tag == tag;
    }
    public boolean requestWord(int address, String occupierID){
        validateWordAddress(address);
        if(!isOccupier(address, occupierID))
            return false;
        if(!isBusy(address)){
            setOccupier(address, occupierID);
        }

        int temp = address / blockSize;
        int blockIndex = temp % (1 << indexBits);
        blocks[blockIndex].remainingCycles--;
        if(blocks[blockIndex].remainingCycles == 0){
            unOccupy(address);
            return true;
        }
        return false;
    }
    public boolean requestDouble(int address, String occupierID){
        validateDoubleAddress(address);
        return requestWord(address, occupierID);
    }
    // Assume data is always aligned (words are placed at addresses divisible by 4
    // and doubles are place at addresses divisible by 8)
    // NB: only use read or write after finishing cycles needed for execution
    public int readWord(int address){
        validateWordAddress(address);

//        blocks[blockIndex].valid = true;
//        blocks[blockIndex].tag = temp / (1 << indexBits);
//        blocks[blockIndex].elements[blockOffset] = memory.readByte(address);
//        blocks[blockIndex].elements[blockOffset + 1] = memory.readByte(address + 1);
//        blocks[blockIndex].elements[blockOffset + 2] = memory.readByte(address + 2);
//        blocks[blockIndex].elements[blockOffset + 3] = memory.readByte(address + 3);

        return memory.readWord(address);
    }
    public void writeWord(int address, int value){
        validateWordAddress(address);

//        blocks[blockIndex].valid = true;
//        blocks[blockIndex].tag = temp / (1 << indexBits);
//        blocks[blockIndex].elements[blockOffset] = (byte) (value >>> 24);
//        blocks[blockIndex].elements[blockOffset + 1] = (byte) (value >>> 16);
//        blocks[blockIndex].elements[blockOffset + 2] = (byte) (value >>> 8);
//        blocks[blockIndex].elements[blockOffset + 3] = (byte) value;

        memory.writeWord(address, value);

    }
    public long readLong(int address){
        validateDoubleAddress(address);
        return ((long) readWord(address) << 32) |
                (readWord(address + 4) & 0xFFFFFFFFL);
    }
    public void writeLong(int address, long value) {
        validateDoubleAddress(address);
        writeWord(address, (int) (value >>> 32));
        writeWord(address + 4, (int) value);
    }
    public float readFloat(int address){
        validateWordAddress(address);
        int bits = readWord(address);
        return Float.intBitsToFloat(bits);
    }
    public void writeFloat(int address, float value) {
        validateWordAddress(address);
        int bits = Float.floatToIntBits(value);
        writeWord(address, bits);
    }
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
    public int getAccessTime(int address) {
        return isHit(address) ? hitLatency : missLatency;
    }
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
    private void validateDoubleAddress(int address) {
        validateAddress(address);
        validateAddress(address + 7);
        if (address % 8 != 0) {
            throw new IllegalArgumentException("Double address must be aligned to 8 bytes: " + address);
        }
    }
    public void clear(){
        for(int i = 0; i < blocks.length; i++){
            blocks[i] = new CacheBlock(blockSize);
        }
    }

    // New getter methods
    public int getSize() { return size; }
    public int getBlockSize() { return blockSize; }
    public int getHitLatency() { return hitLatency; }
    public int getMissLatency() { return missLatency; }
    public ObservableList<CacheEntry> getCacheEntries() {
        return cacheEntries;
    }

    private static class CacheBlock {
        public boolean valid;
        public int tag;
        public String occupier;
        public boolean busy;
        public byte[] elements;
        public int remainingCycles;

        public CacheBlock(int blockSize) {
            valid = false;
            tag = 0;
            elements = new byte[blockSize];
        }
    }
    public static class CacheEntry {
        private final SimpleIntegerProperty blockIndex;
        private final SimpleBooleanProperty valid;
        private final SimpleIntegerProperty tag;
        private final SimpleStringProperty hexValues;


//        private final SimpleStringProperty ascii;

        public CacheEntry(int address, CacheBlock cacheBlock) {
            this.blockIndex = new SimpleIntegerProperty(address);
            this.hexValues = new SimpleStringProperty("");
            for(byte element : cacheBlock.elements){
                hexValues.set(hexValues.get() + String.format("%02X", element) + ", ");
            }
            this.valid = new SimpleBooleanProperty(cacheBlock.valid);
            this.tag = new SimpleIntegerProperty(cacheBlock.tag);
//            this.ascii = new SimpleStringProperty(isPrintable(value) ? String.valueOf((char)value) : ".");
        }

        private boolean isPrintable(byte b) {
            return b >= 32 && b <= 126;
        }

        public void setValue(byte[] elements) {
            hexValues.set("");
            for(byte element : elements){
                hexValues.set(hexValues.get() + ", " +  String.format("%02X", element));
            }
//            ascii.set(isPrintable(value) ? String.valueOf((char)value) : ".");
        }
        public int getBlockIndex() {
            return blockIndex.get();
        }

        public SimpleIntegerProperty blockIndexProperty() {
            return blockIndex;
        }

        public boolean isValid() {
            return valid.get();
        }

        public SimpleBooleanProperty validProperty() {
            return valid;
        }

        public int getTag() {
            return tag.get();
        }

        public SimpleIntegerProperty tagProperty() {
            return tag;
        }
        public String getHexValues() {
            return hexValues.get();
        }

        public SimpleStringProperty hexValuesProperty() {
            return hexValues;
        }

    }
}