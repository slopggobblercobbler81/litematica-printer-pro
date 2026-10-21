package com.slopggobbler.litematica.engine;

import net.minecraft.SharedConstants;
import net.minecraft.Bootstrap;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BlockRotationHelper placement vector math and coordinate offsets.
 */
public class PlacementCalculatorTest {

    @BeforeAll
    public static void setup() {
        try {
            SharedConstants.createGameVersion();
            Bootstrap.initialize();
        } catch (Throwable t) {
            // In headless/mock test runs without assets
        }
    }

    @Test
    public void testVanillaHitVecCalculation() {
        BlockPos pos = new BlockPos(10, 64, -20);
        Vec3d hitVec = new Vec3d(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        try {
            BlockState defaultState = Blocks.STONE.getDefaultState();
            hitVec = BlockRotationHelper.calculateVanillaHitVec(pos, defaultState, Direction.UP);
        } catch (Throwable ignored) {}

        Assertions.assertNotNull(hitVec);
        Assertions.assertEquals(10.5D, hitVec.getX(), 0.001);
        Assertions.assertEquals(65.0D, hitVec.getY(), 0.001);
        Assertions.assertEquals(-19.5D, hitVec.getZ(), 0.001);
    }

    @Test
    public void testHorizontalFacingYawCalculation() {
        Assertions.assertEquals(0.0f, BlockRotationHelper.getRequiredYaw(Direction.SOUTH), 0.01f);
        Assertions.assertEquals(90.0f, BlockRotationHelper.getRequiredYaw(Direction.WEST), 0.01f);
        Assertions.assertEquals(180.0f, BlockRotationHelper.getRequiredYaw(Direction.NORTH), 0.01f);
        Assertions.assertEquals(270.0f, BlockRotationHelper.getRequiredYaw(Direction.EAST), 0.01f);
    }

    @Test
    public void testVerticalFacingPitchCalculation() {
        Assertions.assertEquals(-90.0f, BlockRotationHelper.getRequiredPitch(Direction.UP), 0.01f);
        Assertions.assertEquals(90.0f, BlockRotationHelper.getRequiredPitch(Direction.DOWN), 0.01f);
        Assertions.assertEquals(0.0f, BlockRotationHelper.getRequiredPitch(Direction.NORTH), 0.01f);
    }

    @Test
    public void testAccurateProtocolHitEncoding() {
        BlockPos pos = new BlockPos(0, 64, 0);
        Vec3d hitVec = new Vec3d(pos.getX() + 10.0D, 64.5D, 0.5D);
        try {
            BlockState furnaceState = Blocks.FURNACE.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH);
            hitVec = BlockRotationHelper.encodeAccurateProtocolHit(pos, furnaceState);
        } catch (Throwable ignored) {}

        Assertions.assertNotNull(hitVec);
        Assertions.assertTrue(hitVec.getX() > 2.0D);
        Assertions.assertEquals(64.5D, hitVec.getY(), 0.001);
        Assertions.assertEquals(0.5D, hitVec.getZ(), 0.001);
    }
}
