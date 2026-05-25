package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;

import java.util.List;

public class KuudraPage extends AbstractPage {
    public KuudraPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Auto pearl", "Throws a pearl when you are holding Elle's Supplies or a Ballista Fuel Cell in slot 9.", ConfigManager.FEATURES.ENABLE_KUUDRA_AUTO_PEARL, newValue -> {
                    ConfigManager.FEATURES.ENABLE_KUUDRA_AUTO_PEARL = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                })
        ), 4F, LAYER_DEPTH + 1));
    }
}
