package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.feature.Reminders;
import com.v999.sbutils.client.ui.clickgui.fubuki.list.ListView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import java.util.List;

public class RemindersPage extends AbstractPage {
    private static boolean reminderTypesExpanded = true;
    private static boolean customRemindersExpanded = false;
    private static boolean behaviorExpanded = false;

    public RemindersPage(Minecraft client) {
        super(client, new ListView<>(List.of(
                new ModuleItemView(client, "Reminders", "Send occasional SkyBlock chore reminders in chat.", ConfigManager.FEATURES.ENABLE_REMINDERS, newValue -> {
                    ConfigManager.FEATURES.ENABLE_REMINDERS = newValue;
                    if (ConfigManager.FEATURES.ENABLE_REMINDERS) {
                        Reminders.INSTANCE.scheduleSoon();
                        SbutilsClient.moduleList.showModule(Reminders.INSTANCE);
                    }
                    ConfigManager.FEATURES.markAsChanged();
                }),
                new ModuleItemView(client, "Reminder types",
                        () -> (reminderTypesExpanded ? "Expanded" : "Collapsed") + ". Enabled: " + Reminders.enabledReminderDisplay(),
                        () -> reminderTypesExpanded,
                        () -> reminderTypesExpanded = !reminderTypesExpanded,
                        List.of(
                                new ModuleItemView(client, "Daily tasks", "Commissions, faction quests, bits, and reset chores.", ConfigManager.FEATURES.REMIND_DAILY_TASKS, newValue -> {
                                    ConfigManager.FEATURES.REMIND_DAILY_TASKS = newValue;
                                    ConfigManager.FEATURES.markAsChanged();
                                }),
                                new ModuleItemView(client, "Experiment Table", "Remind you to do experiments.", ConfigManager.FEATURES.REMIND_EXPERIMENT_TABLE, newValue -> {
                                    ConfigManager.FEATURES.REMIND_EXPERIMENT_TABLE = newValue;
                                    ConfigManager.FEATURES.markAsChanged();
                                }),
                                new ModuleItemView(client, "Century Cakes", "Remind you to refresh cake buffs.", ConfigManager.FEATURES.REMIND_CAKES, newValue -> {
                                    ConfigManager.FEATURES.REMIND_CAKES = newValue;
                                    ConfigManager.FEATURES.markAsChanged();
                                }),
                                new ModuleItemView(client, "Forge and minions", "Forge crafts, minions, fuel, and storage.", ConfigManager.FEATURES.REMIND_FORGE_AND_MINIONS, newValue -> {
                                    ConfigManager.FEATURES.REMIND_FORGE_AND_MINIONS = newValue;
                                    ConfigManager.FEATURES.markAsChanged();
                                })
                        )),
                new ModuleItemView(client, "Custom reminders",
                        () -> (customRemindersExpanded ? "Expanded" : "Collapsed") + ". " + Reminders.customReminderCountDisplay(),
                        () -> customRemindersExpanded,
                        () -> customRemindersExpanded = !customRemindersExpanded,
                        List.of(
                                new ModuleItemView(client, "Add custom reminder", "Use /sbutils reminder <time> <message>. Example: 10m check forge.",
                                        () -> client.setScreen(new ChatScreen("/sbutils reminder ", true))),
                                new ModuleItemView(client, "List custom reminders", "Show all custom reminders in chat.",
                                        () -> client.setScreen(new ChatScreen("/sbutils reminder list", true))),
                                new ModuleItemView(client, "Manage custom reminders", "Commands: remove <id>, edit <id> <message>, move <id> <time>.",
                                        () -> client.setScreen(new ChatScreen("/sbutils reminder ", true)))
                        )),
                new ModuleItemView(client, "Reminder behavior",
                        () -> behaviorExpanded ? "Expanded" : "Collapsed",
                        () -> behaviorExpanded,
                        () -> behaviorExpanded = !behaviorExpanded,
                        List.of(
                                new ModuleItemView(client, "SkyBlock only", "Only remind when SBUtils can see a SkyBlock area on the tab list.", ConfigManager.FEATURES.REMINDERS_SKYBLOCK_ONLY, newValue -> {
                                    ConfigManager.FEATURES.REMINDERS_SKYBLOCK_ONLY = newValue;
                                    ConfigManager.FEATURES.markAsChanged();
                                }),
                                new ModuleItemView(client, "Mark preset done", "Use /sbutils reminders done PRESET to reset a preset timer.",
                                        () -> client.setScreen(new ChatScreen("/sbutils reminders done ", true))),
                                new ModuleItemView(client, "Test reminder", "Send the first enabled reminder in chat now.", Reminders.INSTANCE::testNow)
                        ))
        ), 4F, LAYER_DEPTH + 1));
    }
}
