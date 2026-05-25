package com.v999.sbutils.client.feature;

import com.mojang.blaze3d.platform.InputConstants;
import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.mixin.AccessKeyMapping;
import com.v999.sbutils.client.util.SkyblockItem;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

public class EtherwarpHelper extends AbstractModule implements ClientTickEvents.StartTick, ClientTickEvents.EndTick {
    public static final EtherwarpHelper INSTANCE = new EtherwarpHelper();
    private static final ObjectOpenHashSet<String> ETHERWARP_ITEM_IDS = new ObjectOpenHashSet<>(new String[]{
            "ASPECT_OF_THE_VOID",
            "ASPECT_OF_THE_END"
    });

    private Stage stage = Stage.IDLE;
    private boolean forcedShift = false;
    private boolean suppressedAttack = false;

    @Override
    public void onStartTick(Minecraft client) {
        if (client.player == null || client.screen != null) {
            resetKeys(client);
            return;
        }

        boolean shouldHandle = shouldHandle(client);
        if (stage != Stage.IDLE && !shouldHandle) {
            resetKeys(client);
            return;
        }

        boolean consumedAttack = false;
        if (shouldHandle || stage != Stage.IDLE) {
            while (client.options.keyAttack.consumeClick()) {
                consumedAttack = true;
            }
        }

        if (stage == Stage.IDLE) {
            if (!consumedAttack || !shouldHandle) {
                return;
            }

            suppressAttack(client);
            if (!client.player.isShiftKeyDown()) {
                client.options.keyShift.setDown(true);
                forcedShift = true;
            }
            stage = Stage.READY_TO_RIGHT_CLICK;
            moduleList.needResort = true;
            SbutilsClient.moduleList.showModule(this);
            return;
        }

        if (consumedAttack) {
            suppressAttack(client);
        }

        if (stage == Stage.READY_TO_RIGHT_CLICK) {
            KeyMapping.click(((AccessKeyMapping) client.options.keyUse).getKey());
            stage = Stage.RELEASE_SHIFT_AT_END_OF_TICK;
            moduleList.needResort = true;
            SbutilsClient.moduleList.showModule(this);
        }
    }

    @Override
    public void onEndTick(Minecraft client) {
        if (suppressedAttack) {
            client.options.keyAttack.setDown(isKeyPhysicallyDown(client, client.options.keyAttack));
            suppressedAttack = false;
        }

        if (stage == Stage.RELEASE_SHIFT_AT_END_OF_TICK) {
            if (forcedShift) {
                client.options.keyShift.setDown(isKeyPhysicallyDown(client, client.options.keyShift));
                forcedShift = false;
            }
            stage = Stage.IDLE;
            moduleList.needResort = true;
        }
    }

    private boolean shouldHandle(Minecraft client) {
        return ConfigManager.FEATURES.ENABLE_ETHERWARP_HELPER &&
                client.player != null &&
                SkyblockItem.from(client.player.getMainHandItem())
                        .flatMap(SkyblockItem::getID)
                        .map(ETHERWARP_ITEM_IDS::contains)
                        .orElse(false);
    }

    private void suppressAttack(Minecraft client) {
        client.options.keyAttack.setDown(false);
        suppressedAttack = true;
    }

    private void resetKeys(Minecraft client) {
        if (forcedShift) {
            client.options.keyShift.setDown(isKeyPhysicallyDown(client, client.options.keyShift));
            forcedShift = false;
        }
        if (suppressedAttack) {
            client.options.keyAttack.setDown(isKeyPhysicallyDown(client, client.options.keyAttack));
            suppressedAttack = false;
        }
        if (stage != Stage.IDLE) {
            stage = Stage.IDLE;
            moduleList.needResort = true;
        }
    }

    private boolean isKeyPhysicallyDown(Minecraft client, KeyMapping keyMapping) {
        InputConstants.Key key = ((AccessKeyMapping) keyMapping).getKey();
        return InputConstants.isKeyDown(client.getWindow(), key.getValue());
    }

    @Override
    public String title() {
        return "Etherwarp Helper";
    }

    @Override
    public @Nullable String subtitle() {
        return switch (stage) {
            case READY_TO_RIGHT_CLICK -> "SHIFT";
            case RELEASE_SHIFT_AT_END_OF_TICK -> "CLICK";
            case IDLE -> null;
        };
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_ETHERWARP_HELPER && stage != Stage.IDLE;
    }

    private enum Stage {
        IDLE,
        READY_TO_RIGHT_CLICK,
        RELEASE_SHIFT_AT_END_OF_TICK
    }
}
