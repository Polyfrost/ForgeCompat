package net.minecraftforge.event.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class PlayerEvent extends LivingEvent {
    public final PlayerEntity entityPlayer;

    public PlayerEvent(PlayerEntity player) {
        super(player);
        this.entityPlayer = player;
    }

    public static class StartTracking extends PlayerEvent {
        public final Entity target;

        public StartTracking(PlayerEntity player, Entity target) {
            super(player);
            this.target = target;
        }
    }

    public static class StopTracking extends PlayerEvent {
        public final Entity target;

        public StopTracking(PlayerEntity player, Entity target) {
            super(player);
            this.target = target;
        }
    }

    public static class Clone extends PlayerEvent {
        public final PlayerEntity original;
        public final boolean wasDeath;

        public Clone(PlayerEntity player, PlayerEntity original, boolean wasDeath) {
            super(player);
            this.original = original;
            this.wasDeath = wasDeath;
        }
    }

    public static class NameFormat extends PlayerEvent {
        public final String username;
        public String displayname;

        public NameFormat(PlayerEntity player, String username) {
            super(player);
            this.username = username;
            this.displayname = username;
        }
    }
}
