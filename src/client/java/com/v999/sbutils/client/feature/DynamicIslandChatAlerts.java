package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.ui.container.DynamicIslandAlertContainer;
import com.v999.sbutils.client.util.NicknameHider;
import net.minecraft.util.Util;

public class DynamicIslandChatAlerts implements SimpleChatEventHandler.NonOverlay {
    public static final DynamicIslandChatAlerts INSTANCE = new DynamicIslandChatAlerts();

    private static final long COOLDOWN_MS = 900L;
    private long lastAlertTimestamp = 0L;

    private DynamicIslandChatAlerts() {
    }

    @Override
    public void onReceiveChat(String message) {
        if (!ConfigManager.FEATURES.ENABLE_DYNAMIC_ISLAND_CHAT_ALERTS || message == null || message.isBlank()) {
            return;
        }

        String clean = stripFormatting(message).trim();
        String lower = clean.toLowerCase(java.util.Locale.ROOT);

        // RNG meter drops have their own richer RNGDropContainer and sound, so do not duplicate them here.
        if (message.startsWith("§d§lRNG METER!") || lower.startsWith("rng meter!")) {
            return;
        }

        if (isRareDrop(lower)) {
            show("✦ Rare Drop", extractRareDropDetail(clean), DynamicIslandAlertContainer.RARE_DROP_COLOR);
            RNGDrop.playPreview();
            return;
        }

        if (isAuctionSold(lower)) {
            show("Auction Sold", extractAuctionDetail(clean), DynamicIslandAlertContainer.AUCTION_COLOR);
            return;
        }

        if (isIncomingPrivateMessage(lower)) {
            show("Private Message", extractPrivateMessageDetail(clean), DynamicIslandAlertContainer.PRIVATE_MESSAGE_COLOR);
        }
    }

    public void showUsernameMention(String message) {
        if (!ConfigManager.FEATURES.ENABLE_DYNAMIC_ISLAND_CHAT_ALERTS || message == null || message.isBlank()) {
            return;
        }
        show("Username Mention", shorten(stripFormatting(message), 64), DynamicIslandAlertContainer.MENTION_COLOR);
    }

    private void show(String title, String detail, int accentColor) {
        long now = Util.getMillis();
        if (now - lastAlertTimestamp < COOLDOWN_MS) {
            return;
        }
        lastAlertTimestamp = now;
        DynamicIslandAlertContainer.INSTANCE.show(title, detail, accentColor, now + 5500L);
        SbutilsClient.island.show(DynamicIslandAlertContainer.INSTANCE);
    }

    private static boolean isRareDrop(String lower) {
        // Only react to Hypixel system-style messages. Player chat that copies the text
        // usually looks like "Player: RARE DROP! ..." or "Party > Player: RARE DROP! ...",
        // so requiring the alert text at the start avoids false popups.
        return lower.startsWith("rare drop!")
                || lower.startsWith("crazy rare drop!")
                || lower.startsWith("insane drop!");
    }

    private static boolean isIncomingPrivateMessage(String lower) {
        // Hypixel incoming private messages are system-style and normally start with
        // "From <player>: <message>". Requiring it at the start avoids matching
        // copied PM text inside normal player/party/guild chat.
        return lower.startsWith("from ") && lower.indexOf(':') > 5;
    }

    private static boolean isAuctionSold(String lower) {
        // Auction messages from Hypixel are system-style, normally prefixed with [Auction]
        // or phrased as your own auction/claim message. Do not match player chat copies.
        boolean hypixelAuctionMessage = lower.startsWith("[auction]")
                || lower.startsWith("your auction")
                || lower.startsWith("you claimed")
                || lower.startsWith("you collected")
                || lower.startsWith("claim your coins");

        if (!hypixelAuctionMessage || !lower.contains("auction")) {
            return false;
        }

        return lower.contains("sold")
                || lower.contains(" bought ")
                || lower.contains(" purchased ")
                || lower.contains("ended with")
                || lower.contains("claim your coins")
                || lower.contains("claimed")
                || lower.contains("collected");
    }

    private static String extractRareDropDetail(String clean) {
        int bang = clean.indexOf('!');
        if (bang >= 0 && bang + 1 < clean.length()) {
            return shorten(clean.substring(bang + 1).trim(), 58);
        }
        return shorten(clean, 58);
    }

    private static String extractAuctionDetail(String clean) {
        String detail = clean;
        if (NicknameHider.containsUsername(detail)) {
            detail = NicknameHider.replace(detail);
        }
        return shorten(detail, 64);
    }

    private static String extractPrivateMessageDetail(String clean) {
        String detail = clean;
        if (NicknameHider.containsUsername(detail)) {
            detail = NicknameHider.replace(detail);
        }
        return shorten(detail, 64);
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("§.", "");
    }

    private static String shorten(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "Check chat for details";
        }
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }
}
