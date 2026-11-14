package net.minecraftforge.fml.common.gameevent;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerEvent extends Event {
    public final PlayerEntity player;

    public PlayerEvent(PlayerEntity player) {
        this.player = player;
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(PlayerEntity player) {
            super(player);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(PlayerEntity player) {
            super(player);
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {
        public PlayerRespawnEvent(PlayerEntity player) {
            super(player);
        }
    }

    public static class PlayerChangedDimensionEvent extends PlayerEvent {
        public final int fromDim;
        public final int toDim;

        public PlayerChangedDimensionEvent(PlayerEntity player, int fromDim, int toDim) {
            super(player);
            this.fromDim = fromDim;
            this.toDim = toDim;
        }
    }
}
