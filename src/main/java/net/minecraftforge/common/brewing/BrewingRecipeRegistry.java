package net.minecraftforge.common.brewing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

public class BrewingRecipeRegistry {
    private static final List<IBrewingRecipe> RECIPES = new ArrayList<>(List.of(new VanillaBrewingRecipe()));

    public static boolean addRecipe(IBrewingRecipe recipe) {
        return RECIPES.add(recipe);
    }

    public static List<IBrewingRecipe> getRecipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        for (IBrewingRecipe recipe : RECIPES) {
            ItemStack output = recipe.getOutput(input, ingredient);
            if (output != null) return output;
        }
        return null;
    }
}
