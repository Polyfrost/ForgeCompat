package net.minecraftforge.common.brewing;

import net.minecraft.item.ItemStack;

public interface IBrewingRecipe {
    boolean isInput(ItemStack input);

    boolean isIngredient(ItemStack ingredient);

    ItemStack getOutput(ItemStack input, ItemStack ingredient);
}
