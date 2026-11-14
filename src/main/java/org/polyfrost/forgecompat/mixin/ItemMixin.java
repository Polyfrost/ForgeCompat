package org.polyfrost.forgecompat.mixin;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin {
    public boolean onItemUseFirst(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public boolean doesSneakBypassUse(World world, BlockPos pos, PlayerEntity player) {
        return false;
    }

    public CreativeModeTab[] getCreativeTabs() {
        return new CreativeModeTab[]{((Item) (Object) this).getCreativeModeTab()};
    }

    public TextRenderer getFontRenderer(ItemStack stack) {
        return null;
    }

    public void setDamage(ItemStack stack, int damage) {
        stack.setDamage(damage);
    }

    public int getDamage(ItemStack stack) {
        return stack.getMetadata();
    }

    public boolean hasContainerItem(ItemStack stack) {
        return ((Item) (Object) this).hasRecipeRemainder();
    }

    public ItemStack getContainerItem(ItemStack stack) {
        Item remainder = ((Item) (Object) this).getRecipeRemainder();
        return remainder == null ? null : new ItemStack(remainder);
    }
}
