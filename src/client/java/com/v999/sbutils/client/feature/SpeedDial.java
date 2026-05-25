package com.v999.sbutils.client.feature;

import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.config.ContactsBook;
import com.v999.sbutils.client.ui.clickgui.fubuki.nav.NavigationSpinner;
import com.v999.sbutils.client.ui.speeddial.ContactDestinationView;
import com.v999.sbutils.client.util.PlayerHead;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class SpeedDial implements ClientTickEvents.StartTick, ScreenEvents.BeforeInit, ScreenEvents.Remove {
    public static final SpeedDial INSTANCE = new SpeedDial();

    private static final KeyMapping SPEED_DIAL_UP_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.speed_dial_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, SbutilsClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_PAGE_UP_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.speed_dial_page_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, SbutilsClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_DOWN_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.speed_dial_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, SbutilsClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_PAGE_DOWN_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.speed_dial_page_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, SbutilsClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_LEFT_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.speed_dial_left", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, SbutilsClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_RIGHT_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.speed_dial_right", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, SbutilsClient.KEY_CATEGORY)
    );
    private static final KeyMapping SPEED_DIAL_STAR = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.speed_dial_star", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_ENTER, SbutilsClient.KEY_CATEGORY)
    );

    private NavigationSpinner focusedNav;
    private long lastActionTime = 0L;

    @Override
    public void beforeInit(@NonNull Minecraft client, @NonNull Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof ContainerScreen containerScreen) {
            String title = containerScreen.getTitle().getString();
            if (title.startsWith("Abiphone Basic") || title.startsWith("Abiphone X") || title.startsWith("Abiphone Flip")) {
                ScreenEvents.remove(screen).register(this);
            }
        }
    }

    @Override
    public void onRemove(@NonNull Screen screen) {
        if (screen instanceof ContainerScreen containerScreen) {
            // scan contacts
            Container container = containerScreen.getMenu().getContainer();
            for (int row = 1; row < 5; row++) {
                for (int column = 1; column < 8; column++) {
                    ItemStack itemStack = container.getItem(row * 9 + column);
                    if (!itemStack.is(Items.PLAYER_HEAD)) {
                        continue;
                    }
                    String itemName = itemStack.getHoverName().getString();
                    if (itemName.startsWith("Abiphone ")) { // probably the shop page
                        return;
                    }
                    String skinB64 = PlayerHead.getSkinFromHead(itemStack).orElse(null);
                    ContactsBook.Contact contact = ConfigManager.CONTACTS.CONTACTS.get(itemName);
                    if (contact == null) { // not found
                        ConfigManager.CONTACTS.CONTACTS.put(itemName, new ContactsBook.Contact(skinB64, false));
                        ConfigManager.CONTACTS.markAsChanged();
                    } else if (!Objects.equals(contact.skin, skinB64)) {
                        ConfigManager.CONTACTS.CONTACTS.put(itemName, new ContactsBook.Contact(skinB64, contact.starred));
                        ConfigManager.CONTACTS.markAsChanged();
                    }
                }
            }
            ConfigManager.processChanges();
        }
    }

    public enum Category {
        STARRED("Starred"), CONTACTS("Contacts");

        public final String name;

        Category(String name) {
            this.name = name;
        }
    }

    public void showCategory(Category category) {
        Window window = Minecraft.getInstance().getWindow();
        switch (category) {
            case STARRED -> {
                SbutilsClient.speedDial.showNavContact(ConfigManager.CONTACTS.CONTACTS
                        .entrySet().stream()
                        .filter(it -> it.getValue().starred)
                        .map(it -> new ContactDestinationView(it.getValue().skin, it.getKey(), window, 3))
                        .toList());
            }
            case CONTACTS -> {
                SbutilsClient.speedDial.showNavContact(ConfigManager.CONTACTS.CONTACTS
                        .entrySet().stream()
                        .map(it -> new ContactDestinationView(it.getValue().skin, it.getKey(), window, 3))
                        .toList()
                );
            }
        }
    }

    @Override
    public void onStartTick(@NonNull Minecraft client) {
        boolean action = false;
        boolean hideAll = false;
        long now = Util.getMillis();
        if (focusedNav != null && now - lastActionTime > 10_000L) {
            // auto hide if no operation for 10s
            hideAll = true;
        }

        while (SPEED_DIAL_RIGHT_KEY.consumeClick()) {
            if (focusedNav == null) {
                focusedNav = SbutilsClient.speedDial.showNavDock();
            } else if (focusedNav == SbutilsClient.speedDial.navDock) {
                if (focusedNav.navigate()) {
                    focusedNav.setFocused(false);
                    focusedNav = SbutilsClient.speedDial.navContact;
                }
            } else if (focusedNav == SbutilsClient.speedDial.navContact) {
                if (focusedNav.navigate()) {
                    hideAll = true;
                }
            } else {
                throw new IllegalStateException("Unexpected speed dial menu state: " + focusedNav);
            }
            focusedNav.setFocused(true);
            action = true;
        }
        while (SPEED_DIAL_LEFT_KEY.consumeClick() || hideAll) {
            if (focusedNav == null) break;
            //focusedNav.setFocused(false);
            if (focusedNav == SbutilsClient.speedDial.navDock) {
                SbutilsClient.speedDial.hideNavDock();
                focusedNav = null;
            } else if (focusedNav == SbutilsClient.speedDial.navContact) {
                SbutilsClient.speedDial.hideNavContact();
                focusedNav = SbutilsClient.speedDial.navDock;
                focusedNav.setFocused(true);
            }
            action = true;
        }
        while (SPEED_DIAL_UP_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinUp();
            action = true;
        }
        while (SPEED_DIAL_PAGE_UP_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinPageUp();
            action = true;
        }
        while (SPEED_DIAL_DOWN_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinDown();
            action = true;
        }
        while (SPEED_DIAL_PAGE_DOWN_KEY.consumeClick()) {
            if (focusedNav != null) focusedNav.spinPageDown();
            action = true;
        }
        while (SPEED_DIAL_STAR.consumeClick()) {
            if (focusedNav != null && focusedNav == SbutilsClient.speedDial.navContact) {
                ContactDestinationView contactDestinationView = (ContactDestinationView) focusedNav.getSelected();
                if (contactDestinationView != null) {
                    ContactsBook.Contact contact = ConfigManager.CONTACTS.CONTACTS.get(contactDestinationView.contactName);
                    if (contact != null) { // I don't know how to handle it if it's null just why?
                        ConfigManager.CONTACTS.CONTACTS.put(contactDestinationView.contactName,
                                new ContactsBook.Contact(contact.skin, !contact.starred));
                        ConfigManager.CONTACTS.markAsChanged();
                        ConfigManager.processChanges();
                        if (client.player != null) {
                            client.player.sendSystemMessage(Component.literal(
                                    "[Sbutils] Marked " + contactDestinationView.contactName + " as " + (contact.starred ? "not starred." : "starred.")));
                        }
                    }
                }
            }
        }

        if (action) {
            lastActionTime = now;
        }
    }
}
