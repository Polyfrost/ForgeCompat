package net.minecraftforge.fml.common.registry;

import net.minecraft.block.Block;
import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import org.apache.logging.log4j.LogManager;

public class GameRegistry {
    public static Item findItem(String modId, String name) {
        return Item.REGISTRY.get(new Identifier(modId, name));
    }

    public static Block findBlock(String modId, String name) {
        Identifier id = new Identifier(modId, name);
        return Block.REGISTRY.containsKey(id) ? Block.REGISTRY.get(id) : null;
    }

    public static ItemStack findItemStack(String modId, String name, int stackSize) {
        Item item = findItem(modId, name);
        return item == null ? null : new ItemStack(item, stackSize, 0);
    }

    public static UniqueIdentifier findUniqueIdentifierFor(Block block) {
        Identifier id = Block.REGISTRY.getKey(block);
        return id == null ? null : new UniqueIdentifier(id.toString());
    }

    public static UniqueIdentifier findUniqueIdentifierFor(Item item) {
        Identifier id = Item.REGISTRY.getKey(item);
        return id == null ? null : new UniqueIdentifier(id.toString());
    }

    public static void registerItem(Item item, String name) {
        LogManager.getLogger("ForgeCompat").warn("Ignoring Forge item registration {}: content mods are not supported", name);
    }

    public static Block registerBlock(Block block, String name) {
        LogManager.getLogger("ForgeCompat").warn("Ignoring Forge block registration {}: content mods are not supported", name);
        return block;
    }

    public static void addRecipe(Recipe recipe) {
    }

    public static void registerTileEntity(Class<?> type, String id) {
    }

    public static void registerWorldGenerator(Object generator, int weight) {
    }

    public static class UniqueIdentifier {
        public final String modId;
        public final String name;

        public UniqueIdentifier(String id) {
            int colon = id.indexOf(':');
            this.modId = colon < 0 ? "minecraft" : id.substring(0, colon);
            this.name = colon < 0 ? id : id.substring(colon + 1);
        }

        @Override
        public String toString() {
            return modId + ":" + name;
        }
    }
}
