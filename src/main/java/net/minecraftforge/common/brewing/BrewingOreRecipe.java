package net.minecraftforge.common.brewing;

import java.util.List;

import net.minecraft.item.ItemStack;

public class BrewingOreRecipe implements IBrewingRecipe {
    public final ItemStack input;
    public final Object ingredient;
    public final ItemStack output;

    public BrewingOreRecipe(ItemStack input, List<ItemStack> ingredient, ItemStack output) {
        this.input = input;
        this.ingredient = ingredient;
        this.output = output;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return stack != null && input.getItem() == stack.getItem() && input.getMetadata() == stack.getMetadata();
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack != null && ((List<?>) ingredient).stream().anyMatch(o -> ((ItemStack) o).getItem() == stack.getItem());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return isInput(input) && isIngredient(ingredient) ? output.copy() : null;
    }
}
