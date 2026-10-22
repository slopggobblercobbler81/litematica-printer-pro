package com.slopggobbler.litematica.engine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
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
 */
public class InventoryCacheManager {
    private final Map<Item, Integer> hotbarSlotCache = new HashMap<>();
    private final Map<Item, Integer> inventorySlotCache = new HashMap<>();
    private final Map<Item, Integer> shulkerBoxCache = new HashMap<>();
    private int lastScanTick = -1;

    public void updateCache(LocalPlayer player, int currentTick) {
        if (lastScanTick == currentTick) {
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

        if (hotbarSlotCache.containsKey(requiredItem)) {
            int targetHotbarSlot = hotbarSlotCache.get(requiredItem);
            player.getInventory().setSelectedSlot(targetHotbarSlot);
            return true;
        }

        if (inventorySlotCache.containsKey(requiredItem)) {
            int invSlot = inventorySlotCache.get(requiredItem);
            swapToHotbar(client, player, invSlot, player.getInventory().getSelectedSlot());
            return true;
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
        return hotbarSlotCache.containsKey(item) || inventorySlotCache.containsKey(item);
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
