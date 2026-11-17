package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemTooltipEvent extends PlayerEvent {
    public final ItemStack itemStack;
    public final List<String> toolTip;
    public final boolean showAdvancedItemTooltips;

    public ItemTooltipEvent(ItemStack itemStack, PlayerEntity player, List<String> toolTip, boolean showAdvancedItemTooltips) {
        this(player, itemStack, toolTip, showAdvancedItemTooltips);
    }

    public ItemTooltipEvent(PlayerEntity player, ItemStack itemStack, List<String> toolTip, boolean showAdvancedItemTooltips) {
        super(player);
        this.itemStack = itemStack;
        this.toolTip = toolTip;
        this.showAdvancedItemTooltips = showAdvancedItemTooltips;
    }
}
