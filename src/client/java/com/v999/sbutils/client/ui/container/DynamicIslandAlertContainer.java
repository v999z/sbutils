package com.v999.sbutils.client.ui.container;

import com.v999.sbutils.client.mixin.AccessFont;
import com.v999.sbutils.client.ui.Alignment;
import com.v999.sbutils.client.ui.Easy2D;
import com.v999.sbutils.client.ui.VanillaText;
import com.v999.sbutils.client.ui.island.ContainerLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

import java.util.List;

public class DynamicIslandAlertContainer implements IContainer {
    public static final DynamicIslandAlertContainer INSTANCE = new DynamicIslandAlertContainer();

    public static final int RARE_DROP_COLOR = 0xFFFF55FF;
    public static final int MENTION_COLOR = 0xFF55FFFF;
    public static final int AUCTION_COLOR = 0xFFFFD24A;
    public static final int PRIVATE_MESSAGE_COLOR = 0xFFB388FF;

    private String title = "Alert";
    private String detail = "Check chat for details";
    private int accentColor = 0xFFFFFFFF;
    private long activeUntil = 0L;
    private List<VanillaText> vanillaTexts;

    private DynamicIslandAlertContainer() {
    }

    public void show(String title, String detail, int accentColor, long activeUntil) {
        this.title = title == null || title.isBlank() ? "Alert" : title;
        this.detail = detail == null || detail.isBlank() ? "Check chat for details" : detail;
        this.accentColor = accentColor;
        this.activeUntil = activeUntil;
    }

    @Override
    public boolean isActive() {
        return Util.getMillis() < activeUntil;
    }

    @Override
    public int getLevel() {
        return ContainerLevel.WARNING;
    }

    @Override
    public void prepareRender(float scale) {
        vanillaTexts = ObjectArrayList.wrap(new VanillaText[2], 0);
        vanillaTexts.add(new VanillaText(title).color(accentColor));
        vanillaTexts.add(new VanillaText(detail).startFrom(Alignment.START));
    }

    @Override
    public float estimateHeight() {
        return 12F + Minecraft.getInstance().font.lineHeight * vanillaTexts.size();
    }

    @Override
    public float estimateWidth() {
        String widest = vanillaTexts.stream().map(it -> it.text)
                .reduce((a, b) -> a.length() > b.length() ? a : b).orElse(title);
        return 22F + ((AccessFont) Minecraft.getInstance().font).getSplitter().stringWidth(widest);
    }

    @Override
    public void render(GuiGraphicsExtractor context, float left, float top, float right, float bottom, float scale) {
        Easy2D.drawScreenTextElements(Minecraft.getInstance().font,
                left + 11F, right, (top + bottom) * 0.5F,
                false, vanillaTexts);
        vanillaTexts.clear();
        vanillaTexts = null;
    }
}
