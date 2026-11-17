package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class PlayerSetSpawnEvent extends PlayerEvent {
    public final BlockPos newSpawn;
    public final boolean forced;

    public PlayerSetSpawnEvent(PlayerEntity player, BlockPos newSpawn, boolean forced) {
        super(player);
        this.newSpawn = newSpawn;
        this.forced = forced;
    }
}
