package com.v999.sbutils.client.mixin;

import com.v999.sbutils.client.util.NicknameHider;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class MixinPlayerTabOverlayNicknameHider {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void sbutils$replaceNicknameInTabList(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        if (original != null) {
            cir.setReturnValue(NicknameHider.replace(original));
        }
    }
}
