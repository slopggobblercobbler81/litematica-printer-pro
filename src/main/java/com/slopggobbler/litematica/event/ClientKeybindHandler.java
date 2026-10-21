package com.slopggobbler.litematica.event;

import com.slopggobbler.litematica.config.PrinterConfig;
import com.slopggobbler.litematica.engine.SmartPlacementEngine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Handles client keybinds for toggling printer and adjusting settings on the fly.
 */
public class ClientKeybindHandler {
    private static final String KEY_CATEGORY = "key.litematica_printer_pro.category";

    private static KeyBinding togglePrinterKey;
    private static KeyBinding cycleSpeedKey;

    public static void register() {
        togglePrinterKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.litematica_printer_pro.toggle_printer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORY
        ));

        cycleSpeedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.litematica_printer_pro.cycle_speed",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KEY_CATEGORY
        ));
    }

    public static void register(SmartPlacementEngine engine) {
        register();
    }

    /**
     * Polls keybinding presses before placement engine processing.
     */
    public static void onClientTick(net.minecraft.client.MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;

        while (togglePrinterKey != null && togglePrinterKey.wasPressed()) {
            PrinterConfig config = PrinterConfig.getInstance();
            boolean newState = !config.isPrinterEnabled();
            config.setPrinterEnabled(newState);

            if (newState) {
                client.player.sendMessage(
                        Text.translatable("litematica_printer_pro.msg.printer_enabled", config.getBlocksPerTick()),
                        true
                );
            } else {
                client.player.sendMessage(
                        Text.translatable("litematica_printer_pro.msg.printer_disabled"),
                        true
                );
            }
        }

        while (cycleSpeedKey != null && cycleSpeedKey.wasPressed()) {
            PrinterConfig config = PrinterConfig.getInstance();
            int current = config.getBlocksPerTick();
            int next = switch (current) {
                case 1 -> 3;
                case 3 -> 5;
                case 5 -> 10;
                default -> 1;
            };
            config.setBlocksPerTick(next);
            client.player.sendMessage(
                    Text.literal("§6[PrinterPro]§r Placement speed set to §b" + next + "§r blocks/tick"),
                    true
            );
        }
    }
}
