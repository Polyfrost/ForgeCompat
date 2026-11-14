package net.minecraftforge.common.brewing;

import net.minecraft.entity.living.effect.PotionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;

public class VanillaBrewingRecipe implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        return stack != null && stack.getItem() instanceof PotionItem;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack != null && stack.getItem().isPotionIngredient(stack);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (input == null || ingredient == null || !isIngredient(ingredient) || !isInput(input)) return null;
        ItemStack result = input.copy();
        int meta = PotionHelper.applyIngredient(input.getMetadata(), ingredient.getItem().asPotionIngredient(ingredient));
        if (meta == input.getMetadata()) return null;
        result.setDamage(meta);
        return result;
    }
}
