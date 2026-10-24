package com.slopggobbler.litematica;

import com.slopggobbler.litematica.config.PrinterConfig;
import com.slopggobbler.litematica.config.PrinterConfigHandler;
import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import com.slopggobbler.litematica.event.ClientKeybindHandler;
import com.slopggobbler.litematica.gui.PrinterHudOverlay;
import fi.dy.masa.malilib.config.gui.GuiModConfigs;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main client entrypoint for Litematica Printer Pro on Minecraft 26.3.
 * High-performance Litematica companion mod adding smart auto-placement,
 * accurate block rotation resolution, and inventory buffer caching.
 */
public class LitematicaPrinterProMod implements ClientModInitializer {
    public static final String MOD_ID = "litebuilder";
    public static final String MOD_NAME = "LiteBuilder";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static LitematicaPrinterProMod instance;
    private SmartPlacementEngine placementEngine;
    private PrinterHudOverlay hudOverlay;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] Initializing Fabric client integration (v{})...", MOD_NAME, MOD_VERSION);

        try {
            // Load configuration
            PrinterConfig.getInstance().loadConfig();

            // Register PrinterConfigHandler & Config Screen with MaLiLib if available
            try {
                fi.dy.masa.malilib.config.ConfigManager.getInstance().registerConfigHandler(MOD_ID, PrinterConfigHandler.getInstance());
                PrinterConfigHandler.getInstance().syncFromInternal();
                Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(MOD_ID, MOD_NAME, () ->
                        new GuiModConfigs(MOD_ID, PrinterConfigHandler.OPTIONS, MOD_NAME)));
                LOGGER.info("[{}] Registered PrinterConfigHandler & ConfigScreen with MaLiLib.", MOD_NAME);
            } catch (Throwable t) {
                LOGGER.debug("[{}] MaLiLib Config integration deferred/unavailable: {}", MOD_NAME, t.getMessage());
            }

            // Initialize engines
            this.placementEngine = new SmartPlacementEngine();
            this.hudOverlay = new PrinterHudOverlay(this.placementEngine);

            // Register Keybindings
            ClientKeybindHandler.register();

            // Register unified client tick event (keybinds polled before placement engine tick; paused check)
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.isPaused()) {
                    return;
                }
                if (client.level != null && client.player != null) {
                    ClientKeybindHandler.onClientTick(client);
                    this.placementEngine.onClientTick(client);
                }
            });

            // Register world transition listeners to reset inventory cache and queue stats
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                if (this.placementEngine != null) {
                    LOGGER.info("[{}] Resetting placement engine & inventory cache on server disconnect.", MOD_NAME);
                    this.placementEngine.reset();
                }
            });

            LOGGER.info("[{}] Successfully loaded and ready!", MOD_NAME);
        } catch (Throwable t) {
            LOGGER.error("[{}] Critical error during client initialization: {}", MOD_NAME, t.getMessage(), t);
        }
    }

    public static LitematicaPrinterProMod getInstance() {
        return instance;
    }

    public SmartPlacementEngine getPlacementEngine() {
        return this.placementEngine;
    }

    public PrinterHudOverlay getHudOverlay() {
        return this.hudOverlay;
    }
}
