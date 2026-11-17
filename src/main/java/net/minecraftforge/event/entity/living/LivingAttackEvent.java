package net.minecraftforge.event.entity.living;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class LivingAttackEvent extends LivingEvent {
    public final DamageSource source;
    public final float ammount;

    public LivingAttackEvent(LivingEntity entity, DamageSource source, float ammount) {
        super(entity);
        this.source = source;
        this.ammount = ammount;
    }
}
