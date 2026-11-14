package net.minecraftforge.items;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.slot.InventorySlot;

public class SlotItemHandler extends InventorySlot {
    public final IItemHandler itemHandler;

    public SlotItemHandler(IItemHandler itemHandler, int index, int x, int y) {
        super(new SimpleInventory("", false, 0), index, x, y);
        this.itemHandler = itemHandler;
    }
}
