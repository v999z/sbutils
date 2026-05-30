package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.config.ReminderEntry;
import com.v999.sbutils.client.util.SkyblockLocation;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class Reminders extends AbstractModule {
    public static final Reminders INSTANCE = new Reminders();

    private static final long LOGIN_REMINDER_DELAY_MS = 90 * 1000L;
    private static final long MESSAGE_GAP_MS = 30 * 1000L;
    private static final long CUSTOM_REPEAT_INTERVAL_MS = 5 * 60 * 1000L;
    private static final long PRESET_REPEAT_INTERVAL_MS = 5 * 60 * 1000L;
    private static final Pattern DURATION_PART = Pattern.compile("(\\d+)([smhd])");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault());

    private long enabledAfterTimestamp = 0L;
    private long lastMessageTimestamp = 0L;

    static {
        if (ConfigManager.FEATURES.ENABLE_REMINDERS || !customReminders().isEmpty()) {
            SbutilsClient.moduleList.showModule(INSTANCE);
        }
    }

    public void whenServerBrandUpdate(String brand) {
        if (SkyblockLocation.isInHypixel()) {
            scheduleSoon();
        } else {
            enabledAfterTimestamp = 0L;
        }
    }

    public void onClientTick(Minecraft client) {
        if (client.player == null) return;

        long now = Util.getMillis();
        if (sendDueCustomReminder(client, now)) return;

        if (!ConfigManager.FEATURES.ENABLE_REMINDERS) return;
        if (!canRemind(client)) return;
        if (enabledAfterTimestamp <= 0L) {
            enabledAfterTimestamp = now + LOGIN_REMINDER_DELAY_MS;
            return;
        }
        if (now < enabledAfterTimestamp || now - lastMessageTimestamp < MESSAGE_GAP_MS) return;

        for (ReminderType type : enabledReminderTypes()) {
            if (type.isDue(now)) {
                sendReminder(client, type);
                type.markPrompted(now);
                ConfigManager.FEATURES.markAsChanged();
                lastMessageTimestamp = now;
                return;
            }
        }
    }

    public static void registerCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.then(literal("reminder")
                .executes(context -> {
                    sendHelp(context.getSource());
                    return 1;
                })
                .then(literal("list")
                        .executes(context -> {
                            sendCustomReminderList(context.getSource());
                            return 1;
                        }))
                .then(literal("remove")
                        .then(argument("id", StringArgumentType.string())
                                .executes(context -> {
                                    String id = StringArgumentType.getString(context, "id");
                                    ReminderEntry entry = findCustomReminder(id);
                                    if (entry == null) {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Reminder not found: " + id));
                                        return 0;
                                    }
                                    customReminders().remove(entry);
                                    ConfigManager.FEATURES.markAsChanged();
                                    ConfigManager.processChanges();
                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Removed reminder: " + entry.message));
                                    return 1;
                                })))
                .then(literal("edit")
                        .then(argument("id", StringArgumentType.string())
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String id = StringArgumentType.getString(context, "id");
                                            ReminderEntry entry = findCustomReminder(id);
                                            if (entry == null) {
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Reminder not found: " + id));
                                                return 0;
                                            }
                                            entry.message = StringArgumentType.getString(context, "message").trim();
                                            ConfigManager.FEATURES.markAsChanged();
                                            ConfigManager.processChanges();
                                            context.getSource().sendFeedback(Component.literal("[Sbutils] Edited reminder " + entry.id + "."));
                                            return 1;
                                        }))))
                .then(literal("move")
                        .then(argument("id", StringArgumentType.string())
                                .then(argument("time", StringArgumentType.string())
                                        .executes(context -> {
                                            String id = StringArgumentType.getString(context, "id");
                                            ReminderEntry entry = findCustomReminder(id);
                                            if (entry == null) {
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Reminder not found: " + id));
                                                return 0;
                                            }
                                            long duration = parseDurationMs(StringArgumentType.getString(context, "time"));
                                            if (duration <= 0L) {
                                                context.getSource().sendFeedback(Component.literal("[Sbutils] Invalid time. Try 10m, 2h, 1d, or 1h30m."));
                                                return 0;
                                            }
                                            entry.remindAt = Util.getMillis() + duration;
                                            entry.lastReminder = 0L;
                                            ConfigManager.FEATURES.markAsChanged();
                                            ConfigManager.processChanges();
                                            context.getSource().sendFeedback(Component.literal("[Sbutils] Moved reminder " + entry.id + " to " + formatTimestamp(entry.remindAt) + "."));
                                            return 1;
                                        }))))
                .then(argument("time", StringArgumentType.string())
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(context -> {
                                    long duration = parseDurationMs(StringArgumentType.getString(context, "time"));
                                    String message = StringArgumentType.getString(context, "message").trim();
                                    if (duration <= 0L) {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Invalid time. Try 10m, 2h, 1d, or 1h30m."));
                                        return 0;
                                    }
                                    if (message.isEmpty()) {
                                        context.getSource().sendFeedback(Component.literal("[Sbutils] Reminder message cannot be empty."));
                                        return 0;
                                    }
                                    ReminderEntry entry = new ReminderEntry(newId(), message, Util.getMillis() + duration);
                                    customReminders().add(entry);
                                    SbutilsClient.moduleList.showModule(INSTANCE);
                                    ConfigManager.FEATURES.markAsChanged();
                                    ConfigManager.processChanges();
                                    context.getSource().sendFeedback(Component.literal("[Sbutils] Reminder " + entry.id + " set for " + formatTimestamp(entry.remindAt) + "."));
                                    return 1;
                                }))));
    }

    public void scheduleSoon() {
        enabledAfterTimestamp = Util.getMillis() + LOGIN_REMINDER_DELAY_MS;
    }

    public void testNow() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        List<ReminderType> enabled = enabledReminderTypes();
        if (enabled.isEmpty()) return;
        sendReminder(client, enabled.getFirst());
    }

    public static String enabledReminderDisplay() {
        List<ReminderType> enabled = enabledReminderTypes();
        if (enabled.isEmpty()) return "none";
        List<String> names = new ArrayList<>(enabled.size());
        for (ReminderType type : enabled) {
            names.add(type.shortName);
        }
        return String.join(", ", names);
    }

    public static String timerDisplayName(String id) {
        ReminderType type = ReminderType.byId(id);
        return type == null ? "unknown" : type.intervalName;
    }

    public static boolean markPresetDone(String id) {
        ReminderType type = ReminderType.byId(id);
        if (type == null) return false;
        type.markCompleted(Util.getMillis());
        ConfigManager.FEATURES.markAsChanged();
        ConfigManager.processChanges();
        return true;
    }

    public static String presetListDisplay() {
        List<String> ids = new ArrayList<>(ReminderType.values().length);
        for (ReminderType type : ReminderType.values()) {
            ids.add(type.id);
        }
        return String.join(", ", ids);
    }

    public static String customReminderCountDisplay() {
        int size = customReminders().size();
        return size == 1 ? "1 custom reminder" : size + " custom reminders";
    }

    private boolean canRemind(Minecraft client) {
        return client.player != null
                && SkyblockLocation.isInHypixel()
                && (!ConfigManager.FEATURES.REMINDERS_SKYBLOCK_ONLY || !SkyblockLocation.LOCATION_STRING.isBlank())
                && !enabledReminderTypes().isEmpty();
    }

    private boolean sendDueCustomReminder(Minecraft client, long now) {
        if (now - lastMessageTimestamp < MESSAGE_GAP_MS) return false;
        for (ReminderEntry entry : sortedCustomReminders()) {
            if (entry == null || entry.message == null || entry.message.isBlank()) continue;
            if (entry.remindAt > now) continue;
            if (entry.lastReminder > 0L && now - entry.lastReminder < CUSTOM_REPEAT_INTERVAL_MS) continue;

            entry.lastReminder = now;
            ConfigManager.FEATURES.markAsChanged();
            sendCustomReminder(client, entry);
            lastMessageTimestamp = now;
            return true;
        }
        return false;
    }

    private void sendReminder(Minecraft client, ReminderType type) {
        if (client.player == null) return;
        client.player.sendSystemMessage(Component.literal("[SBUtils Reminder] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(type.message).withStyle(ChatFormatting.YELLOW)));
    }

    private void sendCustomReminder(Minecraft client, ReminderEntry entry) {
        if (client.player == null) return;
        client.player.sendSystemMessage(Component.literal("[SBUtils Reminder] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(entry.message).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" [" + entry.id + "]").withStyle(ChatFormatting.GRAY)));
    }

    private static List<ReminderType> enabledReminderTypes() {
        List<ReminderType> enabled = new ArrayList<>(ReminderType.values().length);
        for (ReminderType type : ReminderType.values()) {
            if (type.isEnabled()) {
                enabled.add(type);
            }
        }
        return enabled;
    }

    private static ArrayList<ReminderEntry> customReminders() {
        if (ConfigManager.FEATURES.CUSTOM_REMINDERS == null) {
            ConfigManager.FEATURES.CUSTOM_REMINDERS = new ArrayList<>();
        }
        return ConfigManager.FEATURES.CUSTOM_REMINDERS;
    }

    private static List<ReminderEntry> sortedCustomReminders() {
        return customReminders().stream()
                .filter(entry -> entry != null && entry.id != null && !entry.id.isBlank())
                .sorted(Comparator.comparingLong(entry -> entry.remindAt))
                .toList();
    }

    private static ReminderEntry findCustomReminder(String id) {
        String normalizedId = id == null ? "" : id.trim();
        if (normalizedId.isEmpty()) return null;
        for (ReminderEntry entry : customReminders()) {
            if (entry == null || entry.id == null) continue;
            if (entry.id.equals(normalizedId) || entry.id.startsWith(normalizedId)) {
                return entry;
            }
        }
        return null;
    }

    private static void sendHelp(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("[Sbutils] Reminder commands:"));
        source.sendFeedback(Component.literal("/sbutils reminder <time> <message>"));
        source.sendFeedback(Component.literal("/sbutils reminder list"));
        source.sendFeedback(Component.literal("/sbutils reminder remove <id>"));
        source.sendFeedback(Component.literal("/sbutils reminder edit <id> <message>"));
        source.sendFeedback(Component.literal("/sbutils reminder move <id> <time>"));
    }

    private static void sendCustomReminderList(FabricClientCommandSource source) {
        List<ReminderEntry> reminders = sortedCustomReminders();
        if (reminders.isEmpty()) {
            source.sendFeedback(Component.literal("[Sbutils] No custom reminders."));
            return;
        }
        source.sendFeedback(Component.literal("[Sbutils] Custom reminders:"));
        for (ReminderEntry reminder : reminders) {
            source.sendFeedback(Component.literal("- " + reminder.id + " at " + formatTimestamp(reminder.remindAt) + ": " + reminder.message));
        }
    }

    private static long parseDurationMs(String input) {
        if (input == null) return -1L;
        String text = input.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) return -1L;

        Matcher matcher = DURATION_PART.matcher(text);
        long total = 0L;
        int position = 0;
        while (matcher.find()) {
            if (matcher.start() != position) return -1L;
            long amount;
            try {
                amount = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1L;
            }
            total += switch (matcher.group(2)) {
                case "s" -> amount * 1000L;
                case "m" -> amount * 60L * 1000L;
                case "h" -> amount * 60L * 60L * 1000L;
                case "d" -> amount * 24L * 60L * 60L * 1000L;
                default -> 0L;
            };
            position = matcher.end();
        }
        if (position != text.length()) return -1L;
        return total;
    }

    private static String formatTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DATE_TIME_FORMAT);
    }

    private static String newId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public String title() {
        return "Reminders";
    }

    @Override
    public @Nullable String subtitle() {
        if (!SkyblockLocation.isInHypixel()) return "OFF";
        int customReminderCount = customReminders().size();
        return customReminderCount == 0 ? enabledReminderDisplay() : enabledReminderDisplay() + " + " + customReminderCount;
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_REMINDERS || !customReminders().isEmpty();
    }

    private enum ReminderType {
        DAILY_TASKS("daily", "Daily", "24 hours", 24L * 60L * 60L * 1000L,
                "Check daily SkyBlock tasks: commissions, faction quests, bits, and other reset-based chores.") {
            @Override
            boolean isEnabled() {
                return ConfigManager.FEATURES.REMIND_DAILY_TASKS;
            }

            @Override
            long lastReminder() {
                return ConfigManager.FEATURES.REMINDERS_LAST_DAILY_TASKS;
            }

            @Override
            void setLastReminder(long now) {
                ConfigManager.FEATURES.REMINDERS_LAST_DAILY_TASKS = now;
            }
        },
        EXPERIMENT_TABLE("experiments", "Experiments", "24 hours", 24L * 60L * 60L * 1000L,
                "Do your Experiment Table if it is available.") {
            @Override
            boolean isEnabled() {
                return ConfigManager.FEATURES.REMIND_EXPERIMENT_TABLE;
            }

            @Override
            long lastReminder() {
                return ConfigManager.FEATURES.REMINDERS_LAST_EXPERIMENT_TABLE;
            }

            @Override
            void setLastReminder(long now) {
                ConfigManager.FEATURES.REMINDERS_LAST_EXPERIMENT_TABLE = now;
            }
        },
        CAKES("cakes", "Cakes", "48 hours", 48L * 60L * 60L * 1000L,
                "Refresh Century Cakes if the buffs have fallen off.") {
            @Override
            boolean isEnabled() {
                return ConfigManager.FEATURES.REMIND_CAKES;
            }

            @Override
            long lastReminder() {
                return ConfigManager.FEATURES.REMINDERS_LAST_CAKES;
            }

            @Override
            void setLastReminder(long now) {
                ConfigManager.FEATURES.REMINDERS_LAST_CAKES = now;
            }
        },
        FORGE_AND_MINIONS("forge_minions", "Forge/Minions", "6 hours", 6L * 60L * 60L * 1000L,
                "Check Forge crafts, minions, fuel, compactors, and collection storage.") {
            @Override
            boolean isEnabled() {
                return ConfigManager.FEATURES.REMIND_FORGE_AND_MINIONS;
            }

            @Override
            long lastReminder() {
                return ConfigManager.FEATURES.REMINDERS_LAST_FORGE_AND_MINIONS;
            }

            @Override
            void setLastReminder(long now) {
                ConfigManager.FEATURES.REMINDERS_LAST_FORGE_AND_MINIONS = now;
            }
        };

        final String id;
        final String shortName;
        final String intervalName;
        final long intervalMs;
        final String message;
        private long promptedAt = 0L;

        ReminderType(String id, String shortName, String intervalName, long intervalMs, String message) {
            this.id = id;
            this.shortName = shortName;
            this.intervalName = intervalName;
            this.intervalMs = intervalMs;
            this.message = message;
        }

        boolean isDue(long now) {
            long completedAt = lastReminder();
            if (completedAt > 0L && now - completedAt < intervalMs) return false;
            return promptedAt <= 0L || now - promptedAt >= PRESET_REPEAT_INTERVAL_MS;
        }

        void markPrompted(long now) {
            promptedAt = now;
        }

        void markCompleted(long now) {
            promptedAt = now;
            setLastReminder(now);
        }

        abstract boolean isEnabled();

        abstract long lastReminder();

        abstract void setLastReminder(long now);

        static @Nullable ReminderType byId(String id) {
            for (ReminderType type : values()) {
                if (type.id.equals(id)) return type;
            }
            return null;
        }
    }
}
