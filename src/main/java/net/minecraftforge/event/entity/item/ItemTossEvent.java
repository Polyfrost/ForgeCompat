package net.minecraftforge.event.entity.item;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ItemTossEvent extends ItemEvent {
    public final PlayerEntity player;

    public ItemTossEvent(ItemEntity entityItem, PlayerEntity player) {
        super(entityItem);
        this.player = player;
    }
}
