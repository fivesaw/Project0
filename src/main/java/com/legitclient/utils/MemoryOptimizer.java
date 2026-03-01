package com.legitclient.utils;

import com.legitclient.LegitClient;
import net.minecraft.util.Vec3;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryOptimizer {

    private static final int MAX_POOL_SIZE = 256;
    private static final int GC_HINT_INTERVAL_MS = 30000;

    private final Queue<Vec3> vec3Pool;
    private final AtomicInteger poolHits;
    private final AtomicInteger poolMisses;
    private long lastGCHintTime;

    public MemoryOptimizer() {
        this.vec3Pool = new ConcurrentLinkedQueue<Vec3>();
        this.poolHits = new AtomicInteger(0);
        this.poolMisses = new AtomicInteger(0);
        this.lastGCHintTime = 0L;
    }

    public Vec3 getVec3(double x, double y, double z) {
        Vec3 vec = vec3Pool.poll();
        if (vec != null) {
            poolHits.incrementAndGet();
            return new Vec3(x, y, z);
        }
        poolMisses.incrementAndGet();
        return new Vec3(x, y, z);
    }

    public void returnVec3(Vec3 vec) {
        if (vec != null && vec3Pool.size() < MAX_POOL_SIZE) {
            vec3Pool.offer(vec);
        }
    }

    public void performMaintenance() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastGCHintTime >= GC_HINT_INTERVAL_MS) {
            suggestGC();
            lastGCHintTime = currentTime;
        }

        if (vec3Pool.size() > MAX_POOL_SIZE / 2) {
            int toRemove = vec3Pool.size() - MAX_POOL_SIZE / 4;
            for (int i = 0; i < toRemove; i++) {
                vec3Pool.poll();
            }
        }
    }

    private void suggestGC() {
        Runtime runtime = Runtime.getRuntime();
        long usedBefore = runtime.totalMemory() - runtime.freeMemory();
        long usedMB = usedBefore / (1024 * 1024);

        if (usedMB > 512) {
            runtime.runFinalization();
            System.gc();

            long usedAfter = runtime.totalMemory() - runtime.freeMemory();
            long freed = usedBefore - usedAfter;
            LegitClient.LOGGER.debug("Memory optimization: freed {} MB", freed / (1024 * 1024));
        }
    }

    public void clearPools() {
        vec3Pool.clear();
        poolHits.set(0);
        poolMisses.set(0);
    }

    public int getPoolSize() {
        return vec3Pool.size();
    }

    public int getPoolHits() {
        return poolHits.get();
    }

    public int getPoolMisses() {
        return poolMisses.get();
    }

    public double getPoolHitRate() {
        int hits = poolHits.get();
        int misses = poolMisses.get();
        int total = hits + misses;
        if (total == 0) return 0.0;
        return (double) hits / total * 100.0;
    }

    public MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return new MemoryInfo(
                usedMemory / (1024 * 1024),
                freeMemory / (1024 * 1024),
                totalMemory / (1024 * 1024),
                maxMemory / (1024 * 1024)
        );
    }

    public static class MemoryInfo {
        public final long usedMB;
        public final long freeMB;
        public final long totalMB;
        public final long maxMB;

        public MemoryInfo(long usedMB, long freeMB, long totalMB, long maxMB) {
            this.usedMB = usedMB;
            this.freeMB = freeMB;
            this.totalMB = totalMB;
            this.maxMB = maxMB;
        }

        public double getUsagePercent() {
            if (maxMB == 0) return 0.0;
            return (double) usedMB / maxMB * 100.0;
        }
    }
}
