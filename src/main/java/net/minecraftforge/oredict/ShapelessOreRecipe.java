package net.minecraftforge.oredict;

import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ShapelessOreRecipe implements Recipe {
    protected ItemStack output;
    protected java.util.ArrayList<Object> input;

    public ShapelessOreRecipe(ItemStack output, java.util.ArrayList<Object> input) {
        this.output = output;
        this.input = input;
    }

    public java.util.ArrayList<Object> getInput() {
        return input;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        return false;
    }

    @Override
    public ItemStack getResult(CraftingInventory inventory) {
        return output == null ? null : output.copy();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ItemStack getResult() {
        return output;
    }

    @Override
    public ItemStack[] getRemainder(CraftingInventory inventory) {
        return new ItemStack[inventory.getSize()];
    }
}
