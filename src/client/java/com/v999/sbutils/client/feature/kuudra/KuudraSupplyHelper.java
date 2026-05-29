package com.v999.sbutils.client.feature.kuudra;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.feature.AbstractModule;
import com.v999.sbutils.client.ui.container.DynamicIslandAlertContainer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class KuudraSupplyHelper extends AbstractModule implements SimpleChatEventHandler.NonOverlay {
    public static final KuudraSupplyHelper INSTANCE = new KuudraSupplyHelper();

    private long activeUntil = 0L;
    private String status = "";

    private KuudraSupplyHelper() {
    }

    @Override
    public void onReceiveChat(String message) {
        if (!ConfigManager.FEATURES.ENABLE_KUUDRA_SUPPLY_HELPER || message == null || message.isBlank()) {
            return;
        }

        String clean = stripFormatting(message).trim();
        String lower = clean.toLowerCase(Locale.ROOT);

        if (isSupplyPickup(lower)) {
            show("Kuudra Supply", pickupDetail(clean), DynamicIslandAlertContainer.KUUDRA_COLOR);
            return;
        }
        if (isSupplyPlaced(lower)) {
            show("Kuudra Supply", "Supply delivered", DynamicIslandAlertContainer.KUUDRA_COLOR);
            return;
        }
        if (isFuelCell(lower)) {
            show("Kuudra Fuel Cell", pickupDetail(clean), DynamicIslandAlertContainer.KUUDRA_COLOR);
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

    private static boolean isSupplyPickup(String lower) {
        return lower.contains("elle's supplies")
                || lower.contains("picked up supplies")
                || lower.contains("picked up the supplies");
    }

    private static boolean isSupplyPlaced(String lower) {
        return lower.contains("placed") && lower.contains("supplies");
    }

    private static boolean isFuelCell(String lower) {
        return lower.contains("ballista fuel cell") || lower.contains("fuel cell");
    }

    private static String pickupDetail(String clean) {
        return shorten(clean.replaceAll("\\s+", " ").trim(), 58);
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("Â§.", "").replaceAll("§.", "");
    }

    private static String shorten(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 1)).trim() + "...";
    }

    @Override
    public String title() {
        return "KuudraSupply";
    }

    @Override
    public @Nullable String subtitle() {
        return status.isBlank() ? null : status;
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_KUUDRA_SUPPLY_HELPER && Util.getMillis() < activeUntil;
    }
}
