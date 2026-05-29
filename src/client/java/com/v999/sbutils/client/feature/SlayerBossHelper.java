package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.ui.container.DynamicIslandAlertContainer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class SlayerBossHelper extends AbstractModule implements SimpleChatEventHandler.NonOverlay {
    public static final SlayerBossHelper INSTANCE = new SlayerBossHelper();

    private long activeUntil = 0L;
    private String status = "";

    private SlayerBossHelper() {
    }

    @Override
    public void onReceiveChat(String message) {
        if (!ConfigManager.FEATURES.ENABLE_SLAYER_BOSS_HELPER || message == null || message.isBlank()) {
            return;
        }

        String clean = stripFormatting(message).trim();
        String lower = clean.toLowerCase(Locale.ROOT);

        if (isQuestStarted(lower)) {
            show("Slayer Quest", "Quest started", DynamicIslandAlertContainer.SLAYER_COLOR);
            return;
        }
        if (isBossSpawned(lower)) {
            show("Slayer Boss", "Boss spawned", DynamicIslandAlertContainer.SLAYER_COLOR);
            return;
        }
        if (isMinibossSpawned(lower)) {
            show("Slayer Miniboss", shorten(clean, 58), DynamicIslandAlertContainer.SLAYER_COLOR);
            return;
        }
        if (isBossSlain(lower)) {
            show("Slayer Clear", "Boss slain", DynamicIslandAlertContainer.RARE_DROP_COLOR);
        }
    }

    private void show(String title, String detail, int color) {
        long now = Util.getMillis();
        status = detail;
        activeUntil = now + 5500L;
        DynamicIslandAlertContainer.INSTANCE.show(title, detail, color, activeUntil);
        SbutilsClient.island.show(DynamicIslandAlertContainer.INSTANCE);
        SbutilsClient.moduleList.showModule(this);
    }

    private static boolean isQuestStarted(String lower) {
        return lower.contains("slayer quest started") || lower.contains("slayer quest") && lower.contains("started");
    }

    private static boolean isBossSpawned(String lower) {
        return lower.contains("your slayer boss") && lower.contains("spawn");
    }

    private static boolean isMinibossSpawned(String lower) {
        return lower.contains("miniboss") && lower.contains("spawn");
    }

    private static boolean isBossSlain(String lower) {
        return lower.contains("slayer boss slain") || lower.contains("boss slain") && lower.contains("slayer");
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("Â§.", "").replaceAll("§.", "");
    }

    private static String shorten(String text, int maxLength) {
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 1)).trim() + "...";
    }

    @Override
    public String title() {
        return "Slayer";
    }

    @Override
    public @Nullable String subtitle() {
        return status.isBlank() ? null : status;
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_SLAYER_BOSS_HELPER && Util.getMillis() < activeUntil;
    }
}
