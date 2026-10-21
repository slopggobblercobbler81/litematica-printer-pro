package com.slopggobbler.litematica.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slopggobbler.litematica.LitematicaPrinterProMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration manager for Litematica Printer Pro.
 * Provides configuration options compatible with MaLiLib and Fabric JSON persistence.
 */
public class PrinterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILENAME = "litematica_printer_pro.json";
    private static final PrinterConfig INSTANCE = new PrinterConfig();

    // Generic Placement settings
    private boolean printerEnabled = false;
    private double placementRange = 4.5D;
    private int blocksPerTick = 3;
    private int placementIntervalTicks = 1;

    // Rotation & Protocol settings
    private boolean smartRotationEnabled = true;
    private boolean carpetAccuratePlacement = true;

    // Inventory & Cache settings
    private boolean inventoryBufferCache = true;
    private boolean breakWrongBlocks = false;
    private boolean strictRaycast = false;

    // Visuals & HUD
    private boolean showHud = true;
    private int hudPosX = 10;
    private int hudPosY = 10;

    // Internal MaLiLib adapters list
    private final List<ConfigOption<?>> options = new ArrayList<>();

    public interface ConfigOption<T> {
        String getName();
        String getComment();
        T getValue();
        void setValue(T value);
        T getDefaultValue();
    }

    public static class BooleanOption implements ConfigOption<Boolean> {
        private final String name;
        private final String comment;
        private final boolean defaultValue;
        private boolean value;

        public BooleanOption(String name, String comment, boolean defaultValue) {
            this.name = name;
            this.comment = comment;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        @Override public String getName() { return name; }
        @Override public String getComment() { return comment; }
        @Override public Boolean getValue() { return value; }
        @Override public void setValue(Boolean value) { this.value = value; }
        @Override public Boolean getDefaultValue() { return defaultValue; }
    }

    public static class IntegerOption implements ConfigOption<Integer> {
        private final String name;
        private final String comment;
        private final int min;
        private final int max;
        private final int defaultValue;
        private int value;

        public IntegerOption(String name, String comment, int min, int max, int defaultValue) {
            this.name = name;
            this.comment = comment;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        @Override public String getName() { return name; }
        @Override public String getComment() { return comment; }
        @Override public Integer getValue() { return value; }
        @Override public void setValue(Integer value) { this.value = Math.max(min, Math.min(max, value)); }
        @Override public Integer getDefaultValue() { return defaultValue; }
        public int getMin() { return min; }
        public int getMax() { return max; }
    }

    public static class DoubleOption implements ConfigOption<Double> {
        private final String name;
        private final String comment;
        private final double min;
        private final double max;
        private final double defaultValue;
        private double value;

        public DoubleOption(String name, String comment, double min, double max, double defaultValue) {
            this.name = name;
            this.comment = comment;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        @Override public String getName() { return name; }
        @Override public String getComment() { return comment; }
        @Override public Double getValue() { return value; }
        @Override public void setValue(Double value) { this.value = Math.max(min, Math.min(max, value)); }
        @Override public Double getDefaultValue() { return defaultValue; }
        public double getMin() { return min; }
        public double getMax() { return max; }
    }

    private PrinterConfig() {
        registerConfigOptions();
    }

    public static PrinterConfig getInstance() {
        return INSTANCE;
    }

    private void registerConfigOptions() {
        options.add(new BooleanOption("printerEnabled", "Master switch for auto-placement", false));
        options.add(new DoubleOption("placementRange", "Placement reach range in blocks", 1.0D, 6.0D, 4.5D));
        options.add(new IntegerOption("blocksPerTick", "Maximum blocks to place per tick", 1, 20, 3));
        options.add(new IntegerOption("placementIntervalTicks", "Delay between placement cycles in ticks", 0, 20, 1));
        options.add(new BooleanOption("smartRotationEnabled", "Calculate precise orientation vectors for directional blocks", true));
        options.add(new BooleanOption("carpetAccuratePlacement", "Use Carpet Accurate Placement protocol v2/v3", true));
        options.add(new BooleanOption("inventoryBufferCache", "Cache inventory slots for instant hotbar swapping", true));
        options.add(new BooleanOption("breakWrongBlocks", "Break obstructing or incorrectly placed blocks", false));
        options.add(new BooleanOption("strictRaycast", "Require unobstructed line-of-sight to place blocks", false));
        options.add(new BooleanOption("showHud", "Display printer status HUD on screen", true));
    }

    public List<ConfigOption<?>> getOptions() {
        return options;
    }

    public File getConfigFile() {
        File configDir = null;
        try {
            if (FabricLoader.getInstance() != null) {
                configDir = FabricLoader.getInstance().getConfigDir().toFile();
            }
        } catch (Throwable ignored) {}
        if (configDir == null) {
            configDir = new File("build/test-config");
        }
        return new File(configDir, CONFIG_FILENAME);
    }

    public void loadConfig() {
        File file = getConfigFile();
        if (!file.exists()) {
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("printerEnabled")) this.printerEnabled = json.get("printerEnabled").getAsBoolean();
            if (json.has("placementRange")) this.placementRange = json.get("placementRange").getAsDouble();
            if (json.has("blocksPerTick")) this.blocksPerTick = json.get("blocksPerTick").getAsInt();
            if (json.has("placementIntervalTicks")) this.placementIntervalTicks = json.get("placementIntervalTicks").getAsInt();
            if (json.has("smartRotationEnabled")) this.smartRotationEnabled = json.get("smartRotationEnabled").getAsBoolean();
            if (json.has("carpetAccuratePlacement")) this.carpetAccuratePlacement = json.get("carpetAccuratePlacement").getAsBoolean();
            if (json.has("inventoryBufferCache")) this.inventoryBufferCache = json.get("inventoryBufferCache").getAsBoolean();
            if (json.has("breakWrongBlocks")) this.breakWrongBlocks = json.get("breakWrongBlocks").getAsBoolean();
            if (json.has("strictRaycast")) this.strictRaycast = json.get("strictRaycast").getAsBoolean();
            if (json.has("showHud")) this.showHud = json.get("showHud").getAsBoolean();
            if (json.has("hudPosX")) this.hudPosX = json.get("hudPosX").getAsInt();
            if (json.has("hudPosY")) this.hudPosY = json.get("hudPosY").getAsInt();

            LitematicaPrinterProMod.LOGGER.info("[{}] Configuration loaded successfully.", LitematicaPrinterProMod.MOD_NAME);
        } catch (Exception e) {
            LitematicaPrinterProMod.LOGGER.error("[{}] Failed to load configuration: {}", LitematicaPrinterProMod.MOD_NAME, e.getMessage());
        }
    }

    public void saveConfig() {
        File file = getConfigFile();
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            JsonObject json = new JsonObject();
            json.addProperty("printerEnabled", this.printerEnabled);
            json.addProperty("placementRange", this.placementRange);
            json.addProperty("blocksPerTick", this.blocksPerTick);
            json.addProperty("placementIntervalTicks", this.placementIntervalTicks);
            json.addProperty("smartRotationEnabled", this.smartRotationEnabled);
            json.addProperty("carpetAccuratePlacement", this.carpetAccuratePlacement);
            json.addProperty("inventoryBufferCache", this.inventoryBufferCache);
            json.addProperty("breakWrongBlocks", this.breakWrongBlocks);
            json.addProperty("strictRaycast", this.strictRaycast);
            json.addProperty("showHud", this.showHud);
            json.addProperty("hudPosX", this.hudPosX);
            json.addProperty("hudPosY", this.hudPosY);

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            LitematicaPrinterProMod.LOGGER.error("[{}] Failed to save configuration: {}", LitematicaPrinterProMod.MOD_NAME, e.getMessage());
        }
    }

    // Getters and Setters
    public boolean isPrinterEnabled() { return printerEnabled; }
    public void setPrinterEnabled(boolean printerEnabled) {
        this.printerEnabled = printerEnabled;
        saveConfig();
    }

    public double getPlacementRange() { return placementRange; }
    public void setPlacementRange(double placementRange) {
        this.placementRange = placementRange;
        saveConfig();
    }

    public int getBlocksPerTick() { return blocksPerTick; }
    public void setBlocksPerTick(int blocksPerTick) {
        this.blocksPerTick = Math.max(1, Math.min(20, blocksPerTick));
        saveConfig();
    }

    public int getPlacementIntervalTicks() { return placementIntervalTicks; }
    public void setPlacementIntervalTicks(int placementIntervalTicks) {
        this.placementIntervalTicks = Math.max(0, placementIntervalTicks);
        saveConfig();
    }

    public boolean isSmartRotationEnabled() { return smartRotationEnabled; }
    public void setSmartRotationEnabled(boolean smartRotationEnabled) {
        this.smartRotationEnabled = smartRotationEnabled;
        saveConfig();
    }

    public boolean isCarpetAccuratePlacement() { return carpetAccuratePlacement; }
    public void setCarpetAccuratePlacement(boolean carpetAccuratePlacement) {
        this.carpetAccuratePlacement = carpetAccuratePlacement;
        saveConfig();
    }

    public boolean isInventoryBufferCache() { return inventoryBufferCache; }
    public void setInventoryBufferCache(boolean inventoryBufferCache) {
        this.inventoryBufferCache = inventoryBufferCache;
        saveConfig();
    }

    public boolean isBreakWrongBlocks() { return breakWrongBlocks; }
    public void setBreakWrongBlocks(boolean breakWrongBlocks) {
        this.breakWrongBlocks = breakWrongBlocks;
        saveConfig();
    }

    public boolean isStrictRaycast() { return strictRaycast; }
    public void setStrictRaycast(boolean strictRaycast) {
        this.strictRaycast = strictRaycast;
        saveConfig();
    }

    public boolean isShowHud() { return showHud; }
    public void setShowHud(boolean showHud) {
        this.showHud = showHud;
        saveConfig();
    }

    public int getHudPosX() { return hudPosX; }
    public void setHudPosX(int hudPosX) { this.hudPosX = hudPosX; }

    public int getHudPosY() { return hudPosY; }
    public void setHudPosY(int hudPosY) { this.hudPosY = hudPosY; }
}
