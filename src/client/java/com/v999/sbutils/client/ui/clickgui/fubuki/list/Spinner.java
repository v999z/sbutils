package com.v999.sbutils.client.ui.clickgui.fubuki.list;

import com.v999.sbutils.client.ui.RoundRectRenderer;
import com.v999.sbutils.client.ui.animation.Animation;
import com.v999.sbutils.client.ui.animation.Smooth;
import com.v999.sbutils.client.ui.clickgui.ClickGuiTheme;
import com.v999.sbutils.client.ui.clickgui.Element;
import com.v999.sbutils.client.ui.clickgui.fubuki.ScrollWrapper;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import java.util.Iterator;
import java.util.List;

public class Spinner<T extends MeasurableElement> implements Element {
    public final int layerDepth;
    private final ListView<T> listView;
    private final ScrollWrapper scroll;
    private final Animation highlightStartX = new Smooth(0F, 0F);
    private final Animation highlightEndX = new Smooth(0F, 0F);
    private final Animation colorPercent = new Smooth(1F, 1F);

    public List<T> elementList;
    private float startX, startY, endX, endY;
    private int selectedIndex = 0;
    private @Setter boolean isFocused = true;

    public Spinner(List<T> elementList, float gap, int layerDepth) {
        this.elementList = elementList;
        this.layerDepth = layerDepth;
        this.listView = new ListView<>(elementList, gap, layerDepth);
        this.scroll = new ScrollWrapper(listView);
    }

    public boolean spinUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            return true;
        }
        return false;
    }

    public boolean spinPageUp() {
        if (selectedIndex > 3) {
            selectedIndex -= 3;
            return true;
        } else if (selectedIndex > 0) {
            selectedIndex = 0;
            return true;
        }
        return false;
    }

    public boolean spinDown() {
        if (selectedIndex < elementList.size() - 1) {
            selectedIndex++;
            return true;
        }
        return false;
    }

    public boolean spinPageDown() {
        int size = elementList.size();
        if (selectedIndex < size - 4) {
            selectedIndex += 3;
            return true;
        } else if (selectedIndex < size - 1) {
            selectedIndex = size - 1;
            return true;
        }
        return false;
    }

    public void setSelected(int index) {
        selectedIndex = index;
    }

    public T getSelected() {
        return elementList.size() > selectedIndex ? elementList.get(selectedIndex) : null;
    }

    private void updateAnimation() {
        if (isFocused) {
            highlightStartX.target = 2F;
            highlightEndX.target = endX - startX - 2F;
            colorPercent.target = 1F;
        } else {
            highlightStartX.target = 0F;
            highlightEndX.target = 1F;
            colorPercent.target = 0F;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, long timeDiff) {
        float sumY = listView.gap;
        Iterator<T> iterator = elementList.iterator();
        for (int i = 0; i < selectedIndex; i++) {
            T element = iterator.next();
            sumY += element.measureHeight() + listView.gap;
        }
        if (iterator.hasNext()) { // if the selected element exists
            T selectedElement = iterator.next();
            float selectedElementHalfHeight = selectedElement.measureHeight() * 0.5F;
            sumY += selectedElementHalfHeight;

            // tick animation
            updateAnimation();
            highlightStartX.tick(timeDiff * 0.01F);
            highlightEndX.tick(timeDiff * 0.01F);
            colorPercent.tick(timeDiff * 0.01F);

            // draw highlight background
            float centerY = (endY + startY) * 0.5F;
            int accentColor = ClickGuiTheme.accentColor();
            var colorHSV = com.v999.sbutils.client.util.HSV.fromRGB(accentColor);
            int color = Mth.hsvToArgb(colorHSV.h, colorHSV.s * colorPercent.current, colorHSV.v, 0xFF);
            RoundRectRenderer.State highlightState = new RoundRectRenderer.State(context,
                    startX + highlightStartX.current, centerY - selectedElementHalfHeight, startX + highlightEndX.current, centerY + selectedElementHalfHeight,
                    4F, 16F, color, color);
            if (!isFocused) {
                highlightState.radiusLT = highlightState.radiusLB = 0F;
            }
            context.guiRenderState.addPicturesInPictureState(highlightState);
        }

        // draw scroll wrapper with the list view
        scroll.setVerticalOffset((endY - startY) * 0.5F - sumY);
        scroll.render(context, mouseX, mouseY, timeDiff);
    }

    @Override
    public void updateStartPosition(float newX, float newY) {
        this.startX = newX;
        this.startY = newY;
        this.scroll.updateStartPosition(newX, newY);
    }

    @Override
    public void updateEndPosition(float newX, float newY) {
        this.endX = newX;
        this.endY = newY;
        this.scroll.updateEndPosition(newX, newY);
    }

    @Override
    public void resize() {
        updateAnimation();
        highlightStartX.current = highlightStartX.target;
        highlightEndX.current = highlightEndX.target;
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float scrollX, float scrollY) {
        if (startX < mouseX && mouseX < endX && startY < mouseY && mouseY < endY) {
            if (scrollY > 0) { // scroll down
                return spinUp();
            } else if (scrollY < 0) { // scroll up
                return spinDown();
            }
        }
        return false;
    }

    @Override
    public int getLayerDepth() {
        return 0;
    }
}
