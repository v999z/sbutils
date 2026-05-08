package com.v999.sbutils.client.mixin;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.feature.DayViewer;
import com.v999.sbutils.client.feature.ForagingStyleWarning;
import com.v999.sbutils.client.feature.PickobulusPreview;
import com.v999.sbutils.client.ui.container.ServerTPSContainer;
import com.v999.sbutils.client.util.ChatShortcuts;
import com.v999.sbutils.client.util.NicknameHider;
import com.v999.sbutils.client.util.SkyblockLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    private String sbutils$replaceOutgoingChat(String message) {
        return NicknameHider.replace(message);
    }

    @ModifyVariable(method = "sendCommand", at = @At("HEAD"), argsOnly = true)
    private String sbutils$expandChatShortcuts(String command) {
        return ChatShortcuts.expandCommand(command);
    }

    @Shadow
    public abstract void sendCommand(String command);

    @Inject(method = "handleSetTime", at = @At("TAIL"))
    private void sbutils$handleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        ServerTPSContainer.INSTANCE.whenClientboundSetTime();
        DayViewer.INSTANCE.whenClientboundSetTime();
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void sbutils$handleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        ServerTPSContainer.INSTANCE.whenRespawn();
        PickobulusPreview.INSTANCE.resetCooldown();
        SkyblockLocation.whenRespawn();
    }

    @Inject(method = "handleSoundEvent", at = @At("TAIL"))
    private void sbutils$handleSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        SoundEvent soundEvent = packet.getSound().value();
        if (SoundEvents.LADDER_BREAK.equals(soundEvent)) {
            ForagingStyleWarning.whenWoodBreakSound(packet);
        }
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void sbutils$handleScoreboardUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        SkyblockLocation.whenScoreboardUpdate(packet.entries());
    }

    @Inject(method = "openCommandSendConfirmationWindow", at = @At("HEAD"), cancellable = true)
    private void sbutils$noCommandConfirmation(String command, String titleKey, Screen previousScreen, CallbackInfo ci) {
        if (ConfigManager.PATCHES.NO_COMMAND_EXECUTION_CONFIRMATION) {
            this.sendCommand(command);
            ci.cancel();
        }
    }
}
