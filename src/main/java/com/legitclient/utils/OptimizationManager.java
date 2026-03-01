package com.legitclient.utils;

import com.legitclient.LegitClient;
import net.minecraft.client.Minecraft;

public class OptimizationManager {

    private static OptimizationManager instance;

    private final RenderCache renderCache;
    private final TickOptimizer tickOptimizer;
    private final MemoryOptimizer memoryOptimizer;

    private boolean optimizationsEnabled;
    private long lastOptimizationTime;

    public OptimizationManager() {
        instance = this;
        this.renderCache = new RenderCache();
        this.tickOptimizer = new TickOptimizer();
        this.memoryOptimizer = new MemoryOptimizer();
        this.optimizationsEnabled = false;
        this.lastOptimizationTime = 0L;
    }

    public static OptimizationManager getInstance() {
        return instance;
    }

    public void enableOptimizations() {
        if (optimizationsEnabled) return;
        optimizationsEnabled = true;
        LegitClient.LOGGER.info("Client optimizations enabled");
    }

    public void disableOptimizations() {
        if (!optimizationsEnabled) return;
        optimizationsEnabled = false;
        LegitClient.LOGGER.info("Client optimizations disabled");
    }

    public void onTick() {
        if (!optimizationsEnabled) return;

        long currentTime = System.currentTimeMillis();
        tickOptimizer.onTick();

        if (currentTime - lastOptimizationTime >= 5000) {
            memoryOptimizer.performMaintenance();
            lastOptimizationTime = currentTime;
        }
    }

    public void onRenderTick() {
        if (!optimizationsEnabled) return;
        renderCache.onRenderTick();
    }

    public void invalidateRenderCache() {
        renderCache.invalidate();
    }

    public void onWorldChange() {
        renderCache.fullInvalidate();
        tickOptimizer.reset();
        memoryOptimizer.clearPools();
    }

    public RenderCache getRenderCache() {
        return renderCache;
    }

    public TickOptimizer getTickOptimizer() {
        return tickOptimizer;
    }

    public MemoryOptimizer getMemoryOptimizer() {
        return memoryOptimizer;
    }

    public boolean isOptimizationsEnabled() {
        return optimizationsEnabled;
    }

    public int getFPS() {
        return Minecraft.getMinecraft().getDebugFPS();
    }

    public long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        return used / (1024 * 1024);
    }
}
