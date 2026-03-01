package com.legitclient;

import com.legitclient.module.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = LegitClient.MOD_ID, name = LegitClient.MOD_NAME, version = LegitClient.VERSION, clientSideOnly = true)
public class LegitClient {

    public static final String MOD_ID = "legitclient";
    public static final String MOD_NAME = "Legit Client";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static LegitClient INSTANCE;

    private ModuleManager moduleManager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} {} pre-initializing...", MOD_NAME, VERSION);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("{} {} initializing...", MOD_NAME, VERSION);
        moduleManager = new ModuleManager();
        moduleManager.registerAll();
        MinecraftForge.EVENT_BUS.register(moduleManager);
        LOGGER.info("{} loaded {} modules.", MOD_NAME, moduleManager.getModules().size());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("{} {} post-initialization complete.", MOD_NAME, VERSION);
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
