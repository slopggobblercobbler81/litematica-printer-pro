package com.slopggobbler.litematica.engine;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Smart rotation and hit-vector calculator for directional blocks.
 * Supports vanilla placement geometry and Masa/Carpet Accurate Placement Protocol v2/v3.
 */
public class BlockRotationHelper {

    /**
     * Resolves the optimal BlockHitResult for placing targetState at targetPos.
     *
     * @param targetPos Target block position in the world.
     * @param targetState Expected BlockState from schematic.
     * @param useAccurateProtocol Whether Carpet/Masa accurate placement protocol is active.
     * @return BlockHitResult suitable for client interaction manager.
     */
    public static BlockHitResult calculatePlacementHit(BlockPos targetPos, BlockState targetState, boolean useAccurateProtocol) {
        Direction sideFace = Direction.UP;
        Vec3d hitVec;

        if (useAccurateProtocol) {
            // Carpet / Masa Accurate Placement protocol hit vector encoding
            hitVec = encodeAccurateProtocolHit(targetPos, targetState);
            sideFace = getTargetPlacementFace(targetState);
            return new BlockHitResult(hitVec, sideFace, targetPos, false);
        }

        // Vanilla orientation calculation
        sideFace = determineVanillaClickFace(targetState);
        hitVec = calculateVanillaHitVec(targetPos, targetState, sideFace);

        return new BlockHitResult(hitVec, sideFace, targetPos, false);
    }

    /**
     * Encodes orientation data into hit vector for Carpet/Masa Accurate Block Placement protocol.
     * Protocol v2 & v3 encoding:
     * hitVec.x or hitVec.y encodes property index + 2 offset.
     */
    public static Vec3d encodeAccurateProtocolHit(BlockPos pos, BlockState state) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 0.5D;
        double pz = pos.getZ() + 0.5D;

        if (state.contains(Properties.FACING)) {
            Direction facing = state.get(Properties.FACING);
            // Protocol facing index offset: index 2 to 7 represents Direction.byId
            int facingCode = (facing.getId() + 2) * 2;
            px = pos.getX() + 2.0D + facingCode;
        } else if (state.contains(Properties.HORIZONTAL_FACING)) {
            Direction facing = state.get(Properties.HORIZONTAL_FACING);
            int facingCode = (facing.getId() + 2) * 2;
            px = pos.getX() + 2.0D + facingCode;
        } else if (state.contains(Properties.AXIS)) {
            Direction.Axis axis = state.get(Properties.AXIS);
            int axisCode = switch (axis) {
                case X -> 10;
                case Y -> 12;
                case Z -> 14;
            };
            px = pos.getX() + 2.0D + axisCode;
        }

        if (state.contains(Properties.BLOCK_HALF)) {
            BlockHalf half = state.get(Properties.BLOCK_HALF);
            py = (half == BlockHalf.TOP) ? pos.getY() + 0.75D : pos.getY() + 0.25D;
        } else if (state.contains(Properties.SLAB_TYPE)) {
            SlabType slabType = state.get(Properties.SLAB_TYPE);
            if (slabType == SlabType.TOP) {
                py = pos.getY() + 0.75D;
            } else if (slabType == SlabType.BOTTOM) {
                py = pos.getY() + 0.25D;
            }
        }

        return new Vec3d(px, py, pz);
    }

    /**
     * Determines which face of the neighbor or target to interact with in vanilla mode.
     */
    public static Direction determineVanillaClickFace(BlockState state) {
        Block block = state.getBlock();

        // Hoppers point into the block they were clicked against
        if (block instanceof HopperBlock && state.contains(HopperBlock.FACING)) {
            Direction facing = state.get(HopperBlock.FACING);
            return facing.getOpposite();
        }

        // Observers face opposite to the clicked face
        if (block instanceof ObserverBlock && state.contains(ObserverBlock.FACING)) {
            return state.get(ObserverBlock.FACING).getOpposite();
        }

        // Wall torches and ladders attach to the wall face
        if (state.contains(HorizontalFacingBlock.FACING)) {
            return state.get(HorizontalFacingBlock.FACING);
        }

        return Direction.UP;
    }

    /**
     * Calculates the hit vector inside the target block bounds to satisfy half/facing checks.
     */
    public static Vec3d calculateVanillaHitVec(BlockPos pos, BlockState state, Direction sideFace) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        // Slab upper/lower half hit vector
        if (state.contains(Properties.SLAB_TYPE)) {
            SlabType slab = state.get(Properties.SLAB_TYPE);
            if (slab == SlabType.TOP) {
                y = pos.getY() + 0.75D;
            } else if (slab == SlabType.BOTTOM) {
                y = pos.getY() + 0.25D;
            }
        }

        // Stairs half check
        if (state.contains(Properties.BLOCK_HALF)) {
            BlockHalf half = state.get(Properties.BLOCK_HALF);
            y = (half == BlockHalf.TOP) ? pos.getY() + 0.75D : pos.getY() + 0.25D;
        }

        // Align sub-coordinate to face
        switch (sideFace) {
            case DOWN -> y = pos.getY();
            case UP -> y = pos.getY() + 1.0D;
            case NORTH -> z = pos.getZ();
            case SOUTH -> z = pos.getZ() + 1.0D;
            case WEST -> x = pos.getX();
            case EAST -> x = pos.getX() + 1.0D;
        }

        return new Vec3d(x, y, z);
    }

    /**
     * Calculates required player yaw angle for placing blocks with horizontal facing.
     */
    public static float getRequiredYaw(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
    }

    /**
     * Calculates required player pitch angle for vertical blocks (pistons, droppers).
     */
    public static float getRequiredPitch(Direction facing) {
        return switch (facing) {
            case UP -> -90.0f;
            case DOWN -> 90.0f;
            default -> 0.0f;
        };
    }

    private static Direction getTargetPlacementFace(BlockState state) {
        if (state.contains(Properties.FACING)) {
            return state.get(Properties.FACING);
        }
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.get(Properties.HORIZONTAL_FACING);
        }
        return Direction.UP;
    }
}
