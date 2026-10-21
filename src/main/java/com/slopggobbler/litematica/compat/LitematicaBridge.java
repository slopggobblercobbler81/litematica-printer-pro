package com.slopggobbler.litematica.compat;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean compatibility bridge for Litematica schematic data.
 * Safe fallback handling for schematic world resolution.
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
     *
     * @param world Current client world.
     * @param pos Target world coordinate.
     * @return Expected BlockState from schematic, or Blocks.AIR.getDefaultState() if none.
     */
    public static BlockState getSchematicBlockState(ClientWorld world, BlockPos pos) {
        if (!litematicaAvailable || world == null || pos == null) {
            return Blocks.AIR.getDefaultState();
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
            // Defensive logging on unexpected API changes
            LOGGER.debug("Schematic state lookup exception: {}", t.getMessage());
        }

        return Blocks.AIR.getDefaultState();
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
