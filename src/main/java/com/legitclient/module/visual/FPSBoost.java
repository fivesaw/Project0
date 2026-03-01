package com.legitclient.module.visual;

import com.legitclient.LegitClient;
import com.legitclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class FPSBoost extends Module {

    private static class OriginalSettings {
        boolean vSync;
        boolean fancyGraphics;
        boolean clouds;
        int renderDistanceChunks;
        int particleSetting;
        boolean mipmapLevels;
        boolean anisotropicFiltering;
        int fpsLimit;
    }

    private OriginalSettings originalSettings;
    private boolean settingsApplied;

    public FPSBoost() {
        super("FPSBoost", Category.VISUAL);
        this.originalSettings = null;
        this.settingsApplied = false;
    }

    @Override
    public void onEnable() {
        LegitClient.LOGGER.info("FPSBoost enabled - applying optimizations");
        saveOriginalSettings();
        applyOptimizations();
    }

    @Override
    public void onDisable() {
        LegitClient.LOGGER.info("FPSBoost disabled - restoring original settings");
        restoreOriginalSettings();
    }

    private void saveOriginalSettings() {
        Minecraft mc = Minecraft.getMinecraft();
        GameSettings settings = mc.gameSettings;

        originalSettings = new OriginalSettings();
        originalSettings.vSync = settings.enableVsync;
        originalSettings.fancyGraphics = settings.fancyGraphics;
        originalSettings.clouds = settings.clouds;
        originalSettings.renderDistanceChunks = settings.renderDistanceChunks;
        originalSettings.particleSetting = settings.particleSetting;
        originalSettings.mipmapLevels = settings.mipmapLevels > 0;
        originalSettings.anisotropicFiltering = settings.anisotropicFiltering;
        originalSettings.fpsLimit = settings.limitFramerate;
    }

    private void applyOptimizations() {
        Minecraft mc = Minecraft.getMinecraft();
        GameSettings settings = mc.gameSettings;

        settings.enableVsync = false;
        settings.fancyGraphics = false;
        settings.clouds = false;
        settings.particleSetting = 2;
        settings.mipmapLevels = 0;
        settings.anisotropicFiltering = false;
        settings.limitFramerate = 260;

        if (settings.renderDistanceChunks > 10) {
            settings.renderDistanceChunks = 10;
        }

        settings.saveOptions();
        settingsApplied = true;

        mc.updateDisplay();
    }

    private void restoreOriginalSettings() {
        if (originalSettings == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        GameSettings settings = mc.gameSettings;

        settings.enableVsync = originalSettings.vSync;
        settings.fancyGraphics = originalSettings.fancyGraphics;
        settings.clouds = originalSettings.clouds;
        settings.renderDistanceChunks = originalSettings.renderDistanceChunks;
        settings.particleSetting = originalSettings.particleSetting;
        settings.mipmapLevels = originalSettings.mipmapLevels ? 4 : 0;
        settings.anisotropicFiltering = originalSettings.anisotropicFiltering;
        settings.limitFramerate = originalSettings.fpsLimit;

        settings.saveOptions();
        settingsApplied = false;

        mc.updateDisplay();
    }

    public boolean isSettingsApplied() {
        return settingsApplied;
    }

    public static void applyMinimalOptimizations() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.enableVsync) {
            mc.gameSettings.enableVsync = false;
            mc.gameSettings.saveOptions();
        }
    }

    public static int getRecommendedRenderDistance(int currentFps) {
        if (currentFps >= 144) {
            return 12;
        } else if (currentFps >= 60) {
            return 10;
        } else if (currentFps >= 30) {
            return 8;
        } else {
            return 6;
        }
    }
}
