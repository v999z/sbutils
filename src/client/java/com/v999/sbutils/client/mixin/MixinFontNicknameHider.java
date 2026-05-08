package com.v999.sbutils.client.mixin;

import com.v999.sbutils.client.util.NicknameHider;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class MixinFontNicknameHider {
    @ModifyVariable(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"), argsOnly = true)
    private String sbutils$replaceDrawString(String text) {
        return NicknameHider.replace(text);
    }

    @ModifyVariable(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"), argsOnly = true)
    private Component sbutils$replaceDrawComponent(Component component) {
        return NicknameHider.replace(component);
    }

    @ModifyVariable(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"), argsOnly = true)
    private FormattedCharSequence sbutils$replaceDrawFormatted(FormattedCharSequence sequence) {
        return NicknameHider.replace(sequence);
    }

    @ModifyVariable(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), argsOnly = true)
    private String sbutils$replaceWidthString(String text) {
        return NicknameHider.replace(text);
    }

    @ModifyVariable(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), argsOnly = true)
    private FormattedText sbutils$replaceWidthFormattedText(FormattedText text) {
        return NicknameHider.replace(text);
    }

    @ModifyVariable(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), argsOnly = true)
    private FormattedCharSequence sbutils$replaceWidthFormatted(FormattedCharSequence text) {
        return NicknameHider.replace(text);
    }

    @ModifyVariable(method = "split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;", at = @At("HEAD"), argsOnly = true)
    private FormattedText sbutils$replaceSplitFormattedText(FormattedText text) {
        return NicknameHider.replace(text);
    }
}
