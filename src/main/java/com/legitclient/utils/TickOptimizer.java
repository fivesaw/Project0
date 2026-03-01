package com.legitclient.utils;

import com.legitclient.module.Module;
import com.legitclient.LegitClient;

import java.util.ArrayList;
import java.util.List;

public class TickOptimizer {

    public enum TickPriority {
        CRITICAL(0),
        HIGH(1),
        NORMAL(2),
        LOW(3),
        BACKGROUND(4);

        private final int level;

        TickPriority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    private static final int SKIP_THRESHOLD_FPS = 30;
    private static final int MAX_SKIP_FRAMES = 3;

    private final List<Module>[] priorityQueues;
    private int currentTick;
    private int skippedFrames;
    private long lastTickTime;
    private long averageTickTime;

    @SuppressWarnings("unchecked")
    public TickOptimizer() {
        this.priorityQueues = new ArrayList[5];
        for (int i = 0; i < 5; i++) {
            priorityQueues[i] = new ArrayList<Module>();
        }
        this.currentTick = 0;
        this.skippedFrames = 0;
        this.lastTickTime = System.nanoTime();
        this.averageTickTime = 0L;
    }

    public void registerModule(Module module, TickPriority priority) {
        priorityQueues[priority.getLevel()].add(module);
    }

    public void unregisterModule(Module module) {
        for (List<Module> queue : priorityQueues) {
            queue.remove(module);
        }
    }

    public void onTick() {
        long tickStart = System.nanoTime();
        currentTick++;

        processCriticalModules();
        processHighPriorityModules();

        int fps = getEstimatedFPS();
        if (fps >= SKIP_THRESHOLD_FPS || skippedFrames >= MAX_SKIP_FRAMES) {
            processNormalModules();
            processLowPriorityModules();
            skippedFrames = 0;
        } else {
            skippedFrames++;
        }

        if (currentTick % 20 == 0) {
            processBackgroundModules();
        }

        long tickEnd = System.nanoTime();
        updateTickStats(tickEnd - tickStart);
    }

    private void processCriticalModules() {
        for (Module module : priorityQueues[TickPriority.CRITICAL.getLevel()]) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    LegitClient.LOGGER.error("Error in module {}: {}", module.getName(), e.getMessage());
                }
            }
        }
    }

    private void processHighPriorityModules() {
        for (Module module : priorityQueues[TickPriority.HIGH.getLevel()]) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    LegitClient.LOGGER.error("Error in module {}: {}", module.getName(), e.getMessage());
                }
            }
        }
    }

    private void processNormalModules() {
        for (Module module : priorityQueues[TickPriority.NORMAL.getLevel()]) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    LegitClient.LOGGER.error("Error in module {}: {}", module.getName(), e.getMessage());
                }
            }
        }
    }

    private void processLowPriorityModules() {
        for (Module module : priorityQueues[TickPriority.LOW.getLevel()]) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    LegitClient.LOGGER.error("Error in module {}: {}", module.getName(), e.getMessage());
                }
            }
        }
    }

    private void processBackgroundModules() {
        for (Module module : priorityQueues[TickPriority.BACKGROUND.getLevel()]) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    LegitClient.LOGGER.error("Error in module {}: {}", module.getName(), e.getMessage());
                }
            }
        }
    }

    private int getEstimatedFPS() {
        if (averageTickTime > 0) {
            return (int) (1_000_000_000L / averageTickTime);
        }
        return 60;
    }

    private void updateTickStats(long tickTime) {
        if (averageTickTime == 0) {
            averageTickTime = tickTime;
        } else {
            averageTickTime = (averageTickTime * 9 + tickTime) / 10;
        }
    }

    public void reset() {
        currentTick = 0;
        skippedFrames = 0;
        lastTickTime = System.nanoTime();
        averageTickTime = 0L;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public long getAverageTickTimeNanos() {
        return averageTickTime;
    }

    public double getAverageTickTimeMs() {
        return averageTickTime / 1_000_000.0;
    }

    public boolean shouldSkipLowPriority() {
        return skippedFrames < MAX_SKIP_FRAMES && getEstimatedFPS() < SKIP_THRESHOLD_FPS;
    }
}
