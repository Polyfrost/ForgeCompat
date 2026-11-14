package net.minecraftforge.fml.common.network;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;

public interface IGuiHandler {
    Object getServerGuiElement(int id, PlayerEntity player, World world, int x, int y, int z);

    Object getClientGuiElement(int id, PlayerEntity player, World world, int x, int y, int z);
}
