package com.tomasulo.model;

import java.util.HashMap;
import java.util.Map;

public class Cache {
    private final int size;
    private final int blockSize;
    private final int hitLatency;
    private final int missLatency;
    private final Map<Integer, CacheBlock> blocks;

    public Cache(int size, int blockSize, int hitLatency, int missLatency) {
        this.size = size;
        this.blockSize = blockSize;
        this.hitLatency = hitLatency;
        this.missLatency = missLatency;
        this.blocks = new HashMap<>();
    }

    public boolean isHit(int address) {
        int blockNumber = address / blockSize;
        return blocks.containsKey(blockNumber);
    }

    public int getAccessTime(int address) {
        return isHit(address) ? hitLatency : missLatency;
    }

    public void accessMemory(int address) {
        int blockNumber = address / blockSize;
        if (!isHit(address)) {
            if (blocks.size() >= size) {
                // Implement cache replacement policy (e.g., LRU)
                blocks.remove(blocks.keySet().iterator().next());
            }
            blocks.put(blockNumber, new CacheBlock(blockNumber));
        }
        blocks.get(blockNumber).setLastAccessed(System.currentTimeMillis());
    }

    // New getter methods
    public int getSize() { return size; }
    public int getBlockSize() { return blockSize; }
    public int getHitLatency() { return hitLatency; }
    public int getMissLatency() { return missLatency; }

    private static class CacheBlock {
        private final int blockNumber;
        private long lastAccessed;

        public CacheBlock(int blockNumber) {
            this.blockNumber = blockNumber;
            this.lastAccessed = System.currentTimeMillis();
        }

        public void setLastAccessed(long time) {
            this.lastAccessed = time;
        }
    }
}