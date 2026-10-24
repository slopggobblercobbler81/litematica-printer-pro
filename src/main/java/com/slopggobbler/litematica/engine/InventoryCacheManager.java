package com.slopggobbler.litematica.engine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * High-performance inventory slot caching and hotbar buffer manager on Minecraft 26.3.
 * Synchronizes carried item packets with the server to prevent slot desynchronization
 * and incorrect block placement.
 */
public class InventoryCacheManager {
    private final Map<Item, Integer> hotbarSlotCache = new HashMap<>();
    private final Map<Item, Integer> inventorySlotCache = new HashMap<>();
    private final Map<Item, Integer> shulkerBoxCache = new HashMap<>();
    private int lastScanTick = -1;

    public void updateCache(LocalPlayer player, int currentTick) {
        if (currentTick >= 0 && lastScanTick == currentTick) {
            return;
        }
        lastScanTick = currentTick;

        hotbarSlotCache.clear();
        inventorySlotCache.clear();
        shulkerBoxCache.clear();

        Inventory inv = player.getInventory();

        // Scan hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                hotbarSlotCache.putIfAbsent(stack.getItem(), i);
            }
        }

        // Scan main inventory (slots 9-35)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                inventorySlotCache.putIfAbsent(stack.getItem(), i);
                inspectShulkerBox(stack);
            }
        }
    }

    private void inspectShulkerBox(ItemStack stack) {
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            container.nonEmptyItemCopyStream().forEach(subStack -> {
                if (!subStack.isEmpty()) {
                    shulkerBoxCache.merge(subStack.getItem(), subStack.getCount(), Integer::sum);
                }
            });
        }
    }

    public Item getRequiredItem(BlockState state) {
        if (state == null) return null;
        Block block = state.getBlock();
        return block.asItem();
    }

    public boolean ensureHeldItem(Minecraft client, LocalPlayer player, BlockState state) {
        Item requiredItem = getRequiredItem(state);
        if (requiredItem == null) {
            return false;
        }

        if (player.getMainHandItem().is(requiredItem)) {
            return true;
        }

        // Check if the item is present in hotbar (slots 0-8)
        if (hotbarSlotCache.containsKey(requiredItem)) {
            int targetHotbarSlot = hotbarSlotCache.get(requiredItem);
            if (player.getInventory().getSelectedSlot() != targetHotbarSlot) {
                player.getInventory().setSelectedSlot(targetHotbarSlot);
                if (player.connection != null) {
                    player.connection.send(new ServerboundSetCarriedItemPacket(targetHotbarSlot));
                }
            }
            return player.getMainHandItem().is(requiredItem);
        }

        // Check if item needs to be swapped into hotbar from main inventory
        if (inventorySlotCache.containsKey(requiredItem)) {
            int invSlot = inventorySlotCache.get(requiredItem);
            int currentHotbar = player.getInventory().getSelectedSlot();
            swapToHotbar(client, player, invSlot, currentHotbar);
            // Invalidate cache and yield this tick so server has time to acknowledge container swap
            updateCache(player, -1);
            return false;
        }

        return false;
    }

    private void swapToHotbar(Minecraft client, LocalPlayer player, int fromSlot, int hotbarTarget) {
        if (client.gameMode == null) return;

        int syncId = player.containerMenu.containerId;
        client.gameMode.handleContainerInput(
                syncId,
                fromSlot,
                hotbarTarget,
                ContainerInput.SWAP,
                player
        );
    }

    public boolean hasItem(BlockState state) {
        Item item = getRequiredItem(state);
        return item != null && (hotbarSlotCache.containsKey(item) || inventorySlotCache.containsKey(item));
    }

    public int getShulkerStockCount(Item item) {
        return shulkerBoxCache.getOrDefault(item, 0);
    }

    public void clear() {
        hotbarSlotCache.clear();
        inventorySlotCache.clear();
        shulkerBoxCache.clear();
        lastScanTick = -1;
    }
}
