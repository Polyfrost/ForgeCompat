package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;

public class PlayerDestroyItemEvent extends PlayerEvent {
    public final net.minecraft.item.ItemStack original;

    public PlayerDestroyItemEvent(PlayerEntity player, net.minecraft.item.ItemStack original) {
        super(player);
        this.original = original;
    }
}
