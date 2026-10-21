package com.slopggobbler.litematica.engine;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Validates strict line-of-sight raycasting to ensure server-side anti-cheat compliance.
 */
public class RaycastValidator {

    /**
     * Checks if the player has an unobstructed line of sight to the target block.
     *
     * @param world Current client world.
     * @param player Current player entity.
     * @param targetPos Target block position.
     * @param targetHitVec Specific point on the block face.
     * @return true if ray reaches target position without hitting intermediate solid blocks.
     */
    public static boolean hasLineOfSight(ClientWorld world, ClientPlayerEntity player, BlockPos targetPos, Vec3d targetHitVec) {
        Vec3d eyePos = player.getEyePos();

        RaycastContext context = new RaycastContext(
                eyePos,
                targetHitVec,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        );

        BlockHitResult hit = world.raycast(context);

        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hit.getBlockPos();
            return hitPos.equals(targetPos) || world.getBlockState(hitPos).isReplaceable();
        }

        return false;
    }
}
