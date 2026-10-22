package com.slopggobbler.litematica.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.slopggobbler.litematica.config.PrinterConfig;
import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import fi.dy.masa.malilib.util.GuiUtils;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Handles client keybinds for toggling printer and adjusting settings on the fly for Minecraft 26.3.
 */
public class ClientKeybindHandler {
    private static KeyMapping togglePrinterKey;
    private static KeyMapping cycleSpeedKey;

    public static void register() {
        togglePrinterKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.litematica_printer_pro.toggle_printer",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_V,
                KeyMapping.Category.MISC
        ));

        cycleSpeedKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.litematica_printer_pro.cycle_speed",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_B,
                KeyMapping.Category.MISC
        ));
    }

    public static void register(SmartPlacementEngine engine) {
        register();
    }

    /**
     * Polls keybinding presses before placement engine processing.
     */
    public static void onClientTick(Minecraft client) {
        if (client.player == null || GuiUtils.getCurrentScreen() != null) return;

        while (togglePrinterKey != null && togglePrinterKey.consumeClick()) {
            PrinterConfig config = PrinterConfig.getInstance();
            boolean newState = !config.isPrinterEnabled();
            config.setPrinterEnabled(newState);

            if (newState) {
                client.player.sendOverlayMessage(
                        Component.literal("§a[LiteBuilder]§r Placement ENABLED (§b" + config.getBlocksPerTick() + "§r blocks/tick)")
                );
            } else {
                client.player.sendOverlayMessage(
                        Component.literal("§7[LiteBuilder]§r Placement DISABLED")
                );
            }
        }

        while (cycleSpeedKey != null && cycleSpeedKey.consumeClick()) {
            PrinterConfig config = PrinterConfig.getInstance();
            int current = config.getBlocksPerTick();
            int next = switch (current) {
                case 1 -> 3;
                case 3 -> 5;
                case 5 -> 10;
                default -> 1;
            };
            config.setBlocksPerTick(next);
            client.player.sendOverlayMessage(
                    Component.literal("§6[LiteBuilder]§r Placement speed set to §b" + next + "§r blocks/tick")
            );
        }
    }
}
