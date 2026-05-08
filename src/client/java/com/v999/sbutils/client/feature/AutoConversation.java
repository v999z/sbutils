package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class AutoConversation {
    public static final AutoConversation INSTANCE = new AutoConversation();
    private static final long COOLDOWN_MS = 400L;
    private static final List<String> CONTINUE_HINTS = List.of(
            "continue", "next", "click to continue", "[next]"
    );

    private long lastTriggerAt = 0L;
    private String lastTriggeredCommand = "";

    private AutoConversation() {
    }

    public void onReceiveChat(Component message) {
        if (!ConfigManager.FEATURES.ENABLE_AUTO_CONVERSATION || message == null) {
            return;
        }

        LinkedHashSet<String> commands = new LinkedHashSet<>();
        boolean continueLike = false;

        for (Component part : message.toFlatList(message.getStyle())) {
            String text = part.getString();
            if (text != null) {
                String lowered = text.toLowerCase(Locale.ROOT);
                for (String hint : CONTINUE_HINTS) {
                    if (lowered.contains(hint)) {
                        continueLike = true;
                        break;
                    }
                }
            }

            ClickEvent clickEvent = part.getStyle().getClickEvent();
            if (clickEvent == null) {
                continue;
            }

            String command = null;
            if (clickEvent instanceof ClickEvent.RunCommand runCommand) {
                command = runCommand.command();
            } else if (clickEvent instanceof ClickEvent.SuggestCommand suggestCommand) {
                command = suggestCommand.command();
            }

            if (command == null || command.isBlank()) {
                continue;
            }

            commands.add(command);
            if (commands.size() > 1) {
                return;
            }
        }

        if (commands.size() != 1) {
            return;
        }

        String command = commands.iterator().next();
        if (!continueLike && !looksLikeConversationCommand(command)) {
            return;
        }

        long now = Util.getMillis();
        if (command.equals(lastTriggeredCommand) && now - lastTriggerAt < COOLDOWN_MS) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getConnection() == null) {
            return;
        }

        lastTriggerAt = now;
        lastTriggeredCommand = command;

        if (command.startsWith("/")) {
            client.getConnection().sendCommand(command.substring(1));
        } else {
            client.getConnection().sendChat(command);
        }
    }

    private boolean looksLikeConversationCommand(String command) {
        String lowered = command.toLowerCase(Locale.ROOT);
        return lowered.contains("continue")
                || lowered.contains("dialog")
                || lowered.contains("conversation")
                || lowered.contains("talk")
                || lowered.contains("selectnpc")
                || lowered.contains("npc");
    }
}
