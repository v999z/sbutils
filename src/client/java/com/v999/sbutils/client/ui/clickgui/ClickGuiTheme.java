package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.util.ChromaColor;
import net.minecraft.util.ARGB;

public final class ClickGuiTheme {
    private static final String[] ACCENT_KEYS = {"blue", "purple", "pink", "green", "orange", "red", "gold", "aqua", "rainbow"};
    private static final String[] COMPACT_COUNTER_KEYS = {"accent", "blue", "purple", "pink", "green", "orange", "red", "gold", "aqua", "rainbow"};

    private ClickGuiTheme() {
    }

    public static boolean isLightMode() {
        return ConfigManager.GENERAL.CLICK_GUI_LIGHT_MODE;
    }

    public static void toggleMode() {
        ConfigManager.GENERAL.CLICK_GUI_LIGHT_MODE = !ConfigManager.GENERAL.CLICK_GUI_LIGHT_MODE;
        ConfigManager.GENERAL.markAsChanged();
    }

    public static String themeDisplayName() {
        return isLightMode() ? "Light" : "Dark";
    }

    public static int accentColor() {
        return resolvePaletteColor(ConfigManager.GENERAL.CLICK_GUI_ACCENT_COLOR);
    }

    public static void cycleAccent() {
        ConfigManager.GENERAL.CLICK_GUI_ACCENT_COLOR = nextKey(ConfigManager.GENERAL.CLICK_GUI_ACCENT_COLOR, ACCENT_KEYS);
        ConfigManager.GENERAL.markAsChanged();
    }

    public static String accentDisplayName() {
        return displayNameForPaletteColor(ConfigManager.GENERAL.CLICK_GUI_ACCENT_COLOR);
    }

    public static int compactCounterColor() {
        String key = ConfigManager.FEATURES.COMPACT_DUPLICATE_MESSAGES_COLOR;
        return "accent".equalsIgnoreCase(key) ? accentColor() : resolvePaletteColor(key);
    }

    public static void cycleCompactCounterColor() {
        ConfigManager.FEATURES.COMPACT_DUPLICATE_MESSAGES_COLOR = nextKey(ConfigManager.FEATURES.COMPACT_DUPLICATE_MESSAGES_COLOR, COMPACT_COUNTER_KEYS);
        ConfigManager.FEATURES.markAsChanged();
    }

    public static String compactCounterColorDisplayName() {
        String key = ConfigManager.FEATURES.COMPACT_DUPLICATE_MESSAGES_COLOR;
        if ("accent".equalsIgnoreCase(key)) {
            return "Accent (" + accentDisplayName() + ')';
        }
        return displayNameForPaletteColor(key);
    }

    public static int backgroundColor() {
        return isLightMode() ? 0xFFF6F8FC : 0xFF15181D;
    }

    public static int backgroundShadowColor() {
        return isLightMode() ? 0x24000000 : 0x8A000000;
    }

    public static int titleTextColor() {
        return isLightMode() ? 0xFF111827 : 0xFFFFFFFF;
    }

    public static int textPrimaryColor() {
        return isLightMode() ? 0xFF101828 : 0xFFFFFFFF;
    }

    public static int textSecondaryColor() {
        return isLightMode() ? 0xFF667085 : 0xFFB8C0CC;
    }

    public static int selectedNavigationTextColor() {
        return 0xFFFFFFFF;
    }

    public static int withAlpha(int color, int alpha) {
        return ARGB.color(alpha, ARGB.red(color), ARGB.green(color), ARGB.blue(color));
    }

    private static String nextKey(String current, String[] keys) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equalsIgnoreCase(current)) {
                return keys[(i + 1) % keys.length];
            }
        }
        return keys[0];
    }

    private static String displayNameForPaletteColor(String key) {
        return switch (normalizeKey(key)) {
            case "purple" -> "Purple";
            case "pink" -> "Pink";
            case "green" -> "Green";
            case "orange" -> "Orange";
            case "red" -> "Red";
            case "gold" -> "Gold";
            case "aqua" -> "Aqua";
            case "rainbow" -> "Rainbow";
            default -> "Blue";
        };
    }

    private static int resolvePaletteColor(String key) {
        return switch (normalizeKey(key)) {
            case "purple" -> 0xFF7C4DFF;
            case "pink" -> 0xFFFF4FA3;
            case "green" -> 0xFF10B981;
            case "orange" -> 0xFFFF8A3D;
            case "red" -> 0xFFF04438;
            case "gold" -> 0xFFF5B700;
            case "aqua" -> 0xFF22C7F0;
            case "rainbow" -> ChromaColor.pure(3L, 0L, 0xFF);
            default -> 0xFF2F6DFF;
        };
    }

    private static String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            return "blue";
        }
        return key.trim().toLowerCase();
    }
}
