package com.v999.sbutils.client.mixin;

import com.v999.sbutils.client.feature.FreeLook;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandlerFreeLook {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void sbutils$zoomFreelookOnScroll(long window, double scrollX, double scrollY, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (window == client.getWindow().handle()
                && client.screen == null
                && client.getOverlay() == null
                && FreeLook.INSTANCE.onMouseScroll(scrollY)) {
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void sbutils$beforeTurnPlayer(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            FreeLook.INSTANCE.beforeTurn(client.player);
        }
    }

    @Inject(method = "turnPlayer", at = @At("TAIL"))
    private void sbutils$afterTurnPlayer(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            FreeLook.INSTANCE.afterTurn(client.player);
        }
    }
}
