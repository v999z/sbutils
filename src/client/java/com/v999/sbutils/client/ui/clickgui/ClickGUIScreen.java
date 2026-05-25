package com.v999.sbutils.client.ui.clickgui;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.ui.Easy2D;
import com.v999.sbutils.client.ui.Space;
import com.v999.sbutils.client.ui.animation.Animation;
import com.v999.sbutils.client.ui.animation.Smooth;
import com.v999.sbutils.client.ui.clickgui.fubuki.FragmentView;
import com.v999.sbutils.client.ui.clickgui.fubuki.nav.DummyNavigationDestination;
import com.v999.sbutils.client.ui.clickgui.fubuki.nav.NavigationCategories;
import com.v999.sbutils.client.ui.clickgui.fubuki.nav.NavigationDestination;
import com.v999.sbutils.client.ui.font.FontManager;
import com.v999.sbutils.client.ui.font.RenderInfo;
import com.v999.sbutils.client.ui.font.RenderedText;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.util.PriorityQueue;

public class ClickGUIScreen extends Screen {
    public static final int LAYER_DEPTH = 1;

    private final Screen parent;
    private final Window window;
    private final PriorityQueue<Element> children = new PriorityQueue<>();
    private final NavigationCategories nav;
    private final FragmentView listFragment;

    private static final float DEFAULT_HALF_WIDTH = 180F;
    private static final float DEFAULT_HALF_HEIGHT = 110F;
    private static final float DEFAULT_OUTER_RADIUS = 24F;
    private static final float DEFAULT_OUTER_PADDING = 18F;
    private static final float COMPACT_OUTER_RADIUS = 18F;
    private static final float COMPACT_OUTER_PADDING = 12F;

    public static boolean isCompactMode() {
        return ConfigManager.GENERAL.CLICK_GUI_COMPACT_MODE;
    }

    public static float outerPadding() {
        return isCompactMode() ? COMPACT_OUTER_PADDING : DEFAULT_OUTER_PADDING;
    }

    public static float titleFontSize() {
        return isCompactMode() ? 12F : 14F;
    }

    public static float navigationFontSize() {
        return isCompactMode() ? 8.5F : 10F;
    }

    private static float halfWidth() {
        return DEFAULT_HALF_WIDTH;
    }

    private static float halfHeight() {
        return DEFAULT_HALF_HEIGHT;
    }

    private static float outerRadius() {
        return isCompactMode() ? COMPACT_OUTER_RADIUS : DEFAULT_OUTER_RADIUS;
    }

    private float lastMouseClickRelativeX = 0, lastMouseClickRelativeY = 0;
    private long lastTickTime = 0;
    private final Animation alpha = new Smooth(0F, 0xEF);

    float centerX, centerY;

