package org.polyfrost.forgecompat.mixin;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Biome.class)
public class BiomeMixin {
    public int getWaterColorMultiplier() {
        return ((Biome) (Object) this).waterFogColor;
    }
}
