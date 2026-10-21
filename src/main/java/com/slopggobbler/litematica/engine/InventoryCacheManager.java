package com.slopggobbler.litematica.engine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

/**
 * High-performance inventory slot caching and hotbar buffer manager.
 * Reduces inventory scan overhead during rapid placement loops.
 */
public class InventoryCacheManager {
    private final Map<Item, Integer> hotbarSlotCache = new HashMap<>();
    private final Map<Item, Integer> inventorySlotCache = new HashMap<>();
    private final Map<Item, Integer> shulkerBoxCache = new HashMap<>();
    private int lastScanTick = -1;

    /**
     * Refreshes the inventory slot cache if dirty or on a new tick.
     */
    public void updateCache(ClientPlayerEntity player, int currentTick) {
        if (lastScanTick == currentTick) {
            return;
        }
        lastScanTick = currentTick;

        hotbarSlotCache.clear();
        inventorySlotCache.clear();
        shulkerBoxCache.clear();

        PlayerInventory inv = player.getInventory();

        // Scan hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                hotbarSlotCache.putIfAbsent(stack.getItem(), i);
            }
        }

        // Scan main inventory (slots 9-35)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                inventorySlotCache.putIfAbsent(stack.getItem(), i);
                inspectShulkerBox(stack);
            }
        }
    }

    /**
     * Inspects container components (e.g. Shulker Boxes) for nested items.
     */
    private void inspectShulkerBox(ItemStack stack) {
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            container.stream().forEach(subStack -> {
                if (!subStack.isEmpty()) {
                    shulkerBoxCache.merge(subStack.getItem(), subStack.getCount(), Integer::sum);
                }
            });
        }
    }

    /**
     * Finds the corresponding Item for the required BlockState.
     */
    public Item getRequiredItem(BlockState state) {
        Block block = state.getBlock();
        Item item = block.asItem();
        return item;
    }

    /**
     * Ensures the player is holding the required item in main hand.
     * Swaps from hotbar or main inventory if necessary.
     *
     * @return true if player now holds the required item in main hand.
     */
    public boolean ensureHeldItem(MinecraftClient client, ClientPlayerEntity player, BlockState state) {
        Item requiredItem = getRequiredItem(state);
        if (requiredItem == null) {
            return false;
        }

        // Check if currently holding it in main hand
        if (player.getMainHandStack().isOf(requiredItem)) {
            return true;
        }

        // Check hotbar cache
        if (hotbarSlotCache.containsKey(requiredItem)) {
            int targetHotbarSlot = hotbarSlotCache.get(requiredItem);
            player.getInventory().selectedSlot = targetHotbarSlot;
            return true;
        }

        // Check main inventory cache and swap into current hotbar slot
        if (inventorySlotCache.containsKey(requiredItem)) {
            int invSlot = inventorySlotCache.get(requiredItem);
            swapToHotbar(client, player, invSlot, player.getInventory().selectedSlot);
            return true;
        }

        return false;
    }

    /**
     * Swaps an inventory slot with the active hotbar slot via client interaction manager.
     */
    private void swapToHotbar(MinecraftClient client, ClientPlayerEntity player, int fromSlot, int hotbarTarget) {
        if (client.interactionManager == null) return;

        // Protocol slot index for 3x9 inventory starts at 9
        int syncId = player.currentScreenHandler.syncId;
        client.interactionManager.clickSlot(
                syncId,
                fromSlot,
                hotbarTarget,
                SlotActionType.SWAP,
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

    /**
     * Clears cached slot mappings and container contents.
     */
    public void clear() {
        hotbarSlotCache.clear();
        inventorySlotCache.clear();
        shulkerBoxCache.clear();
        lastScanTick = -1;
    }
}
