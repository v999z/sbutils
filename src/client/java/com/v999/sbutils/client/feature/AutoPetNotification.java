package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.ui.container.AutoPetNotificationContainer;
import net.minecraft.util.Util;

public class AutoPetNotification implements SimpleChatEventHandler.NonOverlay {
    public static final AutoPetNotification INSTANCE = new AutoPetNotification();

    private static final String AUTO_PET_PREFIX = "§cAutopet §eequipped your ";

    @Override
    public void onReceiveChat(String text) {
        if (text.startsWith(AUTO_PET_PREFIX)) {
            text = text.substring(AUTO_PET_PREFIX.length(), text.indexOf('!', AUTO_PET_PREFIX.length()) - 2);
            AutoPetNotificationContainer.instance.warningText = "♣ Autopet equipped " + text;
            AutoPetNotificationContainer.instance.lastTriggeredTimestamp = Util.getMillis();
            SbutilsClient.island.show(AutoPetNotificationContainer.instance);
        }
    }
}
