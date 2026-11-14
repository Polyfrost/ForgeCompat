package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;

public class ItemTooltipEvent extends PlayerEvent {
    public final net.minecraft.item.ItemStack itemStack;
    public final java.util.List<String> toolTip;
    public final boolean showAdvancedItemTooltips;

    public ItemTooltipEvent(PlayerEntity player, net.minecraft.item.ItemStack itemStack, java.util.List<String> toolTip, boolean showAdvancedItemTooltips) {
        super(player);
        this.itemStack = itemStack;
        this.toolTip = toolTip;
        this.showAdvancedItemTooltips = showAdvancedItemTooltips;
    }
}
