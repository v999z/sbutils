package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.feature.DayViewer;
import com.v999.sbutils.client.feature.DroppedItemGlow;
import com.v999.sbutils.client.feature.RNGDrop;
import com.v999.sbutils.client.feature.UsernameMentionSound;
import com.v999.sbutils.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import java.util.List;

public class VisualsPage extends AbstractPage {
    public VisualsPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Day viewer", "Display the date of the current world.", ConfigManager.FEATURES.ENABLE_DAY_VIEWER, newValue -> {
                    ConfigManager.FEATURES.ENABLE_DAY_VIEWER = newValue;
                    if (ConfigManager.FEATURES.ENABLE_DAY_VIEWER) {
                        SbutilsClient.moduleList.showModule(DayViewer.INSTANCE);
                    }
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Dropped item glow", "Highlights dropped items using Minecraft's glowing outline so you can see them through walls.", ConfigManager.FEATURES.ENABLE_DROPPED_ITEM_GLOW, newValue -> {
                    ConfigManager.FEATURES.ENABLE_DROPPED_ITEM_GLOW = newValue;
                    if (ConfigManager.FEATURES.ENABLE_DROPPED_ITEM_GLOW) {
                        SbutilsClient.moduleList.showModule(DroppedItemGlow.INSTANCE);
                    }
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Compact duplicate messages", "Merge repeated chat messages into a single line with an occurrence counter like (x3).", ConfigManager.FEATURES.ENABLE_COMPACT_DUPLICATE_MESSAGES, newValue -> {
                    ConfigManager.FEATURES.ENABLE_COMPACT_DUPLICATE_MESSAGES = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Chat compactor color",
                        () -> "Current: " + ClickGuiTheme.compactCounterColorDisplayName() + ". Click to cycle.",
                        ClickGuiTheme::cycleCompactCounterColor),
                new ModuleItemView(client, "Nickname hider", "Replace your username in rendered text with a custom alias.", ConfigManager.FEATURES.ENABLE_NICKNAME_HIDER, newValue -> {
                    ConfigManager.FEATURES.ENABLE_NICKNAME_HIDER = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Nickname hider alias",
                        () -> "Current: " + ConfigManager.FEATURES.NICKNAME_HIDER_ALIAS + ". Click to edit with /sbutils nickhider set",
                        () -> client.setScreen(new ChatScreen("/sbutils nickhider set " + ConfigManager.FEATURES.NICKNAME_HIDER_ALIAS, true))),
                new ModuleItemView(client, "RNG drop summary", "Notify the RNG drop, and play the music at \"config/Sbutils/rng_music.ogg\". GG!", ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY, newValue -> {
                    ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "RNG drop summary - Test", "Play the current RNG drop sound file now.", () -> RNGDrop.playPreview()),
                new ModuleItemView(client, "Username mention sound", "Play the music at \"config/Sbutils/user_music.ogg\" whenever someone mentions your username.", ConfigManager.FEATURES.ENABLE_USERNAME_MENTION_SOUND, newValue -> {
                    ConfigManager.FEATURES.ENABLE_USERNAME_MENTION_SOUND = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Username mention sound - Test", "Play the current username mention sound file now.", () -> UsernameMentionSound.playPreview()),
                new ModuleItemView(client, "Dynamic Island chat alerts", "Show Dynamic Island popups for rare drops, username mentions, private messages, and sold auctions.", ConfigManager.FEATURES.ENABLE_DYNAMIC_ISLAND_CHAT_ALERTS, newValue -> {
                    ConfigManager.FEATURES.ENABLE_DYNAMIC_ISLAND_CHAT_ALERTS = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Performance HUD", "Show FPS / ping / TPS in a movable HUD box.", ConfigManager.FEATURES.ENABLE_PERFORMANCE_HUD, newValue -> {
                    ConfigManager.FEATURES.ENABLE_PERFORMANCE_HUD = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Performance HUD - Show FPS", "Toggle the FPS readout.", ConfigManager.FEATURES.SHOW_PERFORMANCE_FPS, newValue -> {
                    ConfigManager.FEATURES.SHOW_PERFORMANCE_FPS = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Performance HUD - Show ping", "Toggle the ping readout.", ConfigManager.FEATURES.SHOW_PERFORMANCE_PING, newValue -> {
                    ConfigManager.FEATURES.SHOW_PERFORMANCE_PING = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Performance HUD - Show TPS", "Toggle the TPS readout.", ConfigManager.FEATURES.SHOW_PERFORMANCE_TPS, newValue -> {
                    ConfigManager.FEATURES.SHOW_PERFORMANCE_TPS = newValue;
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Performance HUD - Background color",
                        () -> "Current: " + com.v999.sbutils.client.ui.hud.PerformanceHud.backgroundColorDisplayName() + ". Click to cycle.",
                        com.v999.sbutils.client.ui.hud.PerformanceHud::cycleBackgroundColor),
                new ModuleItemView(client, "Always use spectator fog", "Clear in-lava/in-powder-snow camera.", ConfigManager.PATCHES.USE_SPECTATOR_FOG, newValue -> {
                    ConfigManager.PATCHES.USE_SPECTATOR_FOG = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                }),
                new ModuleItemView(client, "Remove suffocation screen", "Remove the block texture that covers your whole screen when suffocating.", ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN, newValue -> {
                    ConfigManager.PATCHES.REMOVE_SUFFOCATION_SCREEN = newValue;
                    ConfigManager.PATCHES.markAsChanged();
                })
        ), 4F, LAYER_DEPTH + 1));
    }
}