package com.slopggobbler.litematica.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import com.slopggobbler.litematica.LitematicaPrinterProMod;

import java.util.ArrayList;
import java.util.List;

/**
 * MaLiLib config integration handler for Litematica Printer Pro.
 */
public class PrinterConfigHandler implements IConfigHandler {
    private static final PrinterConfigHandler INSTANCE = new PrinterConfigHandler();

    public static final ConfigBoolean PRINTER_ENABLED = new ConfigBoolean(
            "printerEnabled", false, "litematica_printer_pro.config.printer_enabled.comment");

    public static final ConfigDouble PLACEMENT_RANGE = new ConfigDouble(
            "placementRange", 4.5D, 1.0D, 64.0D, "litematica_printer_pro.config.placement_range.comment");

    public static final ConfigInteger BLOCKS_PER_TICK = new ConfigInteger(
            "blocksPerTick", 3, 1, 20, "litematica_printer_pro.config.blocks_per_tick.comment");

    public static final ConfigInteger PLACEMENT_INTERVAL_TICKS = new ConfigInteger(
            "placementIntervalTicks", 1, 0, 20, "litematica_printer_pro.config.placement_interval.comment");

    public static final ConfigBoolean SMART_ROTATION = new ConfigBoolean(
            "smartRotationEnabled", true, "litematica_printer_pro.config.smart_rotation.comment");

    public static final ConfigBoolean CARPET_ACCURATE = new ConfigBoolean(
            "carpetAccuratePlacement", true, "litematica_printer_pro.config.carpet_accurate_placement.comment");

    public static final ConfigBoolean INVENTORY_BUFFER = new ConfigBoolean(
            "inventoryBufferCache", true, "litematica_printer_pro.config.inventory_buffer_cache.comment");

    public static final ConfigBoolean BREAK_WRONG = new ConfigBoolean(
            "breakWrongBlocks", false, "litematica_printer_pro.config.break_wrong_blocks.comment");

    public static final ConfigBoolean STRICT_RAYCAST = new ConfigBoolean(
            "strictRaycast", false, "litematica_printer_pro.config.strict_raycast.comment");

    public static final ConfigBoolean SHOW_HUD = new ConfigBoolean(
            "showHud", false, "litematica_printer_pro.config.show_hud.comment");

    public static final ConfigBoolean TUNE_NOTEBLOCKS = new ConfigBoolean(
            "tuneNoteBlocks", true, "litematica_printer_pro.config.tune_noteblocks.comment");

    public static final List<IConfigBase> OPTIONS = new ArrayList<>();

    static {
        OPTIONS.add(PRINTER_ENABLED);
        OPTIONS.add(PLACEMENT_RANGE);
        OPTIONS.add(BLOCKS_PER_TICK);
        OPTIONS.add(PLACEMENT_INTERVAL_TICKS);
        OPTIONS.add(SMART_ROTATION);
        OPTIONS.add(CARPET_ACCURATE);
        OPTIONS.add(INVENTORY_BUFFER);
        OPTIONS.add(BREAK_WRONG);
        OPTIONS.add(STRICT_RAYCAST);
        OPTIONS.add(SHOW_HUD);
        OPTIONS.add(TUNE_NOTEBLOCKS);

        PRINTER_ENABLED.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        PLACEMENT_RANGE.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        BLOCKS_PER_TICK.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        PLACEMENT_INTERVAL_TICKS.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        SMART_ROTATION.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        CARPET_ACCURATE.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        INVENTORY_BUFFER.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        BREAK_WRONG.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        STRICT_RAYCAST.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        SHOW_HUD.setValueChangeCallback(cfg -> getInstance().syncToInternal());
        TUNE_NOTEBLOCKS.setValueChangeCallback(cfg -> getInstance().syncToInternal());
    }

    public static PrinterConfigHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void load() {
        PrinterConfig.getInstance().loadConfig();
        syncFromInternal();
    }

    @Override
    public void save() {
        syncToInternal();
        PrinterConfig.getInstance().saveConfig();
    }

    public void syncFromInternal() {
        PrinterConfig c = PrinterConfig.getInstance();
        PRINTER_ENABLED.setBooleanValue(c.isPrinterEnabled());
        PLACEMENT_RANGE.setDoubleValue(c.getPlacementRange());
        BLOCKS_PER_TICK.setIntegerValue(c.getBlocksPerTick());
        PLACEMENT_INTERVAL_TICKS.setIntegerValue(c.getPlacementIntervalTicks());
        SMART_ROTATION.setBooleanValue(c.isSmartRotationEnabled());
        CARPET_ACCURATE.setBooleanValue(c.isCarpetAccuratePlacement());
        INVENTORY_BUFFER.setBooleanValue(c.isInventoryBufferCache());
        BREAK_WRONG.setBooleanValue(c.isBreakWrongBlocks());
        STRICT_RAYCAST.setBooleanValue(c.isStrictRaycast());
        SHOW_HUD.setBooleanValue(c.isShowHud());
        TUNE_NOTEBLOCKS.setBooleanValue(c.isTuneNoteBlocks());
    }

    public void syncToInternal() {
        PrinterConfig c = PrinterConfig.getInstance();
        c.setPrinterEnabled(PRINTER_ENABLED.getBooleanValue());
        c.setPlacementRange(PLACEMENT_RANGE.getDoubleValue());
        c.setBlocksPerTick(BLOCKS_PER_TICK.getIntegerValue());
        c.setPlacementIntervalTicks(PLACEMENT_INTERVAL_TICKS.getIntegerValue());
        c.setSmartRotationEnabled(SMART_ROTATION.getBooleanValue());
        c.setCarpetAccuratePlacement(CARPET_ACCURATE.getBooleanValue());
        c.setInventoryBufferCache(INVENTORY_BUFFER.getBooleanValue());
        c.setBreakWrongBlocks(BREAK_WRONG.getBooleanValue());
        c.setStrictRaycast(STRICT_RAYCAST.getBooleanValue());
        c.setShowHud(SHOW_HUD.getBooleanValue());
        c.setTuneNoteBlocks(TUNE_NOTEBLOCKS.getBooleanValue());
    }
}
