package net.minecraftforge.client.event;

import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraftforge.fml.common.eventhandler.Event;

public class TextureStitchEvent extends Event {
    public final TextureAtlas map;

    public TextureStitchEvent(TextureAtlas map) {
        this.map = map;
    }

    public static class Pre extends TextureStitchEvent {
        public Pre(TextureAtlas map) {
            super(map);
        }
    }

    public static class Post extends TextureStitchEvent {
        public Post(TextureAtlas map) {
            super(map);
        }
    }
}
