package com.slopggobbler.litematica.engine;

import com.slopggobbler.litematica.compat.LitematicaBridge;
import com.slopggobbler.litematica.config.PrinterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * High-performance, intelligent auto-placement engine for Minecraft 26.3.
 * Supports smart orientation for repeaters/comparators/observers/pistons/stairs,
 * in-place block state tuning (repeaters, comparators, daylight detectors, trapdoors,
 * fence gates, doors, levers, noteblocks), and item-batched placement.
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

    public enum ActionType {
        PLACE,
        ADJUST_STATE,
        BREAK_WRONG
    }

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
        Item currentHeldItem = player.getMainHandItem().getItem();

        // Sort candidates:
        // 1. BREAK_WRONG first (clear obstructions / wrong orientation)
        // 2. ADJUST_STATE next (tune placed repeaters, note blocks, comparators, etc.)
        // 3. Candidates matching current held item first (minimizes slot swapping)
        // 4. Grouped by item ID so all blocks of the same item are placed in batches
        // 5. Stable vertical ordering (bottom-up) then Euclidean distance
        candidates.sort((c1, c2) -> {
            if (c1.actionType != c2.actionType) {
                return Integer.compare(actionPriority(c1.actionType), actionPriority(c2.actionType));
            }

            if (c1.actionType == ActionType.ADJUST_STATE || c1.actionType == ActionType.BREAK_WRONG) {
                return Double.compare(getSquaredDistance(c1.pos, eyePos), getSquaredDistance(c2.pos, eyePos));
            }

            Item item1 = inventoryCache.getRequiredItem(c1.schematicState);
            Item item2 = inventoryCache.getRequiredItem(c2.schematicState);

            boolean c1MatchesHeld = (item1 != null && item1 == currentHeldItem);
            boolean c2MatchesHeld = (item2 != null && item2 == currentHeldItem);

            if (c1MatchesHeld != c2MatchesHeld) {
                return c1MatchesHeld ? -1 : 1;
            }

            int id1 = (item1 != null) ? Item.getId(item1) : 0;
            int id2 = (item2 != null) ? Item.getId(item2) : 0;
            if (id1 != id2) {
                return Integer.compare(id1, id2);
            }

            int yCompare = Integer.compare(c1.pos.getY(), c2.pos.getY());
            if (yCompare != 0) {
                return yCompare;
            }

            return Double.compare(getSquaredDistance(c1.pos, eyePos), getSquaredDistance(c2.pos, eyePos));
        });

        int placedThisTick = 0;
        int maxPerTick = config.getBlocksPerTick();
        Item activeItemThisTick = null;

        for (PlacementCandidate candidate : candidates) {
            if (placedThisTick >= maxPerTick) {
                break;
            }

            // 1. Break wrong / obstructing blocks
            if (candidate.actionType == ActionType.BREAK_WRONG) {
                if (!config.isBreakWrongBlocks()) {
                    continue;
                }
                BlockState curWorld = world.getBlockState(candidate.pos);
                if (curWorld.isAir()) {
                    continue;
                }
                client.gameMode.destroyBlock(candidate.pos);
                player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
                placedThisTick++;
                continue;
            }

            // 2. Adjust block state in world (Repeaters, Comparators, Daylight Detectors, Note Blocks, Trapdoors, etc.)
            if (candidate.actionType == ActionType.ADJUST_STATE) {
                BlockState curWorldState = world.getBlockState(candidate.pos);
                int clicksNeeded = getClicksToMatchState(curWorldState, candidate.schematicState, config.isTuneNoteBlocks());
                if (clicksNeeded <= 0) {
                    continue;
                }

                int clicks = Math.min(clicksNeeded, Math.max(1, maxPerTick - placedThisTick));
                BlockHitResult hit = new BlockHitResult(
                        new Vec3(candidate.pos.getX() + 0.5D, candidate.pos.getY() + 0.5D, candidate.pos.getZ() + 0.5D),
                        Direction.UP,
                        candidate.pos,
                        false
                );

                for (int c = 0; c < clicks; c++) {
                    client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
                    player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
                    placedThisTick++;
                    totalPlacedBlocks++;
                    currentSecondPlacedCounter++;
                }
                continue;
            }

            // 3. Place new block
            // Prevent item thrashing in the same tick:
            // If blocks were already placed this tick, do not switch items mid-tick
            Item candidateItem = inventoryCache.getRequiredItem(candidate.schematicState);
            if (activeItemThisTick != null && candidateItem != null && activeItemThisTick != candidateItem) {
                break;
            }

            // Ensure held item
            if (!inventoryCache.ensureHeldItem(client, player, candidate.schematicState)) {
                Item needed = inventoryCache.getRequiredItem(candidate.schematicState);
                if (needed != null) {
                    synchronized (missingItems) {
                        missingItems.add(needed);
                    }
                }
                continue;
            }

            // Strict safety check: verify player's main hand actually holds the required item
            ItemStack heldStack = player.getMainHandItem();
            if (candidateItem == null || !heldStack.is(candidateItem) || heldStack.isEmpty()) {
                continue;
            }

            // Calculate placement hit vector and target face
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

            // Handle Smart Rotation for repeaters, comparators, observers, pistons, stairs, etc.
            BlockRotationHelper.RotationAngles rot = config.isSmartRotationEnabled()
                    ? BlockRotationHelper.getRequiredRotation(candidate.schematicState)
                    : null;
            boolean rotate = (rot != null && player.connection != null);
            float origYaw = player.getYRot();
            float origPitch = player.getXRot();

            if (rotate) {
                player.connection.send(new ServerboundMovePlayerPacket.Rot(
                        rot.yaw(),
                        rot.pitch(),
                        player.onGround(),
                        player.horizontalCollision
                ));
                player.setYRot(rot.yaw());
                player.setXRot(rot.pitch());
            }

            try {
                client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
                player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
            } finally {
                if (rotate) {
                    player.setYRot(origYaw);
                    player.setXRot(origPitch);
                    player.connection.send(new ServerboundMovePlayerPacket.Rot(
                            origYaw,
                            origPitch,
                            player.onGround(),
                            player.horizontalCollision
                    ));
                }
            }

            activeItemThisTick = candidateItem;
            placedThisTick++;
            totalPlacedBlocks++;
            currentSecondPlacedCounter++;
        }
    }

    private static int actionPriority(ActionType type) {
        return switch (type) {
            case BREAK_WRONG -> 0;
            case ADJUST_STATE -> 1;
            case PLACE -> 2;
        };
    }

    /**
     * Determines how many right-clicks are required to tune an existing block in the world
     * to match the target schematic state. Returns 0 if already matching or non-adjustable.
     */
    public static int getClicksToMatchState(BlockState worldState, BlockState schematicState, boolean tuneNoteBlocks) {
        if (worldState == null || schematicState == null) return 0;
        if (worldState.getBlock() != schematicState.getBlock()) return 0;

        Block block = worldState.getBlock();

        // 1. Repeaters: DELAY (1, 2, 3, 4)
        if (block instanceof RepeaterBlock && worldState.hasProperty(RepeaterBlock.DELAY) && schematicState.hasProperty(RepeaterBlock.DELAY)) {
            if (worldState.hasProperty(HorizontalDirectionalBlock.FACING) && schematicState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                if (worldState.getValue(HorizontalDirectionalBlock.FACING) != schematicState.getValue(HorizontalDirectionalBlock.FACING)) {
                    return 0; // Facing mismatch requires re-placement, not delay cycling
                }
            }
            int curDelay = worldState.getValue(RepeaterBlock.DELAY);
            int tgtDelay = schematicState.getValue(RepeaterBlock.DELAY);
            if (curDelay != tgtDelay) {
                return (tgtDelay - curDelay + 4) % 4;
            }
        }

        // 2. Comparators: MODE (COMPARE, SUBTRACT)
        if (block instanceof ComparatorBlock && worldState.hasProperty(ComparatorBlock.MODE) && schematicState.hasProperty(ComparatorBlock.MODE)) {
            if (worldState.hasProperty(HorizontalDirectionalBlock.FACING) && schematicState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                if (worldState.getValue(HorizontalDirectionalBlock.FACING) != schematicState.getValue(HorizontalDirectionalBlock.FACING)) {
                    return 0;
                }
            }
            if (worldState.getValue(ComparatorBlock.MODE) != schematicState.getValue(ComparatorBlock.MODE)) {
                return 1;
            }
        }

        // 3. Daylight Detectors: INVERTED (true, false)
        if (block instanceof DaylightDetectorBlock && worldState.hasProperty(DaylightDetectorBlock.INVERTED) && schematicState.hasProperty(DaylightDetectorBlock.INVERTED)) {
            if (worldState.getValue(DaylightDetectorBlock.INVERTED) != schematicState.getValue(DaylightDetectorBlock.INVERTED)) {
                return 1;
            }
        }

        // 4. Note Blocks: NOTE (0..24)
        if (tuneNoteBlocks && block instanceof NoteBlock && worldState.hasProperty(NoteBlock.NOTE) && schematicState.hasProperty(NoteBlock.NOTE)) {
            int curNote = worldState.getValue(NoteBlock.NOTE);
            int tgtNote = schematicState.getValue(NoteBlock.NOTE);
            if (curNote != tgtNote) {
                return (tgtNote - curNote + 25) % 25;
            }
        }

        // 5. Trapdoors: OPEN (true, false)
        if (block instanceof TrapDoorBlock && !worldState.is(Blocks.IRON_TRAPDOOR)
                && worldState.hasProperty(TrapDoorBlock.OPEN) && schematicState.hasProperty(TrapDoorBlock.OPEN)) {
            if (worldState.getValue(TrapDoorBlock.OPEN) != schematicState.getValue(TrapDoorBlock.OPEN)) {
                return 1;
            }
        }

        // 6. Fence Gates: OPEN (true, false)
        if (block instanceof FenceGateBlock && worldState.hasProperty(FenceGateBlock.OPEN) && schematicState.hasProperty(FenceGateBlock.OPEN)) {
            if (worldState.getValue(FenceGateBlock.OPEN) != schematicState.getValue(FenceGateBlock.OPEN)) {
                return 1;
            }
        }

        // 7. Doors: OPEN (true, false)
        if (block instanceof DoorBlock && !worldState.is(Blocks.IRON_DOOR)
                && worldState.hasProperty(DoorBlock.OPEN) && schematicState.hasProperty(DoorBlock.OPEN)) {
            if (worldState.getValue(DoorBlock.OPEN) != schematicState.getValue(DoorBlock.OPEN)) {
                return 1;
            }
        }

        // 8. Levers: POWERED (true, false)
        if (block instanceof LeverBlock && worldState.hasProperty(LeverBlock.POWERED) && schematicState.hasProperty(LeverBlock.POWERED)) {
            if (worldState.getValue(LeverBlock.POWERED) != schematicState.getValue(LeverBlock.POWERED)) {
                return 1;
            }
        }

        return 0;
    }

    private List<PlacementCandidate> scanCandidates(
            Minecraft client,
            LocalPlayer player,
            ClientLevel world,
            PrinterConfig config
    ) {
        List<PlacementCandidate> list = new ArrayList<>();
        if (!LitematicaBridge.hasActiveSchematics()) {
            return list;
        }

        double range = config.getPlacementRange();
        double rangeSq = range * range;
        BlockPos playerPos = player.blockPosition();
        int r = (int) Math.ceil(range);

        AABB playerBox = player.getBoundingBox();
        Vec3 eyePos = player.getEyePosition();

        for (int dy = -r; dy <= r; dy++) {
            double diffY = (playerPos.getY() + dy + 0.5D) - eyePos.y();
            double dySq = diffY * diffY;
            if (dySq > rangeSq) continue;

            for (int dx = -r; dx <= r; dx++) {
                double diffX = (playerPos.getX() + dx + 0.5D) - eyePos.x();
                double dxSq = diffX * diffX;
                if (dySq + dxSq > rangeSq) continue;

                for (int dz = -r; dz <= r; dz++) {
                    double diffZ = (playerPos.getZ() + dz + 0.5D) - eyePos.z();
                    if (dySq + dxSq + (diffZ * diffZ) > rangeSq) continue;

                    BlockPos pos = playerPos.offset(dx, dy, dz);

                    BlockState schematicState = LitematicaBridge.getSchematicBlockState(world, pos);
                    if (schematicState.isAir()) {
                        continue;
                    }

                    BlockState worldState = world.getBlockState(pos);

                    // 1. Same block type in world: check if interactive property tuning can match it
                    if (worldState.getBlock() == schematicState.getBlock()) {
                        int clicks = getClicksToMatchState(worldState, schematicState, config.isTuneNoteBlocks());
                        if (clicks > 0) {
                            list.add(new PlacementCandidate(pos, schematicState, ActionType.ADJUST_STATE, clicks));
                            continue;
                        }

                        // Completely identical
                        if (worldState.equals(schematicState)) {
                            continue;
                        }

                        // Block matches, but un-cyclable state differs (e.g. facing/rotation):
                        if (config.isBreakWrongBlocks()) {
                            list.add(new PlacementCandidate(pos, schematicState, ActionType.BREAK_WRONG, 0));
                        }
                        continue;
                    }

                    // 2. Different block obstructing in world:
                    if (!worldState.isAir() && !worldState.canBeReplaced()) {
                        if (config.isBreakWrongBlocks()) {
                            list.add(new PlacementCandidate(pos, schematicState, ActionType.BREAK_WRONG, 0));
                        }
                        continue;
                    }

                    // 3. World position is air or can be replaced:
                    AABB blockBox = new AABB(pos);
                    if (!schematicState.isAir() && playerBox.intersects(blockBox)) {
                        continue;
                    }

                    if (!hasRequiredSupport(world, pos, schematicState)) {
                        continue;
                    }

                    list.add(new PlacementCandidate(pos, schematicState, ActionType.PLACE, 0));
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
        public final ActionType actionType;
        public final int clicks;

        public PlacementCandidate(BlockPos pos, BlockState schematicState) {
            this(pos, schematicState, ActionType.PLACE, 0);
        }

        public PlacementCandidate(BlockPos pos, BlockState schematicState, ActionType actionType, int clicks) {
            this.pos = pos;
            this.schematicState = schematicState;
            this.actionType = actionType;
            this.clicks = clicks;
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
