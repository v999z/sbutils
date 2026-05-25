package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.ui.container.GhostPickaxeContainer;
import com.v999.sbutils.client.util.SkyblockLocation;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class GhostPickaxe extends AbstractModule implements ClientTickEvents.StartTick {
    public static final GhostPickaxe INSTANCE = new GhostPickaxe();

    private static final KeyMapping GHOST_PICKAXE_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.ghost_pickaxe", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, SbutilsClient.KEY_CATEGORY)
    );

    private static int lastSlot = -1;

    @Override
    public void onStartTick(Minecraft client) {
        if (client.player != null && GHOST_PICKAXE_KEY.isDown()) {
            if (BetterDungeonbreaker.slot > 0 && SkyblockLocation.isInDungeons()) { // found Dungeonbreaker in hotbar
                if (lastSlot < 0) { // not swapped yet
                    Inventory inventory = client.player.getInventory();
                    lastSlot = inventory.getSelectedSlot();
                    inventory.setSelectedSlot(BetterDungeonbreaker.slot);
                }
                GhostPickaxeContainer.INSTANCE.isActivated = true;
                SbutilsClient.island.show(GhostPickaxeContainer.INSTANCE);
                SbutilsClient.moduleList.showModule(this);
            }
        } else {
            if (client.player != null && lastSlot >= 0) {
                client.player.getInventory().setSelectedSlot(lastSlot);
                lastSlot = -1;
            }
            GhostPickaxeContainer.INSTANCE.isActivated = false;
        }
    }

    @Override
    public String title() {
        return "GhostPickaxe";
    }

    @Override
    public @Nullable String subtitle() {
        return null;
    }

    @Override
    public boolean isActive() {
        return GhostPickaxeContainer.INSTANCE.isActivated;
    }
}
