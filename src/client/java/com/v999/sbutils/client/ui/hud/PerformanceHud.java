package com.v999.sbutils.client.ui.hud;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.mixin.AccessFont;
import com.v999.sbutils.client.ui.Easy2D;
import com.v999.sbutils.client.ui.clickgui.ClickGuiTheme;
import com.v999.sbutils.client.ui.container.ServerTPSContainer;
import com.v999.sbutils.client.ui.font.FontManager;
import com.v999.sbutils.client.ui.font.RenderInfo;
import com.v999.sbutils.client.ui.font.RenderedText;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PerformanceHud implements HudElement {
    public static final PerformanceHud INSTANCE = new PerformanceHud();
    private static final float HORIZONTAL_PADDING = 6F;
    private static final float VERTICAL_PADDING = 4F;
    private static final float GRAPH_TOP_GAP = 4F;
    private static final float GRAPH_MIN_WIDTH = 96F;
    public static final float GRAPH_HEIGHT = 26F;
    private static final int GRAPH_SAMPLES = 48;
    private static final long GRAPH_SAMPLE_INTERVAL_MS = 500L;

    private static int lastKnownPing = -1;

    private final float[] graphValues = new float[GRAPH_SAMPLES];
    private int graphWriteIndex = 0;
    private int graphSampleCount = 0;
    private long lastGraphSampleMillis = 0L;

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, @NonNull DeltaTracker deltaTracker) {
        if (!isActive()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        updateGraphSamples(client);

        Easy2D.configure(context);
        float scale = (float) client.getWindow().getGuiScale();
        String text = buildDisplayText(client);
        RenderedText renderedText = FontManager.requestRenderedText(new RenderInfo(FontManager.DEFAULT_FONT, text, 8F), scale);
        boolean graphEnabled = isGraphEnabled();
        float textWidth = renderedText.bounds.width / scale;
        float contentWidth = graphEnabled ? Math.max(textWidth, GRAPH_MIN_WIDTH) : textWidth;
        float width = contentWidth + HORIZONTAL_PADDING * 2F;
        float height = renderedText.lineHeight / scale + VERTICAL_PADDING * 2F + (graphEnabled ? GRAPH_TOP_GAP + GRAPH_HEIGHT : 0F);
        float x = clampX(client, ConfigManager.FEATURES.PERFORMANCE_HUD_X, width);
        float y = clampY(client, ConfigManager.FEATURES.PERFORMANCE_HUD_Y, height);

        int background = backgroundColor();
        Easy2D.drawRoundRect(x, y, x + width, y + height, 8F, 12F, background, background);
        renderedText.draw(context, x + HORIZONTAL_PADDING, y + VERTICAL_PADDING, scale, Easy2D.TEXT_DEFAULT_COLOR);
        if (graphEnabled) {
            drawGraph(context, x + HORIZONTAL_PADDING, y + VERTICAL_PADDING + renderedText.lineHeight / scale + GRAPH_TOP_GAP, contentWidth, GRAPH_HEIGHT);
        }
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
        String normalized = key.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "black", "dark_gray", "transparent" -> normalized;
            default -> "legacy";
        };
    }

    public static void cycleGraphMetric() {
        ConfigManager.FEATURES.PERFORMANCE_HUD_GRAPH_METRIC = switch (normalizeGraphMetric(ConfigManager.FEATURES.PERFORMANCE_HUD_GRAPH_METRIC)) {
            case "fps" -> "ping";
            case "ping" -> "tps";
            default -> "fps";
        };
        INSTANCE.resetGraphHistory();
        ConfigManager.FEATURES.markAsChanged();
    }

    private void resetGraphHistory() {
        graphWriteIndex = 0;
        graphSampleCount = 0;
        lastGraphSampleMillis = 0L;
    }

    public static String graphMetricDisplayName() {
        return switch (normalizeGraphMetric(ConfigManager.FEATURES.PERFORMANCE_HUD_GRAPH_METRIC)) {
            case "ping" -> "Ping";
            case "tps" -> "TPS";
            default -> "FPS";
        };
    }

    public static boolean isGraphEnabled() {
        return ConfigManager.FEATURES.PERFORMANCE_HUD_GRAPH_ENABLED;
    }

    private static String normalizeGraphMetric(String metric) {
        if (metric == null || metric.isBlank()) {
            return "fps";
        }
        String normalized = metric.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "fps", "ping", "tps" -> normalized;
            default -> "fps";
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
        float textWidth = ((AccessFont) client.font).getSplitter().stringWidth(text);
        float contentWidth = isGraphEnabled() ? Math.max(textWidth, GRAPH_MIN_WIDTH) : textWidth;
        return HORIZONTAL_PADDING * 2F + contentWidth;
    }

    public float estimateHeight(Minecraft client) {
        return VERTICAL_PADDING * 2F + client.font.lineHeight + (isGraphEnabled() ? GRAPH_TOP_GAP + GRAPH_HEIGHT : 0F);
    }

    public static float clampX(Minecraft client, float desiredX, float width) {
        return Mth.clamp(desiredX, 2F, client.getWindow().getGuiScaledWidth() - width - 2F);
    }

    public static float clampY(Minecraft client, float desiredY, float height) {
        return Mth.clamp(desiredY, 2F, client.getWindow().getGuiScaledHeight() - height - 2F);
    }

    public void drawGraph(GuiGraphicsExtractor context, float left, float top, float width, float height) {
        Easy2D.drawRoundRect(left, top, left + width, top + height, 4F, 0F, 0x26000000, 0x00000000);

        int gridColor = 0x22FFFFFF;
        Easy2D.drawRoundRect(left + 2F, top + height * 0.33F, left + width - 2F, top + height * 0.33F + 0.6F, 0.3F, 0F, gridColor, 0x00000000);
        Easy2D.drawRoundRect(left + 2F, top + height * 0.66F, left + width - 2F, top + height * 0.66F + 0.6F, 0.3F, 0F, gridColor, 0x00000000);

        if (graphSampleCount <= 0) {
            return;
        }

        float maxValue = graphScaleMax();
        float barSlotWidth = width / GRAPH_SAMPLES;
        float barWidth = Math.max(1F, barSlotWidth - 0.35F);
        int barColor = ClickGuiTheme.withAlpha(ClickGuiTheme.accentColor(), 0xD8);
        int oldestIndex = graphSampleCount < GRAPH_SAMPLES ? 0 : graphWriteIndex;
        int leadingEmptySlots = GRAPH_SAMPLES - graphSampleCount;

        for (int i = 0; i < graphSampleCount; i++) {
            int historyIndex = (oldestIndex + i) % GRAPH_SAMPLES;
            float value = graphValues[historyIndex];
            float normalized = Mth.clamp(value / maxValue, 0F, 1F);
            float barHeight = Math.max(1F, normalized * (height - 4F));
            float x = left + (leadingEmptySlots + i) * barSlotWidth;
            float y = top + height - 2F - barHeight;
            Easy2D.drawRoundRect(x, y, Math.min(x + barWidth, left + width), top + height - 2F,
                    0.75F, 0F, barColor, 0x00000000);
        }
    }

    private void updateGraphSamples(Minecraft client) {
        if (!isGraphEnabled()) {
            return;
        }
        long now = Util.getMillis();
        if (graphSampleCount > 0 && now - lastGraphSampleMillis < GRAPH_SAMPLE_INTERVAL_MS) {
            return;
        }
        lastGraphSampleMillis = now;
        graphValues[graphWriteIndex] = currentGraphValue(client);
        graphWriteIndex = (graphWriteIndex + 1) % GRAPH_SAMPLES;
        graphSampleCount = Math.min(GRAPH_SAMPLES, graphSampleCount + 1);
    }

    private float currentGraphValue(Minecraft client) {
        return switch (normalizeGraphMetric(ConfigManager.FEATURES.PERFORMANCE_HUD_GRAPH_METRIC)) {
            case "ping" -> Math.max(0F, resolvePing(client));
            case "tps" -> Math.max(0F, ServerTPSContainer.INSTANCE.getTps());
            default -> Math.max(0F, client.getFps());
        };
    }

    private float graphScaleMax() {
        String metric = normalizeGraphMetric(ConfigManager.FEATURES.PERFORMANCE_HUD_GRAPH_METRIC);
        if ("tps".equals(metric)) {
            return 20F;
        }
        float max = "ping".equals(metric) ? 200F : 120F;
        for (int i = 0; i < graphSampleCount; i++) {
            max = Math.max(max, graphValues[i] * 1.15F);
        }
        return Math.max(1F, max);
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
        return String.format(Locale.ROOT, "%.2f", tps);
    }
}
