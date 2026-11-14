package net.minecraftforge.common.property;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.BlockState;

public interface IExtendedBlockState extends BlockState {
    ImmutableMap<IUnlistedProperty<?>, com.google.common.base.Optional<?>> getUnlistedProperties();

    BlockState getClean();
}
