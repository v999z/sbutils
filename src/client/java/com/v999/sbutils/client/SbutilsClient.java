package com.v999.sbutils.client;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import com.v999.sbutils.client.compat.Compat;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.feature.*;
import com.v999.sbutils.client.feature.kuudra.KuudraAutoPearl;
import com.v999.sbutils.client.ui.RoundRectRenderer;
import com.v999.sbutils.client.ui.clickgui.ClickGUIScreen;
import com.v999.sbutils.client.ui.container.ServerTPSContainer;
import com.v999.sbutils.client.ui.font.FontManager;
import com.v999.sbutils.client.ui.hud.HudEditorScreen;
import com.v999.sbutils.client.ui.hud.PerformanceHud;
import com.v999.sbutils.client.ui.island.HudDynamicIsland;
import com.v999.sbutils.client.ui.modulelist.HudModuleList;
import com.v999.sbutils.client.ui.speeddial.HudSpeedDial;
import com.v999.sbutils.client.util.ClientTaskScheduler;
import com.v999.sbutils.client.util.NicknameHider;
import com.v999.sbutils.client.util.SoundAssetManager;
import com.v999.sbutils.client.util.SkyblockItem;
import com.v999.sbutils.client.util.SkyblockLocation;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class SbutilsClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder()
            .setFormattingStyle(FormattingStyle.COMPACT.withNewline("\n"))
            .create();
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("sbutils", "main"));

    public static final Identifier POST_PHASE = Identifier.fromNamespaceAndPath("sbutils", "post");

    public static final HudDynamicIsland island = new HudDynamicIsland();
    public static final HudModuleList moduleList = new HudModuleList();
    public static final HudSpeedDial speedDial = new HudSpeedDial();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.addPhaseOrdering(Event.DEFAULT_PHASE, POST_PHASE);

        ConfigManager.init();
        SoundAssetManager.ensureDefaultSounds();
        AutoUpdater.init();
        Compat.init();
        FontManager.init();
        RoundRectRenderer.init();

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("sbutils", "dynamic_island"),
                island);
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("sbutils", "performance_hud"),
                PerformanceHud.INSTANCE);
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("sbutils", "module_list"),
                moduleList);
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("sbutils", "speed_dial"),
                speedDial);

        ClientTickEvents.START_CLIENT_TICK.register(AutoUpdater::onClientTick);
        ClientTickEvents.START_CLIENT_TICK.register(GhostPickaxe.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ServerTPSContainer.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ClientTaskScheduler::whenClientStartTick);
        ClientTickEvents.START_CLIENT_TICK.register(DayViewer.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ClickGUI.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(SpeedDial.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(BetterDungeonbreaker.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(ToggleUse.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(FreeLook.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(Reminders.INSTANCE::onClientTick);
        ClientTickEvents.START_CLIENT_TICK.register(EtherwarpHelper.INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(KuudraAutoPearl.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(EtherwarpHelper.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(PickobulusPreview.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(DroppedItemGlow.INSTANCE);
        LevelRenderEvents.BEFORE_GIZMOS.register(DroppedItemGlow.INSTANCE);
        ClientTickEvents.END_CLIENT_TICK.register(POST_PHASE, SkyblockItem::clearCache);
        ClientReceiveMessageEvents.GAME.register(SimpleChatEventHandler.INSTANCE);
        ClientPlayConnectionEvents.INIT.register(LobbyHistory.INSTANCE);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            AutoUpdater.onClientStopping();
            ConfigManager.processChanges();
        });
        LevelRenderEvents.END_EXTRACTION.register(PickobulusPreview.INSTANCE);
        LevelRenderEvents.BEFORE_GIZMOS.register(PickobulusPreview.INSTANCE);
        ScreenEvents.BEFORE_INIT.register(SpeedDial.INSTANCE);
        AttackEntityCallback.EVENT.register(GoonBlocker.INSTANCE);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            var builder = literal("sbutils")
                    .executes(context -> {
                        var client = net.minecraft.client.Minecraft.getInstance();
                        client.execute(() -> client.setScreen(new ClickGUIScreen(client, client.screen)));
                        return 1;
                    })
                    .then(literal("resetLifeTimer")
                            .executes(context -> {
                                LifeSaverTimer.INSTANCE.reset();
                                return 0;
                            }))
                    .then(literal("resetAutoTip")
                            .executes(context -> {
                                AutoTip.INSTANCE.reset();
                                return 0;
                            }))
                    .then(literal("reminders")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("[Sbutils] Reminders are " + (ConfigManager.FEATURES.ENABLE_REMINDERS ? "enabled" : "disabled") +
                                        ". Enabled: " + Reminders.enabledReminderDisplay() + ". Custom: " + Reminders.customReminderCountDisplay()));
                                return 0;
                            })
                            .then(literal("test")
                                    .executes(context -> {
                                        Reminders.INSTANCE.testNow();
                                        return 0;
                                    }))
                            .then(literal("done")
                                    .executes(context -> {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Reminder presets: " + Reminders.presetListDisplay()));
                                        return 0;
                                    })
                                    .then(argument("preset", StringArgumentType.string())
                                            .executes(context -> {
                                                String preset = StringArgumentType.getString(context, "preset");
                                                if (Reminders.markPresetDone(preset)) {
                                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Marked reminder preset as done: " + preset));
                                                } else {
                                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Unknown reminder preset. Available: " + Reminders.presetListDisplay()));
                                                }
                                                return 0;
                                            }))))
                    .then(literal("tps")
                            .executes(context -> {
                                ServerTPSContainer.INSTANCE.whenRespawn();
                                return 0;
                            }))
                    .then(literal("hud")
                            .executes(context -> {
                                var client = net.minecraft.client.Minecraft.getInstance();
                                client.setScreen(new HudEditorScreen(client, client.screen));
                                return 0;
                            }))
                    .then(literal("nickhider")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("[Sbutils] Nickname hider is " + (ConfigManager.FEATURES.ENABLE_NICKNAME_HIDER ? "enabled" : "disabled") +
                                        ". Alias: " + NicknameHider.getAlias()));
                                return 0;
                            })
                            .then(literal("on")
                                    .executes(context -> {
                                        ConfigManager.FEATURES.ENABLE_NICKNAME_HIDER = true;
                                        if (ConfigManager.FEATURES.NICKNAME_HIDER_ALIAS == null || ConfigManager.FEATURES.NICKNAME_HIDER_ALIAS.isBlank()) {
                                            ConfigManager.FEATURES.NICKNAME_HIDER_ALIAS = NicknameHider.DEFAULT_ALIAS;
                                        }
                                        ConfigManager.FEATURES.markAsChanged();
                                        ConfigManager.processChanges();
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Nickname hider enabled."));
                                        return 0;
                                    }))
                            .then(literal("off")
                                    .executes(context -> {
                                        ConfigManager.FEATURES.ENABLE_NICKNAME_HIDER = false;
                                        ConfigManager.FEATURES.markAsChanged();
                                        ConfigManager.processChanges();
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Nickname hider disabled."));
                                        return 0;
                                    }))
                            .then(literal("set")
                                    .then(argument("alias", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String alias = StringArgumentType.getString(context, "alias").trim();
                                                ConfigManager.FEATURES.NICKNAME_HIDER_ALIAS = alias.isEmpty() ? NicknameHider.DEFAULT_ALIAS : alias;
                                                ConfigManager.FEATURES.markAsChanged();
                                                ConfigManager.processChanges();
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Nickname hider alias set to " + NicknameHider.getAlias()));
                                                return 0;
                                            }))))
                    .then(literal("whitelist")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("[Sbutils] Toggle-use whitelist: " + whitelistDisplay()));
                                return 0;
                            })
                            .then(literal("add")
                                    .then(argument("item_id", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String itemId = ToggleUse.normalize(StringArgumentType.getString(context, "item_id"));
                                                if (itemId.isEmpty()) {
                                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Item ID cannot be empty."));
                                                    return 0;
                                                }
                                                if (!ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST.contains(itemId)) {
                                                    ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST.add(itemId);
                                                    ConfigManager.FEATURES.markAsChanged();
                                                    ConfigManager.processChanges();
                                                }
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Added to toggle-use whitelist: " + itemId));
                                                return 0;
                                            })))
                            .then(literal("remove")
                                    .then(argument("item_id", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String itemId = ToggleUse.normalize(StringArgumentType.getString(context, "item_id"));
                                                ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST.removeIf(entry -> ToggleUse.normalize(entry).equals(itemId));
                                                ConfigManager.FEATURES.markAsChanged();
                                                ConfigManager.processChanges();
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Removed from toggle-use whitelist: " + itemId));
                                                return 0;
                                            })))
                            .then(literal("list")
                                    .executes(context -> {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Toggle-use whitelist: " + whitelistDisplay()));
                                        return 0;
                                    }))
                            .then(literal("clear")
                                    .executes(context -> {
                                        ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST.clear();
                                        ConfigManager.FEATURES.markAsChanged();
                                        ConfigManager.processChanges();
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Cleared toggle-use whitelist."));
                                        return 0;
                                    })))
                    .then(literal("glowfilter")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("[Sbutils] Dropped item glow filters: " + DroppedItemGlow.filterListDisplay()));
                                context.getSource().sendFeedback(Component.literal("[Sbutils] Filter mode is " + (ConfigManager.FEATURES.DROPPED_ITEM_GLOW_ONLY_MATCHING ? "enabled" : "disabled") + ". Use /sbutils glowfilter on to only glow matching items."));
                                return 0;
                            })
                            .then(literal("on")
                                    .executes(context -> {
                                        ConfigManager.FEATURES.DROPPED_ITEM_GLOW_ONLY_MATCHING = true;
                                        ConfigManager.FEATURES.markAsChanged();
                                        ConfigManager.processChanges();
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Dropped item glow filters enabled."));
                                        return 0;
                                    }))
                            .then(literal("off")
                                    .executes(context -> {
                                        ConfigManager.FEATURES.DROPPED_ITEM_GLOW_ONLY_MATCHING = false;
                                        ConfigManager.FEATURES.markAsChanged();
                                        ConfigManager.processChanges();
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Dropped item glow filters disabled. All dropped items will glow."));
                                        return 0;
                                    }))
                            .then(literal("colors")
                                    .then(literal("on")
                                            .executes(context -> {
                                                ConfigManager.FEATURES.DROPPED_ITEM_GLOW_RARITY_COLORS = true;
                                                ConfigManager.FEATURES.markAsChanged();
                                                ConfigManager.processChanges();
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Dropped item glow rarity colors enabled."));
                                                return 0;
                                            }))
                                    .then(literal("off")
                                            .executes(context -> {
                                                ConfigManager.FEATURES.DROPPED_ITEM_GLOW_RARITY_COLORS = false;
                                                ConfigManager.FEATURES.markAsChanged();
                                                ConfigManager.processChanges();
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Dropped item glow rarity colors disabled."));
                                                return 0;
                                            })))
                            .then(literal("add")
                                    .then(argument("filter", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String filter = DroppedItemGlow.normalizeFilter(StringArgumentType.getString(context, "filter"));
                                                if (filter.isEmpty()) {
                                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Glow filter cannot be empty."));
                                                    return 0;
                                                }
                                                if (ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS == null) {
                                                    ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS = new java.util.ArrayList<>();
                                                }
                                                if (ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS.stream().noneMatch(entry -> DroppedItemGlow.normalizeFilter(entry).equals(filter))) {
                                                    ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS.add(filter);
                                                    ConfigManager.FEATURES.markAsChanged();
                                                    ConfigManager.processChanges();
                                                }
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Added dropped item glow filter: " + filter));
                                                return 0;
                                            })))
                            .then(literal("remove")
                                    .then(argument("filter", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String filter = DroppedItemGlow.normalizeFilter(StringArgumentType.getString(context, "filter"));
                                                if (ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS != null) {
                                                    ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS.removeIf(entry -> DroppedItemGlow.normalizeFilter(entry).equals(filter));
                                                    ConfigManager.FEATURES.markAsChanged();
                                                    ConfigManager.processChanges();
                                                }
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Removed dropped item glow filter: " + filter));
                                                return 0;
                                            })))
                            .then(literal("list")
                                    .executes(context -> {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Dropped item glow filters: " + DroppedItemGlow.filterListDisplay()));
                                        return 0;
                                    }))
                            .then(literal("clear")
                                    .executes(context -> {
                                        if (ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS != null) {
                                            ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS.clear();
                                        }
                                        ConfigManager.FEATURES.markAsChanged();
                                        ConfigManager.processChanges();
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Cleared dropped item glow filters."));
                                        return 0;
                                    }))
                            .then(literal("preset")
                                    .executes(context -> {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Glow presets: " + DroppedItemGlow.presetListDisplay()));
                                        return 0;
                                    })
                                    .then(argument("preset", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String preset = StringArgumentType.getString(context, "preset");
                                                if (DroppedItemGlow.applyPreset(preset)) {
                                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Applied dropped item glow preset: " + DroppedItemGlow.normalize(preset)));
                                                } else {
                                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Unknown glow preset. Available: " + DroppedItemGlow.presetListDisplay()));
                                                }
                                                return 0;
                                            }))))
                    .then(literal("configSave")
                            .executes(context -> {
                                ConfigManager.processChanges();
                                context.getSource().sendFeedback(Component.literal("[Sbutils] Processed config changes."));
                                return 0;
                            }))
                    .then(literal("whereAmI")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("[Sbutils] Location=\"" + SkyblockLocation.LOCATION_STRING +
                                        "\", isInDungeons=" + SkyblockLocation.isInDungeons()));
                                return 0;
                            }))
                    .then(literal("update")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("[Sbutils] " + AutoUpdater.getStatusLine()));
                                return 0;
                            })
                            .then(literal("status")
                                    .executes(context -> {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] " + AutoUpdater.getStatusLine()));
                                        return 0;
                                    }))
                            .then(literal("check")
                                    .executes(context -> {
                                        AutoUpdater.checkForUpdatesAsync(true);
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Checking GitHub releases..."));
                                        return 0;
                                    }))
                            .then(literal("on")
                                    .executes(context -> {
                                        ConfigManager.GENERAL.AUTO_UPDATE_ENABLED = true;
                                        ConfigManager.GENERAL.markAsChanged();
                                        ConfigManager.processChanges();
                                        AutoUpdater.setEnabledState(true);
                                        AutoUpdater.checkForUpdatesAsync(true);
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Auto updater enabled."));
                                        return 0;
                                    })));
            Reminders.registerCommand(builder);
            moduleList.registerCommand(builder);
            var command = dispatcher.register(builder);
            dispatcher.register(literal("gy").redirect(command));
        });
    }

    private static String whitelistDisplay() {
        if (ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST == null || ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST.isEmpty()) {
            return "(empty)";
        }
        return String.join(", ", ToggleUse.normalizeAll(ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST));
    }
}
