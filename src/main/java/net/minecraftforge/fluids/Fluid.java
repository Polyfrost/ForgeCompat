package net.minecraftforge.fluids;

import net.minecraft.block.Block;
import net.minecraft.locale.I18n;
import net.minecraft.resource.Identifier;

public class Fluid {
    protected final String fluidName;
    protected final Identifier still;
    protected final Identifier flowing;
    protected String unlocalizedName;
    protected Block block;
    protected int color = 0xFFFFFFFF;

    public Fluid(String fluidName, Identifier still, Identifier flowing) {
        this.fluidName = fluidName.toLowerCase(java.util.Locale.ENGLISH);
        this.unlocalizedName = fluidName;
        this.still = still;
        this.flowing = flowing;
    }

    public Fluid setBlock(Block block) {
        this.block = block;
        return this;
    }

    public Fluid setUnlocalizedName(String name) {
        this.unlocalizedName = name;
        return this;
    }

    public final String getName() {
        return fluidName;
    }

    public final Block getBlock() {
        return block;
    }

    public String getLocalizedName(FluidStack stack) {
        String key = getUnlocalizedName();
        return key == null ? "" : I18n.translate(key);
    }

    public String getUnlocalizedName() {
        return "fluid." + unlocalizedName;
    }

    public Identifier getStill() {
        return still;
    }

    public Identifier getFlowing() {
        return flowing;
    }

    public int getColor() {
        return color;
    }

    public int getColor(FluidStack stack) {
        return getColor();
    }

    public Identifier getStill(FluidStack stack) {
        return getStill();
    }
}
