package com.slopggobbler.litematica.test;

import com.slopggobbler.litematica.config.PrinterConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PrinterPresetTest {

    @Test
    public void testPrinterPresetsAndRoundTrip() {
        PrinterConfig config = PrinterConfig.getInstance();

        // Test Minimalist / Performance Preset
        config.setPrinterEnabled(true);
        config.setBlocksPerTick(5);
        config.setStrictRaycast(false);

        assertTrue(config.isPrinterEnabled());
        assertEquals(5, config.getBlocksPerTick());
        assertFalse(config.isStrictRaycast());

        // Test Power User Preset
        config.setBlocksPerTick(20);
        config.setSmartRotationEnabled(true);
        config.setCarpetAccuratePlacement(true);

        assertEquals(20, config.getBlocksPerTick());
        assertTrue(config.isSmartRotationEnabled());
        assertTrue(config.isCarpetAccuratePlacement());

        // Test Safe / Recording Preset
        config.setStrictRaycast(true);
        config.setBlocksPerTick(2);

        assertTrue(config.isStrictRaycast());
        assertEquals(2, config.getBlocksPerTick());
    }
}
