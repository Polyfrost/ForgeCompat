package net.minecraftforge.event.entity.living;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class LivingDeathEvent extends LivingEvent {
    public final DamageSource source;

    public LivingDeathEvent(LivingEntity entity, DamageSource source) {
        super(entity);
        this.source = source;
    }
}
