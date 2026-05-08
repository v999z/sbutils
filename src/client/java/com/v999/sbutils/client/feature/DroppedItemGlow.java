package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DroppedItemGlow extends AbstractModule implements ClientTickEvents.EndTick, LevelRenderEvents.BeforeGizmos {
    public static final DroppedItemGlow INSTANCE = new DroppedItemGlow();

    private int itemCount = 0;

    static {
        if (ConfigManager.FEATURES.ENABLE_DROPPED_ITEM_GLOW) {
            SbutilsClient.moduleList.showModule(INSTANCE);
        }
    }

    @Override
    public void onEndTick(@NonNull Minecraft client) {
        if (client.level == null || !ConfigManager.FEATURES.ENABLE_DROPPED_ITEM_GLOW) {
            itemCount = 0;
            return;
        }

        int count = 0;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity itemEntity && itemEntity.isAlive()) {
                count++;
            }
        }
        itemCount = count;
    }

    @Override
    public void beforeGizmos(@NonNull LevelRenderContext context) {
        if (!ConfigManager.FEATURES.ENABLE_DROPPED_ITEM_GLOW) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            if (!itemEntity.isAlive()) continue;

            AABB box = createStaticBox(itemEntity);

            float pulse = 0.80F + 0.20F * (float) ((Math.sin((System.currentTimeMillis() / 180.0) + itemEntity.getId()) + 1.0) * 0.5);

            int hiddenOutlineColor = (((int) (90 * pulse)) << 24) | 0xFFFFFF;
            int hiddenFillColor = (((int) (18 * pulse)) << 24) | 0xFFFFFF;

            int visibleOutlineColor = (((int) (235 * pulse)) << 24) | 0xFFFFFF;
            int visibleFillColor = (((int) (32 * pulse)) << 24) | 0xFFFFFF;

            Gizmos.cuboid(box.inflate(0.02), new GizmoStyle(hiddenOutlineColor, 1.4F, hiddenFillColor))
                    .setAlwaysOnTop();

            Gizmos.cuboid(box, new GizmoStyle(visibleOutlineColor, 2.0F, visibleFillColor));
        }
    }

    private AABB createStaticBox(ItemEntity itemEntity) {
        AABB bb = itemEntity.getBoundingBox();

        double centerX = (bb.minX + bb.maxX) * 0.5;
        double centerZ = (bb.minZ + bb.maxZ) * 0.5;

        double baseY = Math.floor((bb.minY + 0.02) * 2.0) / 2.0;

        return new AABB(
                centerX - 0.18, baseY + 0.03, centerZ - 0.18,
                centerX + 0.18, baseY + 0.48, centerZ + 0.18
        );
    }

    @Override
    public String title() {
        return "ItemGlow";
    }

    @Override
    public @Nullable String subtitle() {
        return Integer.toString(itemCount);
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_DROPPED_ITEM_GLOW;
    }
}