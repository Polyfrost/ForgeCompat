package net.minecraftforge.event.terraingen;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSource;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PopulateChunkEvent extends Event {
    public final ChunkSource chunkProvider;
    public final World world;
    public final Random rand;
    public final int chunkX, chunkZ;
    public final boolean hasVillageGenerated;

    public PopulateChunkEvent(ChunkSource chunkProvider, World world, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated) {
        this.chunkProvider = chunkProvider;
        this.world = world;
        this.rand = rand;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.hasVillageGenerated = hasVillageGenerated;
    }

    public static class Pre extends PopulateChunkEvent {
        public Pre(ChunkSource chunkProvider, World world, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated) {
            super(chunkProvider, world, rand, chunkX, chunkZ, hasVillageGenerated);
        }
    }

    public static class Post extends PopulateChunkEvent {
        public Post(ChunkSource chunkProvider, World world, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated) {
            super(chunkProvider, world, rand, chunkX, chunkZ, hasVillageGenerated);
        }
    }
}
