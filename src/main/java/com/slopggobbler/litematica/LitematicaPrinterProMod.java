package com.slopggobbler.litematica;

import com.slopggobbler.litematica.config.PrinterConfig;
import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import com.slopggobbler.litematica.event.ClientKeybindHandler;
import com.slopggobbler.litematica.gui.PrinterHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for Litematica Printer Pro.
 * High-performance Litematica companion mod adding smart auto-placement,
 * accurate block rotation resolution, and inventory buffer caching.
 */
public class LitematicaPrinterProMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "litematica_printer_pro";
    public static final String MOD_NAME = "Litematica Printer Pro";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static LitematicaPrinterProMod instance;
    private SmartPlacementEngine placementEngine;
    private PrinterHudOverlay hudOverlay;

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("[{}] Initializing common components (v{})", MOD_NAME, MOD_VERSION);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing Fabric client integration...", MOD_NAME);

        // Load configuration
        PrinterConfig.getInstance().loadConfig();

        // Initialize engines
        this.placementEngine = new SmartPlacementEngine();
        this.hudOverlay = new PrinterHudOverlay(this.placementEngine);

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                this.placementEngine.onClientTick(client);
            }
        });

        // Register Keybindings
        ClientKeybindHandler.register(this.placementEngine);

        // Register HUD rendering
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            if (this.hudOverlay != null) {
                this.hudOverlay.render(drawContext, renderTickCounter);
            }
        });

        LOGGER.info("[{}] Successfully loaded and ready!", MOD_NAME);
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
