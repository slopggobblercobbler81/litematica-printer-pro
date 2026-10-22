package com.slopggobbler.litematica.engine;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Validates strict line-of-sight raycasting to ensure server-side anti-cheat compliance on 26.3.
 */
public class RaycastValidator {

    public static boolean hasLineOfSight(ClientLevel world, LocalPlayer player, BlockPos targetPos, Vec3 targetHitVec) {
        Vec3 eyePos = player.getEyePosition();

        ClipContext context = new ClipContext(
                eyePos,
                targetHitVec,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult hit = world.clip(context);

        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hit.getBlockPos();
            return hitPos.equals(targetPos) || world.getBlockState(hitPos).canBeReplaced();
        }

        return false;
    }
}
