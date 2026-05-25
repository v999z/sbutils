package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.util.SkyblockItem;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.function.BooleanSupplier;

public class ToggleUse extends AbstractModule implements ClientTickEvents.StartTick, BooleanSupplier {
    public static final ToggleUse INSTANCE = new ToggleUse();

    private boolean forceUse = false;
    private boolean lastState = false;
    private boolean isDown = false;

    @Override
    public boolean getAsBoolean() {
        return forceUse;
    }

    @Override
    public void onStartTick(Minecraft client) {
        forceUse = false;

        if (client.player == null) return;
        if (ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE &&
                SkyblockItem.from(client.player.getMainHandItem())
                        .flatMap(SkyblockItem::getID)
                        .map(ToggleUse::normalize)
                        .map(this::isWhitelisted)
                        .orElse(false)
        ) {
            forceUse = true;
            lastState = true;
            if (isDown != client.options.keyUse.isDown()) {
                isDown = !isDown;
                moduleList.needResort = true;
            }
            SbutilsClient.moduleList.showModule(this);
        } else if (lastState) {
            lastState = false;
            client.options.keyUse.setDown(false);
        }
    }

    private boolean isWhitelisted(String itemId) {
        return !itemId.isEmpty() && normalizeAll(ConfigManager.FEATURES.FORCE_TOGGLE_USE_WHITELIST).contains(itemId);
    }

    public static ObjectOpenHashSet<String> normalizeAll(Collection<String> rawList) {
        ObjectOpenHashSet<String> ids = new ObjectOpenHashSet<>();
        if (rawList == null) {
            return ids;
        }
        for (String entry : rawList) {
            String normalized = normalize(entry);
            if (!normalized.isEmpty()) {
                ids.add(normalized);
            }
        }
        return ids;
    }

    public static String normalize(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String title() {
        return "ToggleUse";
    }

    @Override
    public @Nullable String subtitle() {
        return isDown ? "ON" : "OFF";
    }

    @Override
    public boolean isActive() {
        return forceUse && ConfigManager.FEATURES.ENABLE_FORCE_TOGGLE_USE;
    }
}
