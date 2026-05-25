package com.v999.sbutils.client;

import com.v999.sbutils.client.ui.clickgui.ClickGUIScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;

public class SbutilsModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new ClickGUIScreen(Minecraft.getInstance(), screen);
    }
}
