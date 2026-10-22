package com.slopggobbler.litematica.compat;

import com.slopggobbler.litematica.config.PrinterConfigHandler;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fi.dy.masa.malilib.config.gui.GuiModConfigs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new GuiModConfigs("litematica_printer_pro", PrinterConfigHandler.OPTIONS, "Litematica Printer Pro");
    }
}
