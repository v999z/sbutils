package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.ui.clickgui.fubuki.Switch;
import com.v999.sbutils.client.ui.clickgui.fubuki.list.MeasurableElement;
import com.v999.sbutils.client.ui.font.FontManager;
import com.v999.sbutils.client.ui.font.RenderInfo;
import com.v999.sbutils.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ModuleItemView implements MeasurableElement {
    public static final int LAYER_DEPTH = ClickGUIScreen.LAYER_DEPTH + 3;
    public static final float DEFAULT_HEIGHT = 14F;
    public static final float COMPACT_HEIGHT = 11.5F;

    private static float height() {
        return COMPACT_HEIGHT;
    }

    private static float titleFontSize() {
        return 7F;
    }

    private static float subtitleFontSize() {
        return 5.25F;
    }

    private static float subtitleOffsetY() {
        return 6.6F;
    }

    public @NonNull String title;
    public @Nullable String subtitle;

    private float startX, startY, endX, endY;
    private final Window window;
    private @Nullable Runnable callback;
    private @Nullable Switch simpleSwitcher;
    private @Nullable Supplier<String> subtitleSupplier;
    private @Nullable BooleanSupplier expandedSupplier;
    private @Nullable List<ModuleItemView> dropdownChildren;

    private ModuleItemView(Minecraft client, @NonNull String title, @Nullable String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        this.window = client.getWindow();
    }

    ModuleItemView(Minecraft client, @NonNull String title, String subtitle, boolean switcherValue, @NonNull BooleanConsumer simpleSwitcherCallback) {
        this(client, title, subtitle);
        this.simpleSwitcher = new Switch(ClickGuiTheme::accentColor, switcherValue, LAYER_DEPTH + 1, simpleSwitcherCallback);
    }

    ModuleItemView(Minecraft client, @NonNull String title, String subtitle, @NonNull Runnable callback) {
        this(client, title, subtitle);
        this.callback = callback;
    }

    ModuleItemView(Minecraft client, @NonNull String title, @NonNull Supplier<String> subtitleSupplier, @NonNull Runnable callback) {
        this(client, title, (String) null);
        this.subtitleSupplier = subtitleSupplier;
        this.callback = callback;
    }

    ModuleItemView(Minecraft client, @NonNull String title, @NonNull Supplier<String> subtitleSupplier,
                   @NonNull BooleanSupplier expandedSupplier, @NonNull Runnable callback,
                   @NonNull List<ModuleItemView> dropdownChildren) {
        this(client, title, (String) null);
        this.subtitleSupplier = subtitleSupplier;
        this.expandedSupplier = expandedSupplier;
        this.callback = callback;
        this.dropdownChildren = dropdownChildren;
    }

    @Override
    public float measureHeight() {
        float measuredHeight = height();
        if (isDropdownExpanded()) {
            for (ModuleItemView child : dropdownChildren) {
                measuredHeight += AbstractPage.itemGap() + child.measureHeight();
            }
        }
        return measuredHeight;
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalizedQuery = query.trim().toLowerCase(java.util.Locale.ROOT);
        String resolvedSubtitle = subtitleSupplier != null ? subtitleSupplier.get() : subtitle;
        return title.toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery)
                || (resolvedSubtitle != null && resolvedSubtitle.toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery));
    }

    @Override
    public int getLayerDepth() {
        return LAYER_DEPTH;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        String resolvedSubtitle = subtitleSupplier != null ? subtitleSupplier.get() : subtitle;

        RenderedText titleText = FontManager.requestRenderedText(
                new RenderInfo(FontManager.DEFAULT_FONT, title, titleFontSize()), scale);
        titleText.draw(context, startX, startY, scale, ClickGuiTheme.textPrimaryColor());
        if (resolvedSubtitle != null) {
            RenderedText subtitleText = FontManager.requestRenderedText(
                    new RenderInfo(FontManager.DEFAULT_FONT, resolvedSubtitle, subtitleFontSize()), scale);
            subtitleText.draw(context, startX, startY + subtitleOffsetY(), scale, ClickGuiTheme.textSecondaryColor());
        }

        if (simpleSwitcher != null) {
            float offsetY = 0.5F * (height() - Switch.HEIGHT);
            simpleSwitcher.updateStartPosition(endX - Switch.WIDTH - 2F, startY + offsetY);
            simpleSwitcher.updateEndPosition(endX - 2F, endY - offsetY);
            simpleSwitcher.render(context, mouseX, mouseY, timeDiff);
        }

        if (isDropdownExpanded()) {
            float childStartY = startY + height() + AbstractPage.itemGap();
            float childStartX = startX + 8F;
            for (ModuleItemView child : dropdownChildren) {
                float childHeight = child.measureHeight();
                child.updateStartPosition(childStartX, childStartY);
                child.updateEndPosition(endX, childStartY + childHeight);
                child.render(context, mouseX, mouseY, timeDiff);
                childStartY += childHeight + AbstractPage.itemGap();
            }
        }
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
        this.endX = newX;
        this.endY = newY;
    }

    @Override
    public void remove() {
        MeasurableElement.super.remove();
        if (dropdownChildren != null) {
            for (ModuleItemView child : dropdownChildren) {
                child.remove();
            }
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (isDropdownExpanded() && dropdownChildren != null) {
            for (ModuleItemView child : dropdownChildren) {
                if (child.startX <= mouseX && mouseX <= child.endX && child.startY <= mouseY && mouseY <= child.endY) {
                    return child.mouseClicked(mouseX, mouseY);
                }
            }
            if (mouseY > startY + height()) return true;
        }
        if (callback != null) {
            callback.run();
        } else if (simpleSwitcher != null) {
            simpleSwitcher.mouseClicked(mouseX, mouseY);
        } else {
            return false;
        }
        return true;
    }

    private boolean isDropdownExpanded() {
        return expandedSupplier != null && dropdownChildren != null && expandedSupplier.getAsBoolean();
    }
}
