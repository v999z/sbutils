package com.v999.sbutils.client.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface AccessChatComponent {
    @Accessor("allMessages")
    List<GuiMessage> getAllMessages();

    @Invoker("refreshTrimmedMessages")
    void invokeRefreshTrimmedMessages();
}