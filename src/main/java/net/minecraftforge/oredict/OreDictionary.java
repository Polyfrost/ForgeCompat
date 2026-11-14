package net.minecraftforge.oredict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class OreDictionary {
    public static final int WILDCARD_VALUE = Short.MAX_VALUE;
    private static final List<String> NAMES = new ArrayList<>();
    private static final Map<String, Integer> IDS = new HashMap<>();
    private static final List<List<ItemStack>> ORES = new ArrayList<>();

    public static int getOreID(String name) {
        return IDS.computeIfAbsent(name, n -> {
            NAMES.add(n);
            ORES.add(new ArrayList<>());
            return NAMES.size() - 1;
        });
    }

    public static String getOreName(int id) {
        return id >= 0 && id < NAMES.size() ? NAMES.get(id) : "Unknown";
    }

    public static String[] getOreNames() {
        return NAMES.toArray(new String[0]);
    }

    public static boolean doesOreNameExist(String name) {
        return IDS.containsKey(name);
    }

    public static List<ItemStack> getOres(String name) {
        return ORES.get(getOreID(name));
    }

    public static List<ItemStack> getOres(String name, boolean alwaysCreateEntry) {
        return alwaysCreateEntry || doesOreNameExist(name) ? getOres(name) : new ArrayList<>();
    }

    public static int[] getOreIDs(ItemStack stack) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < ORES.size(); i++) for (ItemStack ore : ORES.get(i)) if (itemMatches(ore, stack, false)) {
            ids.add(i);
            break;
        }
        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void registerOre(String name, Item item) {
        registerOre(name, new ItemStack(item));
    }

    public static void registerOre(String name, ItemStack stack) {
        getOres(name).add(stack);
    }

    public static boolean itemMatches(ItemStack target, ItemStack input, boolean strict) {
        if (input == null && target != null || input != null && target == null) return false;
        if (input == null) return true;
        return target.getItem() == input.getItem() && (target.getMetadata() == WILDCARD_VALUE && !strict || target.getMetadata() == input.getMetadata());
    }
}
