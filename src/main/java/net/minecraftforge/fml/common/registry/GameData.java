package net.minecraftforge.fml.common.registry;

import net.minecraft.block.Block;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.util.registry.IdRegistry;
import net.minecraft.item.Item;

public class GameData {
    private static FMLControlledNamespacedRegistry<Block> blocks;
    private static FMLControlledNamespacedRegistry<Item> items;
    private static FMLControlledNamespacedRegistry<StatusEffect> potions;

    public static FMLControlledNamespacedRegistry<Block> getBlockRegistry() {
        if (blocks == null) blocks = new FMLControlledNamespacedRegistry<>(Block.REGISTRY);
        return blocks;
    }

    public static FMLControlledNamespacedRegistry<Item> getItemRegistry() {
        if (items == null) items = new FMLControlledNamespacedRegistry<>(Item.REGISTRY);
        return items;
    }

    public static FMLControlledNamespacedRegistry<StatusEffect> getPotionRegistry() {
        if (potions == null) {
            IdRegistry<net.minecraft.resource.Identifier, StatusEffect> registry = new IdRegistry<>();
            for (net.minecraft.resource.Identifier key : StatusEffect.getKeys()) {
                StatusEffect effect = StatusEffect.get(key.toString());
                if (effect != null) registry.register(effect.id, key, effect);
            }
            potions = new FMLControlledNamespacedRegistry<>(registry);
        }
        return potions;
    }
}
