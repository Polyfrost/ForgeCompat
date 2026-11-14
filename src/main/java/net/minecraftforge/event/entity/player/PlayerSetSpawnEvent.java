package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;

public class PlayerSetSpawnEvent extends PlayerEvent {
    public final net.minecraft.util.math.BlockPos newSpawn;
    public final boolean forced;

    public PlayerSetSpawnEvent(PlayerEntity player, net.minecraft.util.math.BlockPos newSpawn, boolean forced) {
        super(player);
        this.newSpawn = newSpawn;
        this.forced = forced;
    }
}
