package com.slopggobbler.litematica.compat;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean compatibility bridge for Litematica schematic data on Minecraft 26.3.
 */
public class LitematicaBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("LitematicaBridge");
    private static boolean litematicaAvailable = false;

    static {
        try {
            Class.forName("fi.dy.masa.litematica.data.DataManager");
            litematicaAvailable = true;
            LOGGER.info("[LitematicaBridge] Successfully linked with Litematica runtime.");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("[LitematicaBridge] Litematica classes not detected in runtime classpath.");
            litematicaAvailable = false;
        }
    }

    public static boolean isLitematicaAvailable() {
        return litematicaAvailable;
    }

    /**
     * Retrieves the target BlockState from the active schematic at the given world position.
     */
    public static BlockState getSchematicBlockState(ClientLevel world, BlockPos pos) {
        if (!litematicaAvailable || world == null || pos == null) {
            return Blocks.AIR.defaultBlockState();
        }

        try {
            WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
            if (schematicWorld != null) {
                BlockState state = schematicWorld.getBlockState(pos);
                if (state != null && !state.isAir()) {
                    return state;
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("Schematic state lookup exception: {}", t.getMessage());
        }

        return Blocks.AIR.defaultBlockState();
    }

    /**
     * Checks if any schematic placements are loaded and visible.
     */
    public static boolean hasActiveSchematics() {
        if (!litematicaAvailable) return false;
        try {
            SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();
            return manager != null && !manager.getAllSchematicsPlacements().isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }
}
