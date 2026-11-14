package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;

public class EntityInteractEvent extends PlayerEvent {
    public final net.minecraft.entity.Entity target;

    public EntityInteractEvent(PlayerEntity player, net.minecraft.entity.Entity target) {
        super(player);
        this.target = target;
    }
}
