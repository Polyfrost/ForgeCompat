package net.minecraftforge.event.entity.living;

import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public class LivingEvent extends EntityEvent {
    public final LivingEntity entityLiving;

    public LivingEvent(LivingEntity entity) {
        super(entity);
        this.entityLiving = entity;
    }

    @Cancelable
    public static class LivingUpdateEvent extends LivingEvent {
        public LivingUpdateEvent(LivingEntity entity) {
            super(entity);
        }
    }

    public static class LivingJumpEvent extends LivingEvent {
        public LivingJumpEvent(LivingEntity entity) {
            super(entity);
        }
    }
}
