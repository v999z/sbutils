package com.v999.sbutils.client.mixin;

import com.v999.sbutils.client.config.ConfigManager;
import com.v999.sbutils.client.feature.AutoConversation;
import com.v999.sbutils.client.ui.clickgui.ClickGuiTheme;
import com.v999.sbutils.client.util.NicknameHider;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true)
    private Component sbutils$replaceNicknameInChatHistory(Component message) {
        Component replaced = NicknameHider.replace(message);
        AutoConversation.INSTANCE.onReceiveChat(replaced);
        return replaced;
    }

    @Unique private static String sbutils$lastCompactKey = null;
    @Unique private static GuiMessageSource sbutils$lastCompactSource = null;
    @Unique private static int sbutils$duplicateCount = 1;

    @Inject(method = "addMessage", at = @At("TAIL"))
    private void sbutils$compactDuplicateMessages(Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        if (!ConfigManager.FEATURES.ENABLE_COMPACT_DUPLICATE_MESSAGES) {
            sbutils$resetCounter();
            return;
        }

        String key = message.getString();
        if (key.isBlank()) {
            sbutils$resetCounter();
            return;
        }

        ChatComponent chatComponent = (ChatComponent) (Object) this;
        AccessChatComponent accessor = (AccessChatComponent) chatComponent;
        List<GuiMessage> allMessages = accessor.getAllMessages();

        if (allMessages.size() < 2) {
            sbutils$lastCompactKey = key;
            sbutils$lastCompactSource = source;
            sbutils$duplicateCount = 1;
            return;
        }

        if (!key.equals(sbutils$lastCompactKey) || source != sbutils$lastCompactSource) {
            sbutils$lastCompactKey = key;
            sbutils$lastCompactSource = source;
            sbutils$duplicateCount = 1;
            return;
        }

        GuiMessage newest = allMessages.get(0);
        GuiMessage previous = allMessages.get(1);

        if (newest.source() != previous.source()) return;
        if (!Objects.equals(newest.tag(), previous.tag())) return;
        if (!newest.content().getString().equals(key)) return;
        if (!sbutils$stripDuplicateCounter(previous.content().getString()).equals(key)) return;

        int count = sbutils$duplicateCount + 1;
        MutableComponent compacted = newest.content().copy().append(Component.literal(" (x" + count + ")")
                .withStyle(style -> style.withColor(ClickGuiTheme.compactCounterColor())));

        GuiMessage merged = new GuiMessage(
                newest.addedTime(),
                compacted,
                newest.signature(),
                newest.source(),
                newest.tag()
        );

        allMessages.set(0, merged);
        allMessages.remove(1);
        accessor.invokeRefreshTrimmedMessages();

        sbutils$duplicateCount = count;
    }

    @Unique
    private static String sbutils$stripDuplicateCounter(String text) {
        return text.replaceFirst("\\s*(?:×\\d+|\\(x\\d+\\))$", "");
    }

    @Unique
    private static void sbutils$resetCounter() {
        sbutils$lastCompactKey = null;
        sbutils$lastCompactSource = null;
        sbutils$duplicateCount = 1;
    }
}
