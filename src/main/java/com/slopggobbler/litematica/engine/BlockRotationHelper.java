package com.slopggobbler.litematica.engine;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Smart rotation and hit-vector calculator for directional blocks on Minecraft 26.3.
 * Supports accurate player angle resolution for repeaters, comparators, observers, pistons,
 * dispensers, stairs, and Carpet Accurate Placement Protocol.
 */
public class BlockRotationHelper {

    public record RotationAngles(float yaw, float pitch) {}

    /**
     * Checks if the Carpet Accurate Placement protocol is available in the environment.
     * Prevents sending Carpet offset vectors (+10.0D) to vanilla servers which reject them.
     */
    public static boolean isCarpetProtocolAvailable() {
        try {
            return FabricLoader.getInstance() != null && FabricLoader.getInstance().isModLoaded("carpet");
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Resolves the required player yaw and pitch angles to place the target block
     * with the orientation specified in the schematic state.
     * Returns null if the block does not depend on player view orientation.
     */
    public static RotationAngles getRequiredRotation(BlockState targetState) {
        if (targetState == null) return null;
        Block block = targetState.getBlock();

        // 1. Redstone Diodes (Repeaters and Comparators):
        // In vanilla DiodeBlock.getStateForPlacement, FACING is set to ctx.getHorizontalDirection().getOpposite().
        // To achieve schematic's facing, the player must face facing.getOpposite().
        if (block instanceof DiodeBlock && targetState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction facing = targetState.getValue(HorizontalDirectionalBlock.FACING);
            return new RotationAngles(getRequiredYaw(facing.getOpposite()), 0.0f);
        }

        // 2. Observers:
        // In vanilla ObserverBlock.getStateForPlacement, FACING = ctx.getNearestLookingDirection().
        // Therefore, the player must look in direction facing.
        if (block instanceof ObserverBlock && targetState.hasProperty(ObserverBlock.FACING)) {
            Direction facing = targetState.getValue(ObserverBlock.FACING);
            return switch (facing) {
                case UP -> new RotationAngles(0.0f, -90.0f);
                case DOWN -> new RotationAngles(0.0f, 90.0f);
                default -> new RotationAngles(getRequiredYaw(facing), 0.0f);
            };
        }

        // 3. Pistons, Dispensers, Droppers, Crafters:
        // In vanilla, these face ctx.getNearestLookingDirection().getOpposite() (face towards player).
        // To achieve schematic's facing, the player must look in facing.getOpposite().
        if ((block instanceof PistonBaseBlock || block instanceof DispenserBlock || block instanceof CrafterBlock)
                && targetState.hasProperty(BlockStateProperties.FACING)) {
            Direction facing = targetState.getValue(BlockStateProperties.FACING);
            Direction lookDir = facing.getOpposite();
            return switch (lookDir) {
                case UP -> new RotationAngles(0.0f, -90.0f);
                case DOWN -> new RotationAngles(0.0f, 90.0f);
                default -> new RotationAngles(getRequiredYaw(lookDir), 0.0f);
            };
        }

        // 4. Stairs, Doors, Fence Gates:
        // In vanilla, FACING = ctx.getHorizontalDirection().
        if ((block instanceof StairBlock || block instanceof DoorBlock || block instanceof FenceGateBlock)
                && targetState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction facing = targetState.getValue(HorizontalDirectionalBlock.FACING);
            return new RotationAngles(getRequiredYaw(facing), 0.0f);
        }

        // 5. Chests, Furnaces, Anvils, Glazed Terracotta, Barrels, Looms, Stonecutters:
        // In vanilla, front faces the player: FACING = ctx.getHorizontalDirection().getOpposite().
        if ((block instanceof ChestBlock || block instanceof AbstractFurnaceBlock
                || block instanceof GlazedTerracottaBlock || block instanceof BarrelBlock
                || block instanceof LoomBlock || block instanceof StonecutterBlock
                || block instanceof CampfireBlock || block instanceof EnderChestBlock)
                && targetState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction facing = targetState.getValue(HorizontalDirectionalBlock.FACING);
            return new RotationAngles(getRequiredYaw(facing.getOpposite()), 0.0f);
        }

        // 6. Generic HorizontalDirectionalBlock fallback:
        if (targetState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction facing = targetState.getValue(HorizontalDirectionalBlock.FACING);
            return new RotationAngles(getRequiredYaw(facing.getOpposite()), 0.0f);
        }

        // 7. Generic DirectionalBlock fallback:
        if (targetState.hasProperty(BlockStateProperties.FACING)) {
            Direction facing = targetState.getValue(BlockStateProperties.FACING);
            Direction lookDir = facing.getOpposite();
            return switch (lookDir) {
                case UP -> new RotationAngles(0.0f, -90.0f);
                case DOWN -> new RotationAngles(0.0f, 90.0f);
                default -> new RotationAngles(getRequiredYaw(lookDir), 0.0f);
            };
        }

        return null;
    }

    /**
     * Resolves the optimal BlockHitResult for placing targetState at targetPos.
     */
    public static BlockHitResult calculatePlacementHit(BlockPos targetPos, BlockState targetState, boolean useAccurateProtocol) {
        if (targetState == null) {
            return new BlockHitResult(new Vec3(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D), Direction.UP, targetPos, false);
        }

        boolean canUseCarpet = useAccurateProtocol && isCarpetProtocolAvailable();
        Block block = targetState.getBlock();

        // Floor-supported blocks (repeaters, comparators, redstone wire, pressure plates, rails)
        // must click the top face of the block below them.
        if (block instanceof DiodeBlock || block instanceof RedstoneWireBlock
                || block instanceof BaseRailBlock || block instanceof PressurePlateBlock
                || (block instanceof TorchBlock && !(block instanceof WallTorchBlock))) {
            BlockPos clickPos = targetPos.below();
            Direction sideFace = Direction.UP;
            Vec3 hitVec;
            if (canUseCarpet) {
                hitVec = encodeAccurateProtocolHit(targetPos, targetState);
            } else {
                hitVec = new Vec3(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
            }
            return new BlockHitResult(hitVec, sideFace, clickPos, false);
        }

        // Wall-mounted blocks (wall torches, ladders)
        if ((block instanceof WallTorchBlock || block instanceof LadderBlock)
                && targetState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction facing = targetState.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos supportPos = targetPos.relative(facing.getOpposite());
            Vec3 hitVec = new Vec3(
                    targetPos.getX() + 0.5D - facing.getStepX() * 0.45D,
                    targetPos.getY() + 0.5D,
                    targetPos.getZ() + 0.5D - facing.getStepZ() * 0.45D
            );
            return new BlockHitResult(hitVec, facing, supportPos, false);
        }

        Direction sideFace = Direction.UP;
        Vec3 hitVec;

        if (canUseCarpet) {
            hitVec = encodeAccurateProtocolHit(targetPos, targetState);
            sideFace = getTargetPlacementFace(targetState);
            return new BlockHitResult(hitVec, sideFace, targetPos, false);
        }

        sideFace = determineVanillaClickFace(targetState);
        hitVec = calculateVanillaHitVec(targetPos, targetState, sideFace);

        return new BlockHitResult(hitVec, sideFace, targetPos, false);
    }

    public static Vec3 encodeAccurateProtocolHit(BlockPos pos, Direction facing) {
        double px = pos.getX() + 2.0D + (facing.get3DDataValue() + 2) * 2;
        double py = pos.getY() + 0.5D;
        double pz = pos.getZ() + 0.5D;
        return new Vec3(px, py, pz);
    }

    public static Vec3 encodeAccurateProtocolHit(BlockPos pos, BlockState state) {
        if (state == null) {
            return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        }

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
        if (state == null) return Direction.UP;
        Block block = state.getBlock();

        if (block instanceof RotatedPillarBlock && state.hasProperty(RotatedPillarBlock.AXIS)) {
            Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
            return switch (axis) {
                case X -> Direction.EAST;
                case Y -> Direction.UP;
                case Z -> Direction.SOUTH;
            };
        }

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

        if (state != null) {
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
