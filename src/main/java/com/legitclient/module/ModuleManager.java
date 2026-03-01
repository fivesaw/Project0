package com.legitclient.module;

import com.legitclient.LegitClient;
import com.legitclient.utils.OptimizationManager;
import com.legitclient.utils.TickOptimizer.TickPriority;
import com.legitclient.module.visual.FPSBoost;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<Module>();
    private OptimizationManager optimizationManager;

    public void registerAll() {
        optimizationManager = OptimizationManager.getInstance();
        if (optimizationManager == null) {
            optimizationManager = new OptimizationManager();
        }

        registerCombatModules();
        registerMovementModules();
        registerVisualModules();
        registerUtilityModules();

        optimizationManager.enableOptimizations();
    }

    private void registerCombatModules() {
    }

    private void registerMovementModules() {
    }

    private void registerVisualModules() {
        register(new FPSBoost());
    }

    private void registerUtilityModules() {
    }

    public void register(Module module) {
        modules.add(module);

        if (optimizationManager != null) {
            TickPriority priority = module.getTickPriority();
            optimizationManager.getTickOptimizer().registerModule(module, priority);
        }
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> moduleClass) {
        for (Module module : modules) {
            if (moduleClass.isInstance(module)) {
                return (T) module;
            }
        }
        return null;
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        List<Module> result = new ArrayList<Module>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (optimizationManager != null && optimizationManager.isOptimizationsEnabled()) {
            optimizationManager.onTick();
        } else {
            for (Module module : modules) {
                if (module.isEnabled()) {
                    try {
                        module.onTick();
                    } catch (Exception e) {
                        LegitClient.LOGGER.error("Error in module {}: {}", module.getName(), e.getMessage());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (optimizationManager != null) {
            optimizationManager.onRenderTick();
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public int getEnabledCount() {
        int count = 0;
        for (Module module : modules) {
            if (module.isEnabled()) count++;
        }
        return count;
    }

    public void disableAll() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.toggle();
            }
        }
    }
}
