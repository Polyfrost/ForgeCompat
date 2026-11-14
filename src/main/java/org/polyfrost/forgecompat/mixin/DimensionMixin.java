package org.polyfrost.forgecompat.mixin;

import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Dimension.class)
public class DimensionMixin {
    public String getSaveFolder() {
        int id = ((Dimension) (Object) this).getId();
        return id == 0 ? null : "DIM" + id;
    }
}
