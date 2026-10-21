package com.slopggobbler.litematica.test;

import com.slopggobbler.litematica.config.PrinterConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PrinterPresetTest {

    @Test
    public void testPrinterPresetsAndRoundTrip() {
        // Test Minimalist / Performance Preset
        PrinterConfig.Generic.PRINTER_ENABLED.setBooleanValue(true);
        PrinterConfig.Generic.BLOCKS_PER_TICKS.setIntegerValue(5);
        PrinterConfig.Generic.STRICT_RAYCAST.setBooleanValue(false);

        assertTrue(PrinterConfig.Generic.PRINTER_ENABLED.getBooleanValue());
        assertEquals(5, PrinterConfig.Generic.BLOCKS_PER_TICKS.getIntegerValue());
        assertFalse(PrinterConfig.Generic.STRICT_RAYCAST.getBooleanValue());

        // Test Power User Preset
        PrinterConfig.Generic.BLOCKS_PER_TICKS.setIntegerValue(20);
        PrinterConfig.Generic.SMART_ROTATION.setBooleanValue(true);
        PrinterConfig.Generic.CARPET_ACCURATE_PLACEMENT.setBooleanValue(true);

        assertEquals(20, PrinterConfig.Generic.BLOCKS_PER_TICKS.getIntegerValue());
        assertTrue(PrinterConfig.Generic.SMART_ROTATION.getBooleanValue());
        assertTrue(PrinterConfig.Generic.CARPET_ACCURATE_PLACEMENT.getBooleanValue());

        // Test Safe / Recording Preset
        PrinterConfig.Generic.STRICT_RAYCAST.setBooleanValue(true);
        PrinterConfig.Generic.BLOCKS_PER_TICKS.setIntegerValue(2);

        assertTrue(PrinterConfig.Generic.STRICT_RAYCAST.getBooleanValue());
        assertEquals(2, PrinterConfig.Generic.BLOCKS_PER_TICKS.getIntegerValue());
    }
}
