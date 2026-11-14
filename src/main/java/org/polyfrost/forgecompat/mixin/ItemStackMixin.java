package org.polyfrost.forgecompat.mixin;

import java.util.List;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    public NbtCompound serializeNBT() {
        return ((ItemStack) (Object) this).writeNbt(new NbtCompound());
    }

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void forgecompat$tooltip(PlayerEntity player, boolean advanced, CallbackInfoReturnable<List<String>> cir) {
        MinecraftForge.EVENT_BUS.post(new ItemTooltipEvent(player, (ItemStack) (Object) this, cir.getReturnValue(), advanced));
    }
}
