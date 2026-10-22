package com.slopggobbler.litematica.gui;

import com.slopggobbler.litematica.config.PrinterConfig;
import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import fi.dy.masa.malilib.util.GuiUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.Set;

/**
 * Modern HUD overlay showing real-time placement stats and missing materials for Minecraft 26.3.
 */
public class PrinterHudOverlay {
    private final SmartPlacementEngine engine;

    public PrinterHudOverlay(SmartPlacementEngine engine) {
        this.engine = engine;
    }

    public void render(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        try {
            PrinterConfig config = PrinterConfig.getInstance();
            if (!config.isShowHud()) {
                return;
            }

            Minecraft client = Minecraft.getInstance();
            if (GuiUtils.getCurrentScreen() != null) {
                return;
            }

            Font font = client.font;
            int x = config.getHudPosX();
            int y = config.getHudPosY();

            boolean enabled = config.isPrinterEnabled();
            int queue = engine.getQueueSize();
            int bps = engine.getBlocksPerSecond();
            int bpt = config.getBlocksPerTick();

            // Defensive snapshot of missing items
            Set<Item> missing = engine.getMissingItems();

            // Background translucent plate
            int bgWidth = 145;
            int bgHeight = enabled && !missing.isEmpty() ? 44 : 32;
            extractor.fill(x - 3, y - 3, x + bgWidth, y + bgHeight, 0x88000000);
            extractor.fill(x - 3, y - 3, x - 1, y + bgHeight, enabled ? 0xFF00FF88 : 0xFF777777);

            // Header status
            String statusText = enabled ? "§a● PRINTER ACTIVE" : "§7○ PRINTER STANDBY";
            extractor.text(font, Component.literal(statusText), x + 3, y, 0xFFFFFF, true);

            // Subtitle stats
            String statsText = String.format("§fQueue: §e%d §7| §fRate: §b%dbps §8(%dbpt)", queue, bps, bpt);
            extractor.text(font, Component.literal(statsText), x + 3, y + 12, 0xDDDDDD, true);

            // Missing items warning
            if (enabled && !missing.isEmpty()) {
                Item firstMissing = missing.iterator().next();
                String itemName = (firstMissing != null) ? firstMissing.getName(firstMissing.getDefaultInstance()).getString() : "Unknown";
                String missingText = "§cMissing: §f" + itemName;
                if (missing.size() > 1) {
                    missingText += " §7(+" + (missing.size() - 1) + " more)";
                }
                extractor.text(font, Component.literal(missingText), x + 3, y + 24, 0xFFAAAA, true);
            }
        } catch (Throwable t) {
            // Defensive boundary prevents HUD rendering exceptions from affecting client
        }
    }
}
