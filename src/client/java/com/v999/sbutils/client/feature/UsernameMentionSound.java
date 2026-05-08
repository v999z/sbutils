package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.util.MusicInstance;
import com.v999.sbutils.client.util.NicknameHider;
import com.v999.sbutils.client.util.SoundAssetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

public class UsernameMentionSound implements SimpleChatEventHandler.NonOverlay {
    public static final UsernameMentionSound INSTANCE = new UsernameMentionSound();
    private static final long COOLDOWN_MS = 1200L;

    private long lastMentionTimestamp = 0L;

    private UsernameMentionSound() {
    }

    @Override
    public void onReceiveChat(String message) {
        if (!ConfigManager.FEATURES.ENABLE_USERNAME_MENTION_SOUND || message == null || message.isBlank()) {
            return;
        }

        if (!NicknameHider.containsUsername(message)) {
            return;
        }

        long now = Util.getMillis();
        if (now - lastMentionTimestamp < COOLDOWN_MS) {
            return;
        }

        lastMentionTimestamp = now;
        DynamicIslandChatAlerts.INSTANCE.showUsernameMention(message);
        playSound("music_of_username_mention", now + 9000L);
    }

    public static void playPreview() {
        playSound("music_of_username_mention", Util.getMillis() + 9000L);
    }

    private static void playSound(String resourceId, long stopAt) {
        SoundAssetManager.ensureDefaultSounds();
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getSoundManager() == null) {
            return;
        }
        client.getSoundManager().play(new MusicInstance(
                resourceId,
                "config/Sbutils/user_music.ogg",
                false,
                stopAt
        ));
    }
}