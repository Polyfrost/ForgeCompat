package net.minecraftforge.client.event;

import net.minecraft.block.Block;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class EntityViewRenderEvent extends Event {
    public final GameRenderer renderer;
    public final Entity entity;
    public final Block block;
    public final double renderPartialTicks;

    public EntityViewRenderEvent(GameRenderer renderer, Entity entity, Block block, double renderPartialTicks) {
        this.renderer = renderer;
        this.entity = entity;
        this.block = block;
        this.renderPartialTicks = renderPartialTicks;
    }

    public static class CameraSetup extends EntityViewRenderEvent {
        public float yaw;
        public float pitch;
        public float roll;

        public CameraSetup(GameRenderer renderer, Entity entity, Block block, double renderPartialTicks, float yaw, float pitch, float roll) {
            super(renderer, entity, block, renderPartialTicks);
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }
    }

    public static class FOVModifier extends EntityViewRenderEvent {
        private float fov;

        public FOVModifier(GameRenderer renderer, Entity entity, Block block, double renderPartialTicks, float fov) {
            super(renderer, entity, block, renderPartialTicks);
            this.fov = fov;
        }

        public float getFOV() {
            return fov;
        }

        public void setFOV(float fov) {
            this.fov = fov;
        }
    }
}
