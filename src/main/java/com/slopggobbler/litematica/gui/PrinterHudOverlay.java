package com.slopggobbler.litematica.gui;

import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Screen HUD indicator removed to prevent visual clutter and maximize screen visibility.
 * Status and speed adjustments are cleanly presented via vanilla actionbar notifications.
 */
public class PrinterHudOverlay {
    public PrinterHudOverlay(SmartPlacementEngine engine) {
    }

    public void render(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        // No-op: Persistent top-left UI indicator eliminated per user preference
    }
}
