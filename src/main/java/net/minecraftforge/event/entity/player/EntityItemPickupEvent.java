package net.minecraftforge.event.entity.player;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.living.player.PlayerEntity;

public class EntityItemPickupEvent extends PlayerEvent {
    public final ItemEntity item;

    public EntityItemPickupEvent(PlayerEntity player, ItemEntity item) {
        super(player);
        this.item = item;
    }
}
