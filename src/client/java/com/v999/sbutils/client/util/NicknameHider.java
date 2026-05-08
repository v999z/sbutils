package com.v999.sbutils.client.util;

import com.v999.sbutils.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class NicknameHider {
    public static final String DEFAULT_ALIAS = "SBUtils User";

    private NicknameHider() {
    }

    public static boolean isEnabled() {
        if (!ConfigManager.FEATURES.ENABLE_NICKNAME_HIDER) {
            return false;
        }
        String alias = getAlias();
        String username = getUsername();
        return !alias.isBlank() && !username.isBlank() && !alias.equalsIgnoreCase(username);
    }

    public static String getAlias() {
        String alias = ConfigManager.FEATURES.NICKNAME_HIDER_ALIAS;
        if (alias == null) {
            return DEFAULT_ALIAS;
        }
        alias = alias.trim();
        return alias.isBlank() ? DEFAULT_ALIAS : alias;
    }

    public static String getUsername() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return "";
        }
        String username = client.getUser() == null ? "" : client.getUser().getName();
        if ((username == null || username.isBlank()) && client.player != null) {
            username = client.player.getGameProfile().name();
        }
        return username == null ? "" : username;
    }

    public static boolean containsUsername(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        Pattern pattern = usernamePattern();
        return pattern != null && pattern.matcher(text).find();
    }

    public static String replace(String text) {
        if (text == null || text.isEmpty() || !isEnabled()) {
            return text;
        }
        Pattern pattern = usernamePattern();
        if (pattern == null) {
            return text;
        }
        return pattern.matcher(text).replaceAll(java.util.regex.Matcher.quoteReplacement(getAlias()));
    }

    public static Component replace(Component component) {
        if (component == null || !isEnabled()) {
            return component;
        }
        List<Component> flat = component.toFlatList(component.getStyle());
        MutableComponent rebuilt = Component.empty();
        boolean changed = false;
        for (Component part : flat) {
            String original = part.getString();
            String replaced = replace(original);
            changed |= !Objects.equals(original, replaced);
            rebuilt.append(Component.literal(replaced).withStyle(part.getStyle()));
        }
        return changed ? rebuilt : component;
    }

    public static FormattedText replace(FormattedText text) {
        if (text == null || !isEnabled()) {
            return text;
        }
        String original = text.getString();
        String replaced = replace(original);
        if (Objects.equals(original, replaced)) {
            return text;
        }
        return FormattedText.of(replaced);
    }

    public static FormattedCharSequence replace(FormattedCharSequence sequence) {
        if (sequence == null || !isEnabled()) {
            return sequence;
        }

        List<StyledSegment> segments = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        final Style[] activeStyle = {null};

        sequence.accept((index, style, codePoint) -> {
            if (activeStyle[0] == null) {
                activeStyle[0] = style;
            } else if (!Objects.equals(activeStyle[0], style)) {
                segments.add(new StyledSegment(builder.toString(), activeStyle[0]));
                builder.setLength(0);
                activeStyle[0] = style;
            }
            builder.appendCodePoint(codePoint);
            return true;
        });

        if (builder.length() > 0) {
            segments.add(new StyledSegment(builder.toString(), activeStyle[0] == null ? Style.EMPTY : activeStyle[0]));
        }

        MutableComponent rebuilt = Component.empty();
        boolean changed = false;
        for (StyledSegment segment : segments) {
            String replaced = replace(segment.text);
            changed |= !Objects.equals(segment.text, replaced);
            rebuilt.append(Component.literal(replaced).withStyle(segment.style));
        }
        return changed ? rebuilt.getVisualOrderText() : sequence;
    }

    public static String toPlainString(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }

    private static Pattern usernamePattern() {
        String username = getUsername();
        if (username.isBlank()) {
            return null;
        }
        return Pattern.compile("(?i)(?<![A-Za-z0-9_])" + Pattern.quote(username) + "(?![A-Za-z0-9_])");
    }

    private record StyledSegment(String text, Style style) {
    }
}
