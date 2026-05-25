package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.ui.clickgui.fubuki.list.ListView;
import com.v999.sbutils.client.ui.hud.HudEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;

import java.util.List;

public class GeneralPage extends AbstractPage {
    public GeneralPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Background blur", "Blur the background while in UI.", ConfigManager.GENERAL.CLICK_GUI_BLUR, newValue -> {
                    ConfigManager.GENERAL.CLICK_GUI_BLUR = newValue;
                    ConfigManager.GENERAL.markAsChanged();
                }),
                new ModuleItemView(client, "ClickGUI theme",
                        () -> "Current: " + ClickGuiTheme.themeDisplayName() + ". Click to cycle.",
                        ClickGuiTheme::toggleMode),
                new ModuleItemView(client, "Accent color",
                        () -> "Current: " + ClickGuiTheme.accentDisplayName() + ". Click to cycle.",
                        ClickGuiTheme::cycleAccent),
                new ModuleItemView(client, "Compact mode", "Use smaller padding, labels, gaps, and navigation so more settings fit on screen.", ConfigManager.GENERAL.CLICK_GUI_COMPACT_MODE, newValue -> {
                    ConfigManager.GENERAL.CLICK_GUI_COMPACT_MODE = newValue;
                    ConfigManager.GENERAL.markAsChanged();
                }),
                new ModuleItemView(client, "HUD editor", "Drag the Dynamic Island and performance HUD around.",
                        () -> client.setScreen(new HudEditorScreen(client, client.screen))),
                new ModuleItemView(client, "Press \"Speed Dial - Right\" key in game to open the speed dial menu.", "Open Abiphone once to sync your contact data. Click here for more info.",
                        () -> ConfirmLinkScreen.confirmLinkNow(client.screen, "https://github.com/v999z/sbutils/blob/master/speeddial.md", true)),
                new ModuleItemView(client, "GitHub", "Report bugs, publish releases, and share update builds.",
                        () -> ConfirmLinkScreen.confirmLinkNow(client.screen, "https://github.com/v999z/sbutils", true))
        ), 4F, LAYER_DEPTH + 1));
    }
}
