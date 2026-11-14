package org.polyfrost.forgecompat.mixin;

import net.minecraft.inventory.slot.InventorySlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InventorySlot.class)
public class InventorySlotMixin {
    @Shadow
    @Final
    private int id;

    public int getSlotIndex() {
        return id;
    }
}
