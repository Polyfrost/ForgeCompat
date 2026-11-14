package net.minecraftforge.event.entity.player;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@net.minecraftforge.fml.common.eventhandler.Cancelable
public class PlayerInteractEvent extends PlayerEvent {
    public final Action action;
    public final World world;
    public final BlockPos pos;
    public final Direction face;
    public net.minecraftforge.fml.common.eventhandler.Event.Result useBlock = net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT;
    public net.minecraftforge.fml.common.eventhandler.Event.Result useItem = net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT;

    public PlayerInteractEvent(PlayerEntity player, Action action, BlockPos pos, Direction face, World world) {
        super(player);
        this.action = action;
        this.pos = pos;
        this.face = face;
        this.world = world;
    }

    public enum Action {
        RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK, LEFT_CLICK_BLOCK
    }
}
