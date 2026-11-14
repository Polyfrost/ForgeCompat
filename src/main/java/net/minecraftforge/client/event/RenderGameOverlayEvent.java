package net.minecraftforge.client.event;

import net.minecraft.client.render.Window;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderGameOverlayEvent extends Event {
    public final float partialTicks;
    public final Window resolution;
    public final ElementType type;

    public RenderGameOverlayEvent(float partialTicks, Window resolution, ElementType type) {
        this.partialTicks = partialTicks;
        this.resolution = resolution;
        this.type = type;
    }

    public enum ElementType {
        ALL, HELMET, PORTAL, CROSSHAIRS, BOSSHEALTH, ARMOR, HEALTH, FOOD, AIR, HOTBAR, EXPERIENCE, TEXT, HEALTHMOUNT, JUMPBAR, CHAT, PLAYER_LIST, DEBUG
    }

    @Cancelable
    public static class Pre extends RenderGameOverlayEvent {
        public Pre(float partialTicks, Window resolution, ElementType type) {
            super(partialTicks, resolution, type);
        }
    }

    public static class Post extends RenderGameOverlayEvent {
        public Post(float partialTicks, Window resolution, ElementType type) {
            super(partialTicks, resolution, type);
        }
    }

    public static class Text extends Pre {
        public final java.util.ArrayList<String> left;
        public final java.util.ArrayList<String> right;

        public Text(float partialTicks, Window resolution, java.util.ArrayList<String> left, java.util.ArrayList<String> right) {
            super(partialTicks, resolution, ElementType.TEXT);
            this.left = left;
            this.right = right;
        }
    }

    public static class Chat extends Pre {
        public int posX;
        public int posY;

        public Chat(float partialTicks, Window resolution, int posX, int posY) {
            super(partialTicks, resolution, ElementType.CHAT);
            this.posX = posX;
            this.posY = posY;
        }
    }
}
