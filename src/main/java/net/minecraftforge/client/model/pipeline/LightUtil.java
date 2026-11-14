package net.minecraftforge.client.model.pipeline;

import net.minecraft.client.render.model.block.BakedQuad;
import net.minecraft.client.render.vertex.BufferBuilder;

public class LightUtil {
    public static void renderQuadColor(BufferBuilder buffer, BakedQuad quad, int color) {
        buffer.vertices(quad.getVertices());
        buffer.setQuadColor(color);
    }
}
