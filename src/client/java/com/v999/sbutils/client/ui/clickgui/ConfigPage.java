package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ConfigPage extends AbstractPage {
    public ConfigPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Save config now", "Writes pending SBUtils settings to disk.",
                        ConfigManager::processChanges),
                new ModuleItemView(client, "Open config folder", "Open config/Sbutils to edit JSON, contacts, and sound files.",
                        ConfigManager::openConfigFolder)
        ), 4F, LAYER_DEPTH + 1));
    }
}
