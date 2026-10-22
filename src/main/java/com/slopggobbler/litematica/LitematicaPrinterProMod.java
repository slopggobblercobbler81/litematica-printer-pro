package com.slopggobbler.litematica;

import com.slopggobbler.litematica.config.PrinterConfig;
import com.slopggobbler.litematica.config.PrinterConfigHandler;
import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import com.slopggobbler.litematica.event.ClientKeybindHandler;
import com.slopggobbler.litematica.gui.PrinterHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main client entrypoint for Litematica Printer Pro.
 * High-performance Litematica companion mod adding smart auto-placement,
 * accurate block rotation resolution, and inventory buffer caching.
 */
public class LitematicaPrinterProMod implements ClientModInitializer {
    public static final String MOD_ID = "litematica_printer_pro";
    public static final String MOD_NAME = "Litematica Printer Pro";
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

            // Safely register PrinterConfigHandler with MaLiLib ConfigManager if available via reflection
            try {
                Class<?> configManagerClass = Class.forName("fi.dy.masa.malilib.config.ConfigManager");
                Object configManagerInstance = configManagerClass.getMethod("getInstance").invoke(null);
                Class<?> handlerClass = Class.forName("com.slopggobbler.litematica.config.PrinterConfigHandler");
                Object handlerInstance = handlerClass.getMethod("getInstance").invoke(null);
                Class<?> iConfigHandlerClass = Class.forName("fi.dy.masa.malilib.config.IConfigHandler");
                configManagerClass.getMethod("registerConfigHandler", String.class, iConfigHandlerClass)
                        .invoke(configManagerInstance, MOD_ID, handlerInstance);
                LOGGER.info("[{}] Registered PrinterConfigHandler with MaLiLib ConfigManager.", MOD_NAME);
            } catch (Throwable t) {
                LOGGER.debug("[{}] MaLiLib ConfigManager integration deferred/unavailable: {}", MOD_NAME, t.getMessage());
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
                if (client.world != null && client.player != null) {
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

            ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
                if (this.placementEngine != null) {
                    LOGGER.info("[{}] Resetting placement engine & inventory cache on world transition.", MOD_NAME);
                    this.placementEngine.reset();
                }
            });

            // Register HUD rendering with defensive exception boundary
            HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
                if (this.hudOverlay != null) {
                    try {
                        this.hudOverlay.render(drawContext, renderTickCounter);
                    } catch (Throwable t) {
                        LOGGER.debug("[{}] Render HUD overlay exception: {}", MOD_NAME, t.getMessage());
                    }
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
