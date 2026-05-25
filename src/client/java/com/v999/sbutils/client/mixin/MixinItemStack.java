package com.v999.sbutils.client.mixin;

import com.v999.sbutils.client.interfaces.AccessItemStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements AccessItemStack {
    @Shadow
    public abstract Item getItem();

    @Unique
    private boolean sbutils$isShortbow = false;

    @Unique
    private boolean sbutils$hasPickobulusAbility = false;

    @Override
    public boolean sbutils$isShortbow() {
        return sbutils$isShortbow;
    }

    @Override
    public boolean sbutils$hasPickobulusAbility() {
        return sbutils$hasPickobulusAbility;
    }

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void sbutils$readItemComponents(Holder<Item> item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        List<Component> lines = components.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines();
        if (this.getItem() == Items.BOW) {
            this.sbutils$isShortbow = lines.stream().anyMatch(line -> line.getString().contains("Shortbow: Instantly shoots!"));
        } else {
            this.sbutils$hasPickobulusAbility = lines.stream().anyMatch(line -> line.getString().startsWith("Ability: Pickobulus"));
        }
    }
}
