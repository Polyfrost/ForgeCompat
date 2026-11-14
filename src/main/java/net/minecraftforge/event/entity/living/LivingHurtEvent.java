package net.minecraftforge.event.entity.living;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class LivingHurtEvent extends LivingEvent {
    public final DamageSource source;
    public float ammount;

    public LivingHurtEvent(LivingEntity entity, DamageSource source, float ammount) {
        super(entity);
        this.source = source;
        this.ammount = ammount;
    }
}
