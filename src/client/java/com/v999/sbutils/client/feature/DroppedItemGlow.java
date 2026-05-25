package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.util.SkyblockItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
            if (entity instanceof ItemEntity itemEntity && itemEntity.isAlive() && shouldGlow(itemEntity)) {
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
            if (!shouldGlow(itemEntity)) continue;

            AABB box = createStaticBox(itemEntity);

            float pulse = 0.80F + 0.20F * (float) ((Math.sin((System.currentTimeMillis() / 180.0) + itemEntity.getId()) + 1.0) * 0.5);
            int baseColor = baseColorFor(itemEntity);

            int hiddenOutlineColor = withPulseAlpha(baseColor, (int) (90 * pulse));
            int hiddenFillColor = withPulseAlpha(baseColor, (int) (18 * pulse));

            int visibleOutlineColor = withPulseAlpha(baseColor, (int) (235 * pulse));
            int visibleFillColor = withPulseAlpha(baseColor, (int) (32 * pulse));

            Gizmos.cuboid(box.inflate(0.02), new GizmoStyle(hiddenOutlineColor, 1.4F, hiddenFillColor))
                    .setAlwaysOnTop();

            Gizmos.cuboid(box, new GizmoStyle(visibleOutlineColor, 2.0F, visibleFillColor));
        }
    }

    private boolean shouldGlow(ItemEntity itemEntity) {
        if (!ConfigManager.FEATURES.DROPPED_ITEM_GLOW_ONLY_MATCHING) {
            return true;
        }

        List<String> filters = normalizedFilters();
        if (filters.isEmpty()) {
            return false;
        }

        ItemInfo info = ItemInfo.from(itemEntity.getItem());
        for (String filter : filters) {
            if (matchesFilter(info, filter)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesFilter(ItemInfo info, String filter) {
        int prefixSeparator = filter.indexOf(':');
        if (prefixSeparator > 0) {
            String prefix = filter.substring(0, prefixSeparator);
            String value = filter.substring(prefixSeparator + 1).trim();
            if (value.isEmpty()) return false;

            return switch (prefix) {
                case "id" -> info.id.map(id -> normalize(id).equals(value) || normalize(id).contains(value)).orElse(false);
                case "name" -> info.name.contains(value);
                case "rarity" -> info.rarity.map(rarity -> rarity.matches(value)).orElse(false);
                default -> matchesAny(info, filter);
            };
        }

        return matchesAny(info, filter);
    }

    private static boolean matchesAny(ItemInfo info, String filter) {
        if (info.name.contains(filter)) return true;
        if (info.id.map(id -> normalize(id).equals(filter) || normalize(id).contains(filter)).orElse(false)) return true;
        return info.rarity.map(rarity -> rarity.matches(filter)).orElse(false);
    }

    private static int baseColorFor(ItemEntity itemEntity) {
        if (!ConfigManager.FEATURES.DROPPED_ITEM_GLOW_RARITY_COLORS) {
            return 0xFFFFFF;
        }
        return ItemInfo.from(itemEntity.getItem()).rarity.map(ItemRarity::color).orElse(0xFFFFFF);
    }

    private static int withPulseAlpha(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
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

    public static String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeFilter(String value) {
        return normalize(value);
    }

    public static List<String> normalizedFilters() {
        if (ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS == null) {
            ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS = new ArrayList<>();
        }
        return ConfigManager.FEATURES.DROPPED_ITEM_GLOW_FILTERS.stream()
                .map(DroppedItemGlow::normalizeFilter)
                .filter(filter -> !filter.isEmpty())
                .distinct()
                .toList();
    }

    public static String filterListDisplay() {
        List<String> filters = normalizedFilters();
        if (filters.isEmpty()) {
            return "(empty)";
        }
        return String.join(", ", filters);
    }

    public static int filterCount() {
        return normalizedFilters().size();
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

    private record ItemInfo(String name, Optional<String> id, Optional<ItemRarity> rarity) {
        private static ItemInfo from(ItemStack stack) {
            String name = normalize(stack.getHoverName().getString());
            Optional<SkyblockItem> skyblockItem = SkyblockItem.from(stack);
            Optional<String> id = skyblockItem.flatMap(SkyblockItem::getID).map(DroppedItemGlow::normalize);
            Optional<ItemRarity> rarity = skyblockItem
                    .flatMap(SkyblockItem::getStyledLoreLines)
                    .flatMap(ItemRarity::fromLore);
            return new ItemInfo(name, id, rarity);
        }
    }

    private enum ItemRarity {
        COMMON("common", 0xFFFFFF),
        UNCOMMON("uncommon", 0x55FF55),
        RARE("rare", 0x5555FF),
        EPIC("epic", 0xAA00AA),
        LEGENDARY("legendary", 0xFFAA00),
        MYTHIC("mythic", 0xFF55FF),
        DIVINE("divine", 0x55FFFF),
        SPECIAL("special", 0xFF5555),
        VERY_SPECIAL("very special", 0xFF5555),
        SUPREME("supreme", 0xAA0000);

        private final String displayName;
        private final int color;

        ItemRarity(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }

        int color() {
            return color;
        }

        boolean matches(String filter) {
            return normalize(displayName).equals(filter) || normalize(name()).equals(filter) || normalize(name()).replace('_', ' ').equals(filter);
        }

        static Optional<ItemRarity> fromLore(List<Component> lines) {
            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i).getString().toUpperCase(Locale.ROOT);
                if (line.contains("VERY SPECIAL")) return Optional.of(VERY_SPECIAL);
                if (line.contains("LEGENDARY")) return Optional.of(LEGENDARY);
                if (line.contains("UNCOMMON")) return Optional.of(UNCOMMON);
                if (line.contains("COMMON")) return Optional.of(COMMON);
                if (line.contains("MYTHIC")) return Optional.of(MYTHIC);
                if (line.contains("DIVINE")) return Optional.of(DIVINE);
                if (line.contains("SPECIAL")) return Optional.of(SPECIAL);
                if (line.contains("SUPREME")) return Optional.of(SUPREME);
                if (line.contains("RARE")) return Optional.of(RARE);
                if (line.contains("EPIC")) return Optional.of(EPIC);
            }
            return Optional.empty();
        }
    }
}
