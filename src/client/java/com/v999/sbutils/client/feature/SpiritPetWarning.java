package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.events.SimpleChatEventHandler;
import com.v999.sbutils.client.ui.container.SpiritPetWarningContainer;
import net.minecraft.util.Util;

public class SpiritPetWarning implements SimpleChatEventHandler.NonOverlay {
    public static final SpiritPetWarning INSTANCE = new SpiritPetWarning();

    @Override
    public void onReceiveChat(String text) {
        if (text.startsWith("Your Spirit Pet hit ")) {
            SpiritPetWarningContainer.instance.lastTriggeredTimestamp = Util.getMillis();
            SbutilsClient.island.show(SpiritPetWarningContainer.instance);
        }
    }
}
