package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.util.PlatformNotification;
import com.v999.sbutils.client.util.SkyblockLocation;
import net.minecraft.client.Minecraft;

public class EntranceNotifier implements SimpleChatEventHandler.NonOverlay {
    public static final EntranceNotifier INSTANCE = new EntranceNotifier();

    private static final String SPLIT_LINE = "-----------------------------";

    @Override
    public void onReceiveChat(String message) {
        if (ConfigManager.FEATURES.ENABLE_ENTRANCE_NOTIFIER
                && SkyblockLocation.isInHypixel()
                && !SkyblockLocation.LOCATION_STRING.isEmpty()
                && !Minecraft.getInstance().isWindowActive()
                && message.startsWith(SPLIT_LINE)
                && message.endsWith(SPLIT_LINE)
                && message.contains(" entered ")) {
            PlatformNotification.show("Game started!", message.substring(SPLIT_LINE.length(), message.length() - SPLIT_LINE.length()));
        }
    }
}
