package net.minecraftforge.fluids;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.resource.Identifier;

public class FluidRegistry {
    public static final Fluid WATER = new Fluid("water", new Identifier("blocks/water_still"), new Identifier("blocks/water_flow")).setBlock(Blocks.WATER).setUnlocalizedName(Blocks.WATER.getTranslationKey());
    public static final Fluid LAVA = new Fluid("lava", new Identifier("blocks/lava_still"), new Identifier("blocks/lava_flow")).setBlock(Blocks.LAVA).setUnlocalizedName(Blocks.LAVA.getTranslationKey());
    private static final Map<String, Fluid> FLUIDS = new HashMap<>(Map.of("water", WATER, "lava", LAVA));

    public static boolean registerFluid(Fluid fluid) {
        return FLUIDS.putIfAbsent(fluid.getName(), fluid) == null;
    }

    public static Fluid getFluid(String name) {
        return FLUIDS.get(name);
    }

    public static boolean isFluidRegistered(String name) {
        return FLUIDS.containsKey(name);
    }

    public static Map<String, Fluid> getRegisteredFluids() {
        return FLUIDS;
    }

    public static FluidStack getFluidStack(String name, int amount) {
        Fluid fluid = FLUIDS.get(name);
        return fluid == null ? null : new FluidStack(fluid, amount);
    }

    public static Fluid lookupFluidForBlock(Block block) {
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) return WATER;
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) return LAVA;
        for (Fluid fluid : FLUIDS.values()) if (fluid.getBlock() == block) return fluid;
        return null;
    }
}