    public ClickGUIScreen(Minecraft client, Screen parent) {
        super(Component.literal("Sbutils ClickGUI"));
        this.parent = parent;

        this.window = client.getWindow();
        this.centerX = (float) Math.ceil(window.getGuiScaledWidth() * 0.5F);
        this.centerY = (float) Math.ceil(window.getGuiScaledHeight() * 0.5F);

        this.listFragment = new FragmentView(new GeneralPage(client), LAYER_DEPTH + 1);
        this.children.add(this.listFragment);

        this.nav = new NavigationCategories(ClickGUIScreen::navigationFontSize, window, LAYER_DEPTH + 1);
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "General";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new GeneralPage(client));
                return true;
            }
        });
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "Features";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new FeaturesPage(client));
                return true;
            }
        });
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "Visuals";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new VisualsPage(client));
                return true;
            }
        });
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "Kuudra";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new KuudraPage(client));
                return true;
            }
        });
        this.nav.destinations.add(new NavigationDestination() {
            @Override
            public String name() {
                return "Config";
            }

            @Override
            public boolean navigate() {
                listFragment.push(new ConfigPage(client));
                return true;
            }
        });
        this.nav.destinations.add((DummyNavigationDestination) () -> "Made by v999");
        this.children.add(this.nav);
        //this.children.add(new FragmentView(0F, 0F, new GeneralPage(), LAYER_DEPTH + 1));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(context, mouseX, mouseY, partialTick);
        Easy2D.configure(context);

        float scale = (float) window.getGuiScale();
        long now = Util.getMillis();
        long timeDiff = now - lastTickTime;
        if (timeDiff > 40) timeDiff = 40;
        float halfWidth = halfWidth();
        float halfHeight = halfHeight();
        float outerPadding = outerPadding();
        Space space = new Space(2 * (halfWidth - outerPadding), 2 * (halfHeight - outerPadding));
        space.translate(centerX - halfWidth + outerPadding, centerY - halfHeight + outerPadding);

        // tick animations
        alpha.tick(timeDiff * 0.005F);

        float startX = centerX - halfWidth;
        float startY = centerY - halfHeight;
        // draw background
        int alphaInt = (int) alpha.current;
        Easy2D.drawRoundRect(startX, startY, centerX + halfWidth, centerY + halfHeight,
                outerRadius(), 16F,
                ClickGuiTheme.withAlpha(ClickGuiTheme.backgroundColor(), alphaInt),
                ClickGuiTheme.withAlpha(ClickGuiTheme.backgroundShadowColor(), alphaInt));
        // draw title
        RenderedText titleText = FontManager.requestRenderedText(new RenderInfo(FontManager.BOLD_FONT, "SBUtils", titleFontSize()), (float) window.getGuiScale());
        titleText.draw(context, startX + outerPadding, startY + outerPadding, (float) window.getGuiScale(),
                ClickGuiTheme.withAlpha(ClickGuiTheme.titleTextColor(), alphaInt));

        // set navigation categories
        this.nav.updateStartPosition(
                startX + outerPadding + titleText.bounds.width / scale + outerPadding,
                startY + outerPadding);
        this.nav.width = halfWidth * 2 - outerPadding * 3 - titleText.bounds.width / scale;
        this.nav.height = titleText.bounds.height / scale;
        this.nav.buttonHeight = this.nav.height + (isCompactMode() ? 2F : 4F);

        // set left list
        this.listFragment.updateStartPosition(startX + outerPadding, startY + outerPadding + titleText.bounds.height / scale + outerPadding);
        this.listFragment.updateEndPosition(centerX + halfWidth - outerPadding, centerY + halfHeight - outerPadding);

        // draw children
        for (Element child : children) {
            child.render(context, mouseX, mouseY, timeDiff);
        }

        Easy2D.cleanup();
        lastTickTime = now;
    }

    @Override
    protected void extractBlurredBackground(@NonNull GuiGraphicsExtractor context) {
        if (ConfigManager.GENERAL.CLICK_GUI_BLUR) {
            super.extractBlurredBackground(context);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        float mouseXF = (float) event.x();
        float mouseYF = (float) event.y();
        lastMouseClickRelativeX = mouseXF - centerX;
        lastMouseClickRelativeY = mouseYF - centerY;

        for (Element child : children) {
            if (child.mouseClicked(mouseXF, mouseYF)) return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        float mouseXF = (float) event.x();
        float mouseYF = (float) event.y();
        centerX = mouseXF - lastMouseClickRelativeX;
        centerY = mouseYF - lastMouseClickRelativeY;

        for (Element child : children) {
            if (child.mouseDragged(mouseXF, mouseYF, (float) dragX, (float) dragY)) return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Element child : children) {
            if (child.mouseScrolled((float) mouseX, (float) mouseY, (float) scrollX, (float) scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void resize(int width, int height) {
        for (Element child : children) {
            child.resize();
        }
        super.resize(width, height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return children.stream().allMatch(Element::shouldCloseOnEsc);
    }

    @Override
    public void removed() {
        for (Element child : children) {
            child.remove();
        }
        super.removed();
    }

    @Override
    public void onClose() {
        ConfigManager.processChanges();
        this.minecraft.setScreen(parent);
    }
}
