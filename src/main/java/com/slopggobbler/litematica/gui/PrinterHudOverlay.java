package com.slopggobbler.litematica.gui;

import com.slopggobbler.litematica.config.PrinterConfig;
import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

import java.util.Set;

/**
 * Modern HUD overlay showing real-time placement stats and missing materials.
 */
public class PrinterHudOverlay {
    private final SmartPlacementEngine engine;

    public PrinterHudOverlay(SmartPlacementEngine engine) {
        this.engine = engine;
    }

    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        try {
            PrinterConfig config = PrinterConfig.getInstance();
            if (!config.isShowHud()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden || client.currentScreen != null) {
                return;
            }

            TextRenderer tr = client.textRenderer;
            int x = config.getHudPosX();
            int y = config.getHudPosY();

            boolean enabled = config.isPrinterEnabled();
            int queue = engine.getQueueSize();
            int bps = engine.getBlocksPerSecond();
            int bpt = config.getBlocksPerTick();

            // Defensive snapshot of missing items to prevent concurrent modification or race conditions
            Set<Item> missing = engine.getMissingItems();

            // Background translucent plate
            int bgWidth = 145;
            int bgHeight = enabled && !missing.isEmpty() ? 44 : 32;
            drawContext.fill(x - 3, y - 3, x + bgWidth, y + bgHeight, 0x88000000);
            drawContext.fill(x - 3, y - 3, x - 1, y + bgHeight, enabled ? 0xFF00FF88 : 0xFF777777);

            // Header status
            String statusText = enabled ? "§a● PRINTER ACTIVE" : "§7○ PRINTER STANDBY";
            drawContext.drawText(tr, Text.literal(statusText), x + 3, y, 0xFFFFFF, true);

            // Subtitle stats
            String statsText = String.format("§fQueue: §e%d §7| §fRate: §b%dbps §8(%dbpt)", queue, bps, bpt);
            drawContext.drawText(tr, Text.literal(statsText), x + 3, y + 12, 0xDDDDDD, true);

            // Missing items warning
            if (enabled && !missing.isEmpty()) {
                Item firstMissing = missing.iterator().next();
                String itemName = (firstMissing != null && firstMissing.getName() != null) ? firstMissing.getName().getString() : "Unknown";
                String missingText = "§cMissing: §f" + itemName;
                if (missing.size() > 1) {
                    missingText += " §7(+" + (missing.size() - 1) + " more)";
                }
                drawContext.drawText(tr, Text.literal(missingText), x + 3, y + 24, 0xFFAAAA, true);
            }
        } catch (Throwable t) {
            // Defensive boundary prevents unexpected HUD rendering exceptions from crashing Minecraft
        }
    }
}
