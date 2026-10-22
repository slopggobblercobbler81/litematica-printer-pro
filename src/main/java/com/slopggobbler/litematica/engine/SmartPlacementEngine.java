package com.slopggobbler.litematica.engine;

import com.slopggobbler.litematica.compat.LitematicaBridge;
import com.slopggobbler.litematica.config.PrinterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * High-performance, intelligent auto-placement engine for Minecraft 26.3.
 */
public class SmartPlacementEngine {
    private final InventoryCacheManager inventoryCache = new InventoryCacheManager();
    private int tickCounter = 0;
    private int cooldown = 0;

    private int totalPlacedBlocks = 0;
    private int queueSize = 0;
    private int lastSecondPlacedCount = 0;
    private int currentSecondPlacedCounter = 0;
    private long lastSecondTimestamp = System.currentTimeMillis();
    private final Set<Item> missingItems = new HashSet<>();

    public void onClientTick(Minecraft client) {
        tickCounter++;

        long now = System.currentTimeMillis();
        if (now - lastSecondTimestamp >= 1000L) {
            lastSecondPlacedCount = currentSecondPlacedCounter;
            currentSecondPlacedCounter = 0;
            lastSecondTimestamp = now;
        }

        PrinterConfig config = PrinterConfig.getInstance();
        if (!config.isPrinterEnabled()) {
            queueSize = 0;
            return;
        }

        if (client.level == null || client.player == null || client.gameMode == null) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        executePlacementCycle(client);
        cooldown = config.getPlacementIntervalTicks();
    }

    private void executePlacementCycle(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel world = client.level;
        PrinterConfig config = PrinterConfig.getInstance();

        inventoryCache.updateCache(player, tickCounter);
        synchronized (missingItems) {
            missingItems.clear();
        }

        List<PlacementCandidate> candidates = scanCandidates(client, player, world, config);
        this.queueSize = candidates.size();

        if (candidates.isEmpty()) {
            return;
        }

        Vec3 eyePos = player.getEyePosition();
        candidates.sort(Comparator
                .comparingInt((PlacementCandidate c) -> c.pos.getY())
                .thenComparingDouble(c -> getSquaredDistance(c.pos, eyePos))
        );

        int placedThisTick = 0;
        int maxPerTick = config.getBlocksPerTick();

        for (PlacementCandidate candidate : candidates) {
            if (placedThisTick >= maxPerTick) {
                break;
            }

            if (!inventoryCache.ensureHeldItem(client, player, candidate.schematicState)) {
                Item needed = inventoryCache.getRequiredItem(candidate.schematicState);
                if (needed != null) {
                    synchronized (missingItems) {
                        missingItems.add(needed);
                    }
                }
                continue;
            }

            BlockHitResult hitResult = BlockRotationHelper.calculatePlacementHit(
                    candidate.pos,
                    candidate.schematicState,
                    config.isCarpetAccuratePlacement()
            );

            if (config.isStrictRaycast()) {
                if (!RaycastValidator.hasLineOfSight(world, player, candidate.pos, hitResult.getLocation())) {
                    continue;
                }
            }

            client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
            player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);

            placedThisTick++;
            totalPlacedBlocks++;
            currentSecondPlacedCounter++;
        }
    }

    private List<PlacementCandidate> scanCandidates(
            Minecraft client,
            LocalPlayer player,
            ClientLevel world,
            PrinterConfig config
    ) {
        List<PlacementCandidate> list = new ArrayList<>();
        double range = config.getPlacementRange();
        double rangeSq = range * range;
        BlockPos playerPos = player.blockPosition();
        int r = (int) Math.ceil(range);

        AABB playerBox = player.getBoundingBox();
        Vec3 eyePos = player.getEyePosition();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (getSquaredDistance(pos, eyePos) > rangeSq) {
                        continue;
                    }

                    BlockState schematicState = LitematicaBridge.getSchematicBlockState(world, pos);
                    if (schematicState.isAir()) {
                        continue;
                    }

                    BlockState worldState = world.getBlockState(pos);
                    if (worldState.equals(schematicState)) {
                        continue;
                    }

                    if (!worldState.isAir() && !worldState.canBeReplaced()) {
                        continue;
                    }

                    AABB blockBox = new AABB(pos);
                    if (!schematicState.isAir() && playerBox.intersects(blockBox)) {
                        continue;
                    }

                    if (!hasRequiredSupport(world, pos, schematicState)) {
                        continue;
                    }

                    list.add(new PlacementCandidate(pos, schematicState));
                }
            }
        }

        return list;
    }

    private boolean hasRequiredSupport(ClientLevel world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        if (block instanceof TorchBlock && !(block instanceof WallTorchBlock)
                || block instanceof RedstoneWireBlock
                || block instanceof BaseRailBlock
                || block instanceof DiodeBlock) {
            BlockPos down = pos.below();
            BlockState downState = world.getBlockState(down);
            return downState.isFaceSturdy(world, down, Direction.UP);
        }

        if (block instanceof WallTorchBlock || block instanceof LadderBlock) {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos supportPos = pos.relative(facing.getOpposite());
            BlockState supportState = world.getBlockState(supportPos);
            return supportState.isFaceSturdy(world, supportPos, facing);
        }

        return true;
    }

    public static class PlacementCandidate {
        public final BlockPos pos;
        public final BlockState schematicState;

        public PlacementCandidate(BlockPos pos, BlockState schematicState) {
            this.pos = pos;
            this.schematicState = schematicState;
        }
    }

    public int getTotalPlacedBlocks() { return totalPlacedBlocks; }
    public int getQueueSize() { return queueSize; }
    public int getBlocksPerSecond() { return lastSecondPlacedCount; }
    public Set<Item> getMissingItems() {
        synchronized (missingItems) {
            return new HashSet<>(missingItems);
        }
    }
    public void resetTotal() { this.totalPlacedBlocks = 0; }

    public void reset() {
        this.queueSize = 0;
        this.cooldown = 0;
        this.lastSecondPlacedCount = 0;
        this.currentSecondPlacedCounter = 0;
        synchronized (this.missingItems) {
            this.missingItems.clear();
        }
        this.inventoryCache.clear();
    }

    private static double getSquaredDistance(BlockPos pos, Vec3 vec) {
        double dx = (pos.getX() + 0.5D) - vec.x();
        double dy = (pos.getY() + 0.5D) - vec.y();
        double dz = (pos.getZ() + 0.5D) - vec.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
