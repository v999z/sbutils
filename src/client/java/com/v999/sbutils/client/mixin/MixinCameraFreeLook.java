package com.v999.sbutils.client.mixin;

import com.v999.sbutils.client.feature.FreeLook;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.objectweb.asm.Opcodes;

@Mixin(Camera.class)
public abstract class MixinCameraFreeLook {
    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    private float fov;

    @Inject(method = "update", at = @At("HEAD"))
    private void sbutils$applyFreelookRotationBeforeCameraPosition(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (FreeLook.INSTANCE.isActiveNow() && Minecraft.getInstance().player != null) {
            FreeLook.INSTANCE.applyCameraRotation(Minecraft.getInstance().player);
        }
    }

    @Inject(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Camera;fov:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void sbutils$applyFreelookZoomBeforeProjection(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (FreeLook.INSTANCE.isActiveNow()) {
            this.fov = FreeLook.INSTANCE.applyZoomFov(this.fov);
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void sbutils$restoreFreelookRotationAfterCameraPosition(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (FreeLook.INSTANCE.isActiveNow() && Minecraft.getInstance().player != null) {
            FreeLook.INSTANCE.restorePlayerRotation(Minecraft.getInstance().player);
            this.setRotation(FreeLook.INSTANCE.getCameraYaw(), FreeLook.INSTANCE.getCameraPitch());
        }
    }
}
