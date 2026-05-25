package com.v999.sbutils.client.ui.hud;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.ui.Easy2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

public class HudEditorScreen extends Screen {
    private static final float DYNAMIC_ISLAND_PREVIEW_WIDTH = 156F;
    private static final float DYNAMIC_ISLAND_PREVIEW_HEIGHT = 28F;

    private final Screen parent;
    private DragTarget dragging = DragTarget.NONE;
    private float dragOffsetX;
    private float dragOffsetY;

    public HudEditorScreen(Minecraft client, Screen parent) {
        super(client, client.font, Component.literal("SBUtils HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(context);
        Easy2D.configure(context);

        context.text(this.font, Component.literal("SBUtils HUD Editor"), 8, 8, 0xFFFFFFFF, false);
        context.text(this.font, Component.literal("Drag the boxes. Press ESC to save and go back."), 8, 20, 0xFFB8B8B8, false);

        float performanceWidth = PerformanceHud.INSTANCE.estimateWidth(this.minecraft);
        float performanceHeight = PerformanceHud.INSTANCE.estimateHeight(this.minecraft);
        float performanceX = PerformanceHud.clampX(this.minecraft, ConfigManager.FEATURES.PERFORMANCE_HUD_X, performanceWidth);
        float performanceY = PerformanceHud.clampY(this.minecraft, ConfigManager.FEATURES.PERFORMANCE_HUD_Y, performanceHeight);
        ConfigManager.FEATURES.PERFORMANCE_HUD_X = performanceX;
        ConfigManager.FEATURES.PERFORMANCE_HUD_Y = performanceY;

        float islandX = clampX(resolveIslandX(), DYNAMIC_ISLAND_PREVIEW_WIDTH);
        float islandY = clampY(resolveIslandY(), DYNAMIC_ISLAND_PREVIEW_HEIGHT);
        ConfigManager.FEATURES.DYNAMIC_ISLAND_X = islandX;
        ConfigManager.FEATURES.DYNAMIC_ISLAND_Y = islandY;

        int performanceColor = PerformanceHud.backgroundColor();
        Easy2D.drawRoundRect(performanceX, performanceY, performanceX + performanceWidth, performanceY + performanceHeight,
                8F, 10F, performanceColor, performanceColor);
        context.text(this.font, Component.literal(PerformanceHud.INSTANCE.buildDisplayText(this.minecraft)),
                Mth.floor(performanceX + 6F), Mth.floor(performanceY + 4F), 0xFFFFFFFF, false);
        if (PerformanceHud.isGraphEnabled()) {
            PerformanceHud.INSTANCE.drawGraph(context, performanceX + 6F, performanceY + 4F + this.font.lineHeight + 4F,
                    performanceWidth - 12F, PerformanceHud.GRAPH_HEIGHT);
        }

        Easy2D.drawRoundRect(islandX, islandY, islandX + DYNAMIC_ISLAND_PREVIEW_WIDTH, islandY + DYNAMIC_ISLAND_PREVIEW_HEIGHT,
                12F, 12F, 0xDB000000, 0xDB000000);
        context.centeredText(this.font, Component.literal("Dynamic Island / TPS"),
                Mth.floor(islandX + DYNAMIC_ISLAND_PREVIEW_WIDTH * 0.5F), Mth.floor(islandY + 10F), 0xFFFFFFFF);

        Easy2D.cleanup();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, isDoubleClick);
        }

        float performanceWidth = PerformanceHud.INSTANCE.estimateWidth(this.minecraft);
        float performanceHeight = PerformanceHud.INSTANCE.estimateHeight(this.minecraft);
        float performanceX = ConfigManager.FEATURES.PERFORMANCE_HUD_X;
        float performanceY = ConfigManager.FEATURES.PERFORMANCE_HUD_Y;
        float mouseX = (float) event.x();
        float mouseY = (float) event.y();

        if (contains(mouseX, mouseY, performanceX, performanceY, performanceWidth, performanceHeight)) {
            dragging = DragTarget.PERFORMANCE;
            dragOffsetX = mouseX - performanceX;
            dragOffsetY = mouseY - performanceY;
            return true;
        }

        float islandX = resolveIslandX();
        float islandY = resolveIslandY();
        if (contains(mouseX, mouseY, islandX, islandY, DYNAMIC_ISLAND_PREVIEW_WIDTH, DYNAMIC_ISLAND_PREVIEW_HEIGHT)) {
            dragging = DragTarget.DYNAMIC_ISLAND;
            dragOffsetX = mouseX - islandX;
            dragOffsetY = mouseY - islandY;
            return true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        float mouseX = (float) event.x();
        float mouseY = (float) event.y();
        switch (dragging) {
            case PERFORMANCE -> {
                float width = PerformanceHud.INSTANCE.estimateWidth(this.minecraft);
                float height = PerformanceHud.INSTANCE.estimateHeight(this.minecraft);
                ConfigManager.FEATURES.PERFORMANCE_HUD_X = clampX(mouseX - dragOffsetX, width);
                ConfigManager.FEATURES.PERFORMANCE_HUD_Y = clampY(mouseY - dragOffsetY, height);
                ConfigManager.FEATURES.markAsChanged();
                return true;
            }
            case DYNAMIC_ISLAND -> {
                ConfigManager.FEATURES.DYNAMIC_ISLAND_X = clampX(mouseX - dragOffsetX, DYNAMIC_ISLAND_PREVIEW_WIDTH);
                ConfigManager.FEATURES.DYNAMIC_ISLAND_Y = clampY(mouseY - dragOffsetY, DYNAMIC_ISLAND_PREVIEW_HEIGHT);
                ConfigManager.FEATURES.markAsChanged();
                return true;
            }
            case NONE -> {
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = DragTarget.NONE;
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        ConfigManager.FEATURES.markAsChanged();
        ConfigManager.processChanges();
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean contains(float mouseX, float mouseY, float x, float y, float width, float height) {
        return x <= mouseX && mouseX <= x + width && y <= mouseY && mouseY <= y + height;
    }

    private float resolveIslandX() {
        if (ConfigManager.FEATURES.DYNAMIC_ISLAND_X >= 0F) {
            return ConfigManager.FEATURES.DYNAMIC_ISLAND_X;
        }
        return (this.width - DYNAMIC_ISLAND_PREVIEW_WIDTH) * 0.5F;
    }

    private float resolveIslandY() {
        if (ConfigManager.FEATURES.DYNAMIC_ISLAND_Y >= 0F) {
            return ConfigManager.FEATURES.DYNAMIC_ISLAND_Y;
        }
        return 36F;
    }

    private float clampX(float x, float width) {
        return Mth.clamp(x, 2F, this.width - width - 2F);
    }

    private float clampY(float y, float height) {
        return Mth.clamp(y, 2F, this.height - height - 2F);
    }

    private enum DragTarget {
        NONE,
        PERFORMANCE,
        DYNAMIC_ISLAND
    }
}
