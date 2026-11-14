package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;

public class EntityItemPickupEvent extends PlayerEvent {
    public final net.minecraft.entity.ItemEntity item;

    public EntityItemPickupEvent(PlayerEntity player, net.minecraft.entity.ItemEntity item) {
        super(player);
        this.item = item;
    }
}
