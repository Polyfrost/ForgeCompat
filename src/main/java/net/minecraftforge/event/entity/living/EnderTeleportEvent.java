package net.minecraftforge.event.entity.living;

import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class EnderTeleportEvent extends LivingEvent {
    public double targetX, targetY, targetZ;
    public float attackDamage;

    public EnderTeleportEvent(LivingEntity entity, double targetX, double targetY, double targetZ, float attackDamage) {
        super(entity);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.attackDamage = attackDamage;
    }
}
