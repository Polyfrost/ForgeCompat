package net.minecraftforge.event.entity.item;

import net.minecraft.entity.ItemEntity;
import net.minecraftforge.event.entity.EntityEvent;

public class ItemEvent extends EntityEvent {
    public final ItemEntity entityItem;

    public ItemEvent(ItemEntity entityItem) {
        super(entityItem);
        this.entityItem = entityItem;
    }
}
