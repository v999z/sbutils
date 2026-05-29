package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.feature.AutoTip;
import com.v999.sbutils.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;

import java.util.List;

public class FeaturesPage extends AbstractPage {
    public FeaturesPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Auto tip", "Send /tipall regularly when in Hypixel.", ConfigManager.FEATURES.ENABLE_AUTO_TIP, newValue -> {
                    ConfigManager.FEATURES.ENABLE_AUTO_TIP = newValue;
                    if (ConfigManager.FEATURES.ENABLE_AUTO_TIP) {
                        AutoTip.INSTANCE.setupTask();
                        SbutilsClient.moduleList.showModule(AutoTip.INSTANCE);
                    }
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Foraging style warning", "Warn when you chop trees in the wrong order to prevent the loss of foraging efficiency.", ConfigManager.FEATURES.ENABLE_FORAGING_STYLE_WARNING, newValue -> {
                    ConfigManager.FEATURES.ENABLE_FORAGING_STYLE_WARNING = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Entrance notifier", "Pop up a system notification when entering Dungeon/Kuudra if the game window is not focused.", ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER, newValue -> {
                    ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Force toggle use on whitelisted items", "Uses toggle mode only for item IDs you add with /sbutils whitelist add ITEM_ID.", ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE, newValue -> {
                    ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Freelook", "Press Left Alt to toggle third person freelook without turning your character.", ConfigManager.FEATURES.ENABLE_FREELOOK, newValue -> {
                    ConfigManager.FEATURES.ENABLE_FREELOOK = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Etherwarp Helper", "Left click with AOTV / AOTE to auto-shift and right click.", ConfigManager.FEATURES.ENABLE_ETHERWARP_HELPER, newValue -> {
                    ConfigManager.FEATURES.ENABLE_ETHERWARP_HELPER = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Auto conversation", "Automatically advances NPC conversations only when there is exactly one continue option.", ConfigManager.FEATURES.ENABLE_AUTO_CONVERSATION, newValue -> {
                    ConfigManager.FEATURES.ENABLE_AUTO_CONVERSATION = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Prevent attacking on Goons", "Reduce worries when Aura / Dante is in power.", ConfigManager.FEATURES.ENABLE_PREVENT_ATTACKING_ON_GOONS, newValue -> {
                    ConfigManager.FEATURES.ENABLE_PREVENT_ATTACKING_ON_GOONS = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Slayer boss helper", "Show alerts for Slayer quest starts, minibosses, boss spawns, and boss clears.", ConfigManager.FEATURES.ENABLE_SLAYER_BOSS_HELPER, newValue -> {
                    ConfigManager.FEATURES.ENABLE_SLAYER_BOSS_HELPER = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "0 Ping Dungeonbreaker", "Ignore mining fatigue when holding Dungeonbreaker in Dungeons.", ConfigManager.PATCHES.ZERO_PING_DUNGEONBREAKER, newValue -> {
                    ConfigManager.PATCHES.ZERO_PING_DUNGEONBREAKER = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Cancel shortbow pull animation", "Avoid pulling shortbow when you have arrows in your inventory.", ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL, newValue -> {
                    ConfigManager.PATCHES.CANCEL_SHORTBOW_PULL = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "No command execution confirmation", "Skip the confirmation dialog from Mojang. Just execute it.", ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION, newValue -> {
                    ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                })
        ), 4F, LAYER_DEPTH + 1));
    }
}
