package net.minecraftforge.common.property;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.BlockState;

public interface IExtendedBlockState extends BlockState {
    ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties();

    BlockState getClean();
}
