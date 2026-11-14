package net.minecraftforge.client.event;

import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderBlockOverlayEvent extends Event {
    public final PlayerEntity player;
    public final float renderPartialTicks;
    public final OverlayType overlayType;
    public final BlockState blockForOverlay;
    public final BlockPos blockPos;

    public RenderBlockOverlayEvent(PlayerEntity player, float renderPartialTicks, OverlayType type, BlockState block, BlockPos blockPos) {
        this.player = player;
        this.renderPartialTicks = renderPartialTicks;
        this.overlayType = type;
        this.blockForOverlay = block;
        this.blockPos = blockPos;
    }

    public enum OverlayType {
        FIRE, BLOCK, WATER
    }
}
