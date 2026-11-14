package net.minecraftforge.client.event;

import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.HitResult;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class DrawBlockHighlightEvent extends Event {
    public final WorldRenderer context;
    public final PlayerEntity player;
    public final HitResult target;
    public final int subID;
    public final ItemStack currentItem;
    public final float partialTicks;

    public DrawBlockHighlightEvent(WorldRenderer context, PlayerEntity player, HitResult target, int subID, ItemStack currentItem, float partialTicks) {
        this.context = context;
        this.player = player;
        this.target = target;
        this.subID = subID;
        this.currentItem = currentItem;
        this.partialTicks = partialTicks;
    }
}
