package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.ui.container.RNGDropContainer;
import com.v999.sbutils.client.util.MusicInstance;
import com.v999.sbutils.client.util.SoundAssetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

public class RNGDrop implements SimpleChatEventHandler.NonOverlay {
    public static final RNGDrop INSTANCE = new RNGDrop();

    private static final String RNG_MESSAGE_HEAD = "§d§lRNG METER! §r§aReselected the ";
    private static final String RNG_ITEM_SOURCE_SPLITTER = " §afor ";

    public long lastDropTimestamp = 0;
    public String itemName = "";
    public String sourceName;

    @Override
    public void onReceiveChat(String message) {
        if (ConfigManager.FEATURES.ENABLE_RNG_DROP_SUMMARY && message.startsWith(RNG_MESSAGE_HEAD)) {
            int endIndex = message.indexOf('!', RNG_MESSAGE_HEAD.length());
            if (endIndex < 0) endIndex = message.length();

            String itemAndSource = message.substring(RNG_MESSAGE_HEAD.length(), endIndex);

            int splitIndex = itemAndSource.lastIndexOf(RNG_ITEM_SOURCE_SPLITTER);
            if (splitIndex < 0) {
                itemName = itemAndSource;
                sourceName = null;
            } else {
                itemName = itemAndSource.substring(0, splitIndex);
                sourceName = itemAndSource.substring(splitIndex + RNG_ITEM_SOURCE_SPLITTER.length());
            }

            lastDropTimestamp = Util.getMillis();
            RNGDropContainer.INSTANCE.resetAnimation();
            SbutilsClient.island.show(RNGDropContainer.INSTANCE);
            playSound("music_of_rng_drop", lastDropTimestamp + 9000L);
        }
    }

    public static void playPreview() {
        playSound("music_of_rng_drop", Util.getMillis() + 9000L);
    }

    private static void playSound(String resourceId, long stopAt) {
        SoundAssetManager.ensureDefaultSounds();
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getSoundManager() == null) {
            return;
        }
        client.getSoundManager().play(new MusicInstance(
                resourceId,
                "config/Sbutils/rng_music.ogg",
                false,
                stopAt
        ));
    }
}