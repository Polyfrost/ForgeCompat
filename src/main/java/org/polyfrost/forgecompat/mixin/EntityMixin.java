package org.polyfrost.forgecompat.mixin;

import net.minecraft.entity.Entity;
import org.polyfrost.forgecompat.RavenHooks;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyVariable(method = "move(DDD)V", at = @At("STORE"), ordinal = 0)
    private boolean forgecompat$ravenSafeWalk(boolean sneakingOnEdge) {
        return RavenHooks.active() ? RavenHooks.safeWalk((Entity) (Object) this) : sneakingOnEdge;
    }

    @Unique
    private NbtCompound forgecompat$entityData;

    public NbtCompound getEntityData() {
        if (forgecompat$entityData == null) forgecompat$entityData = new NbtCompound();
        return forgecompat$entityData;
    }

    public NbtCompound serializeNBT() {
        NbtCompound nbt = new NbtCompound();
        ((Entity) (Object) this).writeNbt(nbt);
        return nbt;
    }

    public boolean shouldRiderSit() {
        return true;
    }

    public java.util.UUID getPersistentID() {
        return ((Entity) (Object) this).getUuid();
    }
}
