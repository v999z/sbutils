package com.v999.sbutils.client.ui.hud;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.mixin.AccessFont;
import com.v999.sbutils.client.ui.Easy2D;
import com.v999.sbutils.client.ui.font.FontManager;
import com.v999.sbutils.client.ui.font.RenderInfo;
import com.v999.sbutils.client.ui.font.RenderedText;
import com.v999.sbutils.client.ui.island.HudDynamicIsland;
import com.v999.sbutils.client.ui.container.ServerTPSContainer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PerformanceHud implements HudElement {
    public static final PerformanceHud INSTANCE = new PerformanceHud();
    private static final float HORIZONTAL_PADDING = 6F;
    private static final float VERTICAL_PADDING = 4F;
    private static int lastKnownPing = -1;

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, @NonNull DeltaTracker deltaTracker) {
        if (!isActive()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        Easy2D.configure(context);
        float scale = (float) client.getWindow().getGuiScale();
        String text = buildDisplayText(client);
        RenderedText renderedText = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, text, 8F), scale);
        float width = renderedText.bounds.width / scale + HORIZONTAL_PADDING * 2F;
        float height = renderedText.lineHeight / scale + VERTICAL_PADDING * 2F;
        float x = clampX(client, ConfigManager.FEATURES.PERFORMANCE_HUD_X, width);
        float y = clampY(client, ConfigManager.FEATURES.PERFORMANCE_HUD_Y, height);

        int background = backgroundColor();
        Easy2D.drawRoundRect(x, y, x + width, y + height, 8F, 12F, background, background);
        renderedText.draw(context, x + HORIZONTAL_PADDING, y + VERTICAL_PADDING, scale, Easy2D.TEXT_DEFAULT_COLOR);
        Easy2D.cleanup();
    }

    public static int backgroundColor() {
        return switch (normalizeBackgroundColorKey(ConfigManager.FEATURES.PERFORMANCE_HUD_BACKGROUND_COLOR)) {
            case "black" -> 0xDB000000;
            case "dark_gray" -> 0xDB2B2F36;
            case "transparent" -> 0x66000000;
            default -> ConfigManager.FEATURES.PERFORMANCE_HUD_TRANSPARENT_BACKGROUND ? 0x66000000 : 0xDB000000;
        };
    }

    public static void cycleBackgroundColor() {
        String current = normalizeBackgroundColorKey(ConfigManager.FEATURES.PERFORMANCE_HUD_BACKGROUND_COLOR);
        ConfigManager.FEATURES.PERFORMANCE_HUD_BACKGROUND_COLOR = switch (current) {
            case "black" -> "dark_gray";
            case "dark_gray" -> "transparent";
            case "transparent" -> "black";
            default -> ConfigManager.FEATURES.PERFORMANCE_HUD_TRANSPARENT_BACKGROUND ? "black" : "dark_gray";
        };
        ConfigManager.FEATURES.PERFORMANCE_HUD_TRANSPARENT_BACKGROUND = "transparent".equals(ConfigManager.FEATURES.PERFORMANCE_HUD_BACKGROUND_COLOR);
        ConfigManager.FEATURES.markAsChanged();
    }

    public static String backgroundColorDisplayName() {
        return switch (normalizeBackgroundColorKey(ConfigManager.FEATURES.PERFORMANCE_HUD_BACKGROUND_COLOR)) {
            case "black" -> "Black";
            case "dark_gray" -> "Dark gray";
            case "transparent" -> "Transparent black";
            default -> ConfigManager.FEATURES.PERFORMANCE_HUD_TRANSPARENT_BACKGROUND ? "Transparent black" : "Black";
        };
    }

    private static String normalizeBackgroundColorKey(String key) {
        if (key == null || key.isBlank()) {
            return "legacy";
        }
        String normalized = key.trim().toLowerCase(java.util.Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "black", "dark_gray", "transparent" -> normalized;
            default -> "legacy";
        };
    }

    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_PERFORMANCE_HUD &&
                (ConfigManager.FEATURES.SHOW_PERFORMANCE_FPS ||
                        ConfigManager.FEATURES.SHOW_PERFORMANCE_PING ||
                        ConfigManager.FEATURES.SHOW_PERFORMANCE_TPS);
    }

    public String buildDisplayText(Minecraft client) {
        List<String> parts = new ArrayList<>(3);
        if (ConfigManager.FEATURES.SHOW_PERFORMANCE_FPS) {
            parts.add("FPS " + client.getFps());
        }
        if (ConfigManager.FEATURES.SHOW_PERFORMANCE_PING) {
            parts.add("Ping " + getPing(client));
        }
        if (ConfigManager.FEATURES.SHOW_PERFORMANCE_TPS) {
            parts.add("TPS " + getTps());
        }
        if (parts.isEmpty()) {
            return "Performance";
        }
        return String.join(" • ", parts);
    }

    public float estimateWidth(Minecraft client) {
        String text = buildDisplayText(client);
        return HORIZONTAL_PADDING * 2F + ((AccessFont) client.font).getSplitter().stringWidth(text);
    }

    public float estimateHeight(Minecraft client) {
        return VERTICAL_PADDING * 2F + client.font.lineHeight;
    }

    public static float clampX(Minecraft client, float desiredX, float width) {
        return Mth.clamp(desiredX, 2F, client.getWindow().getGuiScaledWidth() - width - 2F);
    }

    public static float clampY(Minecraft client, float desiredY, float height) {
        return Mth.clamp(desiredY, 2F, client.getWindow().getGuiScaledHeight() - height - 2F);
    }

    private static String getPing(Minecraft client) {
        int ping = resolvePing(client);
        return ping >= 0 ? Integer.toString(ping) : "--";
    }

    private static int resolvePing(Minecraft client) {
        if (client.player != null) {
            ClientPacketListener connection = client.getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(client.player.getUUID());
                if (info != null) {
                    int latency = info.getLatency();
                    if (latency > 1) {
                        lastKnownPing = latency;
                        return latency;
                    }
                }
                ServerData serverData = connection.getServerData();
                if (serverData != null && serverData.ping > 1L && serverData.ping <= Integer.MAX_VALUE) {
                    int fallback = (int) serverData.ping;
                    lastKnownPing = fallback;
                    return fallback;
                }
            }
        }
        return lastKnownPing;
    }

    private static String getTps() {
        float tps = ServerTPSContainer.INSTANCE.getTps();
        if (tps <= 0F) {
            return "--";
        }
        return String.format(java.util.Locale.ROOT, "%.2f", tps);
    }
}
