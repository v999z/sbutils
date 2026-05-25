package com.v999.sbutils.client.ui.clickgui.fubuki;

public interface CullingProvider {
    boolean canBeCulled(float startX, float startY, float endX, float endY);
}
