package com.v999.sbutils.client.ui.clickgui.fubuki.list;

import com.v999.sbutils.client.ui.clickgui.Element;
import com.v999.sbutils.client.ui.clickgui.fubuki.CullingProvider;
import com.v999.sbutils.client.ui.clickgui.fubuki.CullingReceiver;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;

import java.util.SequencedCollection;

public class ListView<T extends MeasurableElement> implements Element, CullingReceiver {
    public SequencedCollection<T> elementList;
    public float gap;
    public int layerDepth;
    private float startX, startY, endX, endY;
    private CullingProvider cullingProvider;

    public ListView(SequencedCollection<T> elementList, float gap, int layerDepth) {
        this.elementList = elementList;
        this.gap = gap;
        this.layerDepth = layerDepth;
    }

    public float measureContentHeight() {
        if (elementList == null || elementList.isEmpty()) {
            return gap * 2F;
        }
        float height = gap;
        for (T element : elementList) {
            height += element.measureHeight() + gap;
        }
        return height;
    }

    private Element getElementByPosition(float x, float y) {
        if (startX > x || endX < x) {
            return null;
        }
        float currentY = startY + gap;
        if (currentY > y) return null;
        for (T element : elementList) {
            float measuredHeight = element.measureHeight();
            if (currentY <= y && currentY + measuredHeight >= y) {
                return element;
            }
            currentY += measuredHeight + gap;
        }
        return null;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, long timeDiff) {
        float currentY = startY + gap;
        for (T element : elementList) {
            float measuredHeight = element.measureHeight();
            if (cullingProvider != null && !cullingProvider.canBeCulled(startX, currentY, endX, currentY + measuredHeight)) {
                element.updateStartPosition(startX, currentY);
                element.updateEndPosition(endX, currentY + measuredHeight);
                element.render(context, mouseX, mouseY, timeDiff);
            }
            currentY += measuredHeight + gap;
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
    public void resize() {
        if (elementList != null) {
            elementList.forEach(Element::resize);
        }
    }

    @Override
    public void remove() {
        if (elementList != null) {
            elementList.forEach(Element::remove);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return elementList == null || elementList.stream().allMatch(Element::shouldCloseOnEsc);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY) {
        Element selected = getElementByPosition(mouseX, mouseY);
        if (selected != null) {
            return selected.mouseClicked(mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, float dragX, float dragY) {
        Element selected = getElementByPosition(mouseX, mouseY);
        if (selected != null) {
            return selected.mouseDragged(mouseX, mouseY, dragX, dragY);
        }
        return false;
    }

    @Override
    public int getLayerDepth() {
        return layerDepth;
    }

    @Override
    public int compareTo(@NonNull Element o) {
        return 0;
    }

    @Override
    public void setCullingProvider(@NonNull CullingProvider provider) {
        this.cullingProvider = provider;
    }
}
