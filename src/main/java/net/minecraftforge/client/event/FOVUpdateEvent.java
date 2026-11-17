package net.minecraftforge.client.event;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class FOVUpdateEvent extends Event {
    public final PlayerEntity entity;
    public final float fov;
    public float newfov;

    public FOVUpdateEvent(PlayerEntity entity, float fov) {
        this.entity = entity;
        this.fov = fov;
        this.newfov = fov;
    }
}
