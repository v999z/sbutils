package com.v999.sbutils.client.feature;

import com.mojang.blaze3d.platform.InputConstants;
import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class FreeLook extends AbstractModule implements ClientTickEvents.StartTick {
    public static final FreeLook INSTANCE = new FreeLook();
    private static final KeyMapping FREELOOK_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sbutils.freelook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, SbutilsClient.KEY_CATEGORY)
    );

    private boolean active = false;
    private CameraType previousCameraType = CameraType.FIRST_PERSON;
    private float cameraYaw = 0F;
    private float cameraPitch = 0F;
    private int zoomLevel = 0;

    private float lockedYaw = 0F;
    private float lockedPitch = 0F;
    private float lockedHeadYaw = 0F;
    private float lockedBodyYaw = 0F;

    private float turnStartYaw = 0F;
    private float turnStartPitch = 0F;

    @Override
    public void onStartTick(Minecraft client) {
        if (!ConfigManager.FEATURES.ENABLE_FREELOOK || client.player == null) {
            deactivate(client);
            return;
        }

        while (FREELOOK_KEY.consumeClick()) {
            if (active) {
                deactivate(client);
            } else if (client.screen == null) {
                activate(client, client.player);
            }
        }

        if (active && client.screen == null) {
            SbutilsClient.moduleList.showModule(this);
        }
    }

    private void activate(Minecraft client, LocalPlayer player) {
        active = true;
        previousCameraType = client.options.getCameraType();
        if (previousCameraType.isFirstPerson()) {
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        lockedYaw = player.getYRot();
        lockedPitch = player.getXRot();
        lockedHeadYaw = player.yHeadRot;
        lockedBodyYaw = player.yBodyRot;
        cameraYaw = lockedYaw;
        cameraPitch = lockedPitch;
        zoomLevel = 0;
        moduleList.needResort = true;
        SbutilsClient.moduleList.showModule(this);
    }

    public void deactivate(Minecraft client) {
        if (!active) {
            return;
        }
        active = false;
        zoomLevel = 0;
        client.options.setCameraType(previousCameraType);
        var player = client.player;
        if (player != null) {
            restorePlayerRotation(player);
        }
        moduleList.needResort = true;
    }

    public void beforeTurn(LocalPlayer player) {
        if (!active) {
            return;
        }
        turnStartYaw = player.getYRot();
        turnStartPitch = player.getXRot();
    }

    public void afterTurn(LocalPlayer player) {
        if (!active) {
            return;
        }
        cameraYaw = Mth.wrapDegrees(cameraYaw + (player.getYRot() - turnStartYaw));
        cameraPitch = Mth.clamp(cameraPitch + (player.getXRot() - turnStartPitch), -90F, 90F);
        restorePlayerRotation(player);
        moduleList.needResort = true;
        SbutilsClient.moduleList.showModule(this);
    }

    public void applyCameraRotation(LocalPlayer player) {
        player.setYRot(cameraYaw);
        player.setXRot(cameraPitch);
        player.yRotO = cameraYaw;
        player.xRotO = cameraPitch;
        player.yHeadRot = cameraYaw;
        player.yHeadRotO = cameraYaw;
        player.yBodyRot = lockedBodyYaw;
        player.yBodyRotO = lockedBodyYaw;
    }

    public void restorePlayerRotation(LocalPlayer player) {
        player.setYRot(lockedYaw);
        player.setXRot(lockedPitch);
        player.yRotO = lockedYaw;
        player.xRotO = lockedPitch;
        player.yHeadRot = lockedHeadYaw;
        player.yHeadRotO = lockedHeadYaw;
        player.yBodyRot = lockedBodyYaw;
        player.yBodyRotO = lockedBodyYaw;
    }

    public boolean isActiveNow() {
        return active;
    }

    public boolean onMouseScroll(double scrollY) {
        if (!active) {
            return false;
        }
        if (scrollY > 0D) {
            zoomLevel = Mth.clamp(zoomLevel + 1, 0, 8);
        } else if (scrollY < 0D) {
            zoomLevel = Mth.clamp(zoomLevel - 1, 0, 8);
        } else {
            return false;
        }
        moduleList.needResort = true;
        SbutilsClient.moduleList.showModule(this);
        return true;
    }

    public float applyZoomFov(float fov) {
        if (!active || zoomLevel <= 0) {
            return fov;
        }
        float zoomMultiplier = 1F - zoomLevel * 0.08F;
        return Mth.clamp(fov * zoomMultiplier, 15F, fov);
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    @Override
    public String title() {
        return "Freelook";
    }

    @Override
    public @Nullable String subtitle() {
        if (!active) return null;
        return zoomLevel > 0 ? "Zoom " + zoomLevel : "ON";
    }

    @Override
    public boolean isActive() {
        return ConfigManager.FEATURES.ENABLE_FREELOOK && active;
    }
}
