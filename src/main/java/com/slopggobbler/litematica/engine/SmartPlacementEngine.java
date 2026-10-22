package com.slopggobbler.litematica.engine;

import com.slopggobbler.litematica.compat.LitematicaBridge;
import com.slopggobbler.litematica.config.PrinterConfig;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * High-performance, intelligent auto-placement engine.
 * Handles rate limiting, topological dependency ordering, collision checks,
 * inventory swapping, and Carpet accurate placement protocol execution.
 */
public class SmartPlacementEngine {
    private final InventoryCacheManager inventoryCache = new InventoryCacheManager();
    private int tickCounter = 0;
    private int cooldown = 0;

    // Real-time statistics
    private int totalPlacedBlocks = 0;
    private int queueSize = 0;
    private int lastSecondPlacedCount = 0;
    private int currentSecondPlacedCounter = 0;
    private long lastSecondTimestamp = System.currentTimeMillis();
    private final Set<Item> missingItems = new HashSet<>();

    public void onClientTick(MinecraftClient client) {
        tickCounter++;

        // Update 1-second BPS rolling window
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

        if (client.world == null || client.player == null || client.interactionManager == null) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        executePlacementCycle(client);
        cooldown = config.getPlacementIntervalTicks();
    }

    private void executePlacementCycle(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        PrinterConfig config = PrinterConfig.getInstance();

        inventoryCache.updateCache(player, tickCounter);
        synchronized (missingItems) {
            missingItems.clear();
        }

        // 1. Scan and collect placement candidates
        List<PlacementCandidate> candidates = scanCandidates(client, player, world, config);
        this.queueSize = candidates.size();

        if (candidates.isEmpty()) {
            return;
        }

        // 2. Sort topologically: Bottom-up (Y ascending), then distance to player
        Vec3d eyePos = player.getEyePos();
        candidates.sort(Comparator
                .comparingInt((PlacementCandidate c) -> c.pos.getY())
                .thenComparingDouble(c -> getSquaredDistance(c.pos, eyePos))
        );

        // 3. Execute placement burst
        int placedThisTick = 0;
        int maxPerTick = config.getBlocksPerTick();

        for (PlacementCandidate candidate : candidates) {
            if (placedThisTick >= maxPerTick) {
                break;
            }

            // Ensure required item in hand
            if (!inventoryCache.ensureHeldItem(client, player, candidate.schematicState)) {
                Item needed = inventoryCache.getRequiredItem(candidate.schematicState);
                if (needed != null) {
                    synchronized (missingItems) {
                        missingItems.add(needed);
                    }
                }
                continue;
            }

            // Calculate precise BlockHitResult
            BlockHitResult hitResult = BlockRotationHelper.calculatePlacementHit(
                    candidate.pos,
                    candidate.schematicState,
                    config.isCarpetAccuratePlacement()
            );

            // Strict raycast check if enabled
            if (config.isStrictRaycast()) {
                if (!RaycastValidator.hasLineOfSight(world, player, candidate.pos, hitResult.getPos())) {
                    continue;
                }
            }

            // Dispatch interaction
            client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
            player.swingHand(Hand.MAIN_HAND);

            placedThisTick++;
            totalPlacedBlocks++;
            currentSecondPlacedCounter++;
        }
    }

    /**
     * Scans for blocks within reach range that need to be placed.
     */
    private List<PlacementCandidate> scanCandidates(
            MinecraftClient client,
            ClientPlayerEntity player,
            ClientWorld world,
            PrinterConfig config
    ) {
        List<PlacementCandidate> list = new ArrayList<>();
        double range = config.getPlacementRange();
        double rangeSq = range * range;
        BlockPos playerPos = player.getBlockPos();
        int r = (int) Math.ceil(range);

        Box playerBox = player.getBoundingBox();
        Vec3d eyePos = player.getEyePos();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    if (getSquaredDistance(pos, eyePos) > rangeSq) {
                        continue;
                    }

                    // Check schematic
                    BlockState schematicState = LitematicaBridge.getSchematicBlockState(world, pos);
                    if (schematicState.isAir()) {
                        continue;
                    }

                    BlockState worldState = world.getBlockState(pos);
                    if (worldState.equals(schematicState)) {
                        continue; // Already placed correctly
                    }

                    // Can we place here?
                    if (!worldState.isAir() && !worldState.isReplaceable()) {
                        // Obstructed
                        continue;
                    }

                    // Check player collision to avoid suffocation
                    Box blockBox = new Box(pos);
                    if (schematicState.isSolidBlock(world, pos) && playerBox.intersects(blockBox)) {
                        continue;
                    }

                    // Check structural support for dependent blocks (torches, redstone, rails)
                    if (!hasRequiredSupport(world, pos, schematicState)) {
                        continue;
                    }

                    list.add(new PlacementCandidate(pos, schematicState));
                }
            }
        }

        return list;
    }

    /**
     * Verifies that blocks requiring support (dust, torches, repeaters) have a solid base.
     */
    private boolean hasRequiredSupport(ClientWorld world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        // Floor-supported blocks
        if (block instanceof TorchBlock && !(block instanceof WallTorchBlock)
                || block instanceof RedstoneWireBlock
                || block instanceof AbstractRailBlock
                || block instanceof AbstractRedstoneGateBlock) {
            BlockPos down = pos.down();
            BlockState downState = world.getBlockState(down);
            return downState.isSideSolidFullSquare(world, down, Direction.UP);
        }

        // Wall mounted blocks
        if (block instanceof WallTorchBlock || block instanceof LadderBlock) {
            Direction facing = state.get(HorizontalFacingBlock.FACING);
            BlockPos supportPos = pos.offset(facing.getOpposite());
            BlockState supportState = world.getBlockState(supportPos);
            return supportState.isSideSolidFullSquare(world, supportPos, facing);
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

    // Diagnostics & HUD metrics
    public int getTotalPlacedBlocks() { return totalPlacedBlocks; }
    public int getQueueSize() { return queueSize; }
    public int getBlocksPerSecond() { return lastSecondPlacedCount; }
    public Set<Item> getMissingItems() {
        synchronized (missingItems) {
            return new HashSet<>(missingItems);
        }
    }
    public void resetTotal() { this.totalPlacedBlocks = 0; }

    /**
     * Resets placement queue metrics, rolling counters, and inventory slot caches.
     */
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

    private static double getSquaredDistance(BlockPos pos, Vec3d vec) {
        double dx = (pos.getX() + 0.5D) - vec.getX();
        double dy = (pos.getY() + 0.5D) - vec.getY();
        double dz = (pos.getZ() + 0.5D) - vec.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
