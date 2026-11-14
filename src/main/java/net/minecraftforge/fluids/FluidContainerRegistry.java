package net.minecraftforge.fluids;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class FluidContainerRegistry {
    public static final int BUCKET_VOLUME = 1000;

    public static FluidStack getFluidForFilledItem(ItemStack container) {
        if (container == null) return null;
        if (container.getItem() == Items.WATER_BUCKET) return new FluidStack(FluidRegistry.WATER, BUCKET_VOLUME);
        if (container.getItem() == Items.LAVA_BUCKET) return new FluidStack(FluidRegistry.LAVA, BUCKET_VOLUME);
        return null;
    }

    public static boolean isFilledContainer(ItemStack container) {
        return getFluidForFilledItem(container) != null;
    }

    public static boolean isEmptyContainer(ItemStack container) {
        return container != null && container.getItem() == Items.BUCKET;
    }

    public static boolean isContainer(ItemStack container) {
        return isEmptyContainer(container) || isFilledContainer(container);
    }
}
