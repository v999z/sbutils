package com.v999.sbutils.client.ui.clickgui.fubuki.nav;

import com.v999.sbutils.client.ui.Easy2D;
import com.v999.sbutils.client.ui.animation.Animation;
import com.v999.sbutils.client.ui.animation.Smooth;
import com.v999.sbutils.client.ui.clickgui.ClickGuiTheme;
import com.v999.sbutils.client.ui.clickgui.Element;
import com.v999.sbutils.client.ui.font.FontManager;
import com.v999.sbutils.client.ui.font.RenderInfo;
import com.v999.sbutils.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.function.DoubleSupplier;

public class NavigationCategories implements Element {
    public float startX, startY;
    public float width, height;
    public float buttonHeight;
    public float fontSize;
    private final DoubleSupplier fontSizeSupplier;
    public final List<NavigationDestination> destinations = new ObjectArrayList<>();
    public int layerDepth;

    private final Window window;
    private int selectedIndex = 0;
    private Animation highlightStartX = new Smooth(0, 0);
    private Animation highlightEndX = new Smooth(0, 0);
    private List<FloatFloatImmutablePair> categoryTextBounds;

    public NavigationCategories(float fontSize, Window window, int layerDepth) {
        this(() -> fontSize, window, layerDepth);
    }

    public NavigationCategories(DoubleSupplier fontSizeSupplier, Window window, int layerDepth) {
        this.buttonHeight = height;
        this.fontSizeSupplier = fontSizeSupplier;
        this.fontSize = (float) fontSizeSupplier.getAsDouble();
        this.window = window;
        this.layerDepth = layerDepth;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, long timeDiff) {
        float scale = (float) window.getGuiScale();
        this.fontSize = (float) fontSizeSupplier.getAsDouble();

        // tick animations
        highlightStartX.tick(timeDiff * 0.02F);
        highlightEndX.tick(timeDiff * 0.02F);

        // draw selected background
        final float buttonY = startY + (height - buttonHeight) * 0.5F;
        Easy2D.drawRoundRect(highlightStartX.current + startX, buttonY,
                highlightEndX.current + startX, buttonY + buttonHeight, buttonHeight * 0.5F,
                16F, ClickGuiTheme.accentColor(), 0x00000000);

        // draw navigation buttons
        if (destinations.isEmpty()) { // should be avoided
            highlightStartX.target = startX;
            highlightEndX.target = startX + width;
        } else {
            List<RenderedText> categoryRenderedTexts = destinations.stream()
                    .map(destination -> {
                        String name = destination.name();
                        if (name == null || name.isBlank()) name = "|EMPTY|"; // wow
                        return FontManager.requestRenderedText(
                                new RenderInfo(FontManager.DEFAULT_FONT, name, fontSize), (float) window.getGuiScale()
                        );
                    }).toList();
            float gapWidth = destinations.size() == 1 ? 0F : (float) ((width - categoryRenderedTexts.stream()
                    .mapToDouble(it -> it.bounds.width / scale)
                    .reduce(Double::sum)
                    .orElseThrow()) / (destinations.size() - 1));
            float currentX = startX;
            List<FloatFloatImmutablePair> bounds = new ObjectArrayList<>(categoryRenderedTexts.size());
            for (int i = 0; i < categoryRenderedTexts.size(); i++) {
                RenderedText text = categoryRenderedTexts.get(i);
                // TODO: pass alpha value
                text.draw(context, currentX, startY + (height + text.bounds.y / scale) * 0.5F, (float) window.getGuiScale(),
                        i == selectedIndex ? ClickGuiTheme.selectedNavigationTextColor() : ClickGuiTheme.textPrimaryColor());
                if (i == selectedIndex) {
                    highlightStartX.target = currentX - buttonHeight * 0.5F - startX;
                    highlightEndX.target = currentX + text.bounds.width / scale + buttonHeight * 0.5F - startX;
                }
                bounds.add(new FloatFloatImmutablePair(currentX, currentX += text.bounds.width / scale));
                currentX += gapWidth;
            }
            categoryTextBounds = bounds;
        }
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        if (categoryTextBounds != null
                && startX <= mouseX && mouseX <= startX + width
                && startY <= mouseY && mouseY <= startY + height) {
            List<FloatFloatImmutablePair> bounds = categoryTextBounds;
            for (int i = 0; i < bounds.size(); i++) {
                FloatFloatImmutablePair bound = categoryTextBounds.get(i);
                if (bound.leftFloat() <= mouseX && mouseX <= bound.rightFloat()) {
                    if (selectedIndex != i && destinations.get(i).navigate()) {
                        selectedIndex = i;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void resize() {
        // avoid unwanted animations caused by window size changes
        highlightStartX.current = highlightStartX.target;
        highlightEndX.current = highlightEndX.target;
    }

    @Override
    public void remove() {
        if (categoryTextBounds != null) {
            this.categoryTextBounds.clear();
            this.categoryTextBounds = null;
        }
        Element.super.remove();
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }
}
