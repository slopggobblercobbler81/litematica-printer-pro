package com.slopggobbler.litematica.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Smart rotation and hit-vector calculator for directional blocks on Minecraft 26.3.
 */
public class BlockRotationHelper {

    public static BlockHitResult calculatePlacementHit(BlockPos targetPos, BlockState targetState, boolean useAccurateProtocol) {
        Direction sideFace = Direction.UP;
        Vec3 hitVec;

        if (useAccurateProtocol) {
            hitVec = encodeAccurateProtocolHit(targetPos, targetState);
            sideFace = getTargetPlacementFace(targetState);
            return new BlockHitResult(hitVec, sideFace, targetPos, false);
        }

        sideFace = determineVanillaClickFace(targetState);
        hitVec = calculateVanillaHitVec(targetPos, targetState, sideFace);

        return new BlockHitResult(hitVec, sideFace, targetPos, false);
    }

    public static Vec3 encodeAccurateProtocolHit(BlockPos pos, BlockState state) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 0.5D;
        double pz = pos.getZ() + 0.5D;

        if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            int facingCode = (facing.get3DDataValue() + 2) * 2;
            px = pos.getX() + 2.0D + facingCode;
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            int facingCode = (facing.get3DDataValue() + 2) * 2;
            px = pos.getX() + 2.0D + facingCode;
        } else if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
            int axisCode = switch (axis) {
                case X -> 10;
                case Y -> 12;
                case Z -> 14;
            };
            px = pos.getX() + 2.0D + axisCode;
        }

        if (state.hasProperty(BlockStateProperties.HALF)) {
            Half half = state.getValue(BlockStateProperties.HALF);
            py = (half == Half.TOP) ? pos.getY() + 0.75D : pos.getY() + 0.25D;
        } else if (state.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            SlabType slabType = state.getValue(BlockStateProperties.SLAB_TYPE);
            if (slabType == SlabType.TOP) {
                py = pos.getY() + 0.75D;
            } else if (slabType == SlabType.BOTTOM) {
                py = pos.getY() + 0.25D;
            }
        }

        return new Vec3(px, py, pz);
    }

    public static Direction determineVanillaClickFace(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof HopperBlock && state.hasProperty(HopperBlock.FACING)) {
            Direction facing = state.getValue(HopperBlock.FACING);
            return facing.getOpposite();
        }

        if (block instanceof ObserverBlock && state.hasProperty(ObserverBlock.FACING)) {
            return state.getValue(ObserverBlock.FACING).getOpposite();
        }

        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.getValue(HorizontalDirectionalBlock.FACING);
        }

        return Direction.UP;
    }

    public static Vec3 calculateVanillaHitVec(BlockPos pos, BlockState state, Direction sideFace) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        if (state.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            SlabType slab = state.getValue(BlockStateProperties.SLAB_TYPE);
            if (slab == SlabType.TOP) {
                y = pos.getY() + 0.75D;
            } else if (slab == SlabType.BOTTOM) {
                y = pos.getY() + 0.25D;
            }
        }

        if (state.hasProperty(BlockStateProperties.HALF)) {
            Half half = state.getValue(BlockStateProperties.HALF);
            y = (half == Half.TOP) ? pos.getY() + 0.75D : pos.getY() + 0.25D;
        }

        switch (sideFace) {
            case DOWN -> y = pos.getY();
            case UP -> y = pos.getY() + 1.0D;
            case NORTH -> z = pos.getZ();
            case SOUTH -> z = pos.getZ() + 1.0D;
            case WEST -> x = pos.getX();
            case EAST -> x = pos.getX() + 1.0D;
        }

        return new Vec3(x, y, z);
    }

    public static float getRequiredYaw(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
    }

    public static float getRequiredPitch(Direction facing) {
        return switch (facing) {
            case UP -> -90.0f;
            case DOWN -> 90.0f;
            default -> 0.0f;
        };
    }

    private static Direction getTargetPlacementFace(BlockState state) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.UP;
    }
}
