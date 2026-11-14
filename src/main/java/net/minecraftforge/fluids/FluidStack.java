package net.minecraftforge.fluids;

import net.minecraft.nbt.NbtCompound;

public class FluidStack {
    public int amount;
    public NbtCompound tag;
    private final Fluid fluid;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public FluidStack(Fluid fluid, int amount, NbtCompound nbt) {
        this(fluid, amount);
        this.tag = nbt;
    }

    public final Fluid getFluid() {
        return fluid;
    }

    public String getLocalizedName() {
        return fluid.getLocalizedName(this);
    }

    public String getUnlocalizedName() {
        return fluid.getUnlocalizedName();
    }

    public FluidStack copy() {
        return new FluidStack(fluid, amount, tag);
    }

    public boolean isFluidEqual(FluidStack other) {
        return other != null && fluid == other.fluid;
    }
}
