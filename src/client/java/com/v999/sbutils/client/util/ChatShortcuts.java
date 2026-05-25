package com.v999.sbutils.client.util;

import java.util.Locale;

public final class ChatShortcuts {
    private ChatShortcuts() {
    }

    public static String expandCommand(String command) {
        if (command == null || command.isBlank()) {
            return command;
        }

        String trimmed = command.stripLeading();
        int split = trimmed.indexOf(' ');
        String head = split < 0 ? trimmed : trimmed.substring(0, split);
        String tail = split < 0 ? "" : trimmed.substring(split);

        return switch (head.toLowerCase(Locale.ROOT)) {
            case "ca" -> "chat all";
            case "cp" -> "chat party";
            case "cg" -> "chat guild";
            case "co" -> "chat officer";
            case "cc" -> "chat coop";
            case "m" -> "msg" + tail;
            case "v" -> "visit" + tail;
            default -> command;
        };
    }
}
