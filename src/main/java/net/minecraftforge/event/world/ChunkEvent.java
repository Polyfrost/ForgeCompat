package net.minecraftforge.event.world;

import net.minecraft.world.chunk.WorldChunk;

public class ChunkEvent extends WorldEvent {
    private final WorldChunk chunk;

    public ChunkEvent(WorldChunk chunk) {
        super(chunk.getWorld());
        this.chunk = chunk;
    }

    public WorldChunk getChunk() {
        return chunk;
    }

    public static class Load extends ChunkEvent {
        public Load(WorldChunk chunk) {
            super(chunk);
        }
    }

    public static class Unload extends ChunkEvent {
        public Unload(WorldChunk chunk) {
            super(chunk);
        }
    }
}
