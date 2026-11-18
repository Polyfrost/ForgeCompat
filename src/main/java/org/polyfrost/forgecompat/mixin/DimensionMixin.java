package org.polyfrost.forgecompat.mixin;

import net.minecraft.world.dimension.Dimension;
import net.minecraftforge.client.IRenderHandler;
import org.polyfrost.forgecompat.ForgeDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Dimension.class)
public class DimensionMixin implements ForgeDimension {
    @Unique
    private IRenderHandler forgecompat$skyRenderer;
    @Unique
    private IRenderHandler forgecompat$cloudRenderer;
    @Unique
    private IRenderHandler forgecompat$weatherRenderer;

    public String getSaveFolder() {
        int id = ((Dimension) (Object) this).getId();
        return id == 0 ? null : "DIM" + id;
    }

    @Override
    public IRenderHandler getSkyRenderer() {
        return forgecompat$skyRenderer;
    }

    public void setSkyRenderer(IRenderHandler renderer) {
        forgecompat$skyRenderer = renderer;
    }

    @Override
    public IRenderHandler getCloudRenderer() {
        return forgecompat$cloudRenderer;
    }

    public void setCloudRenderer(IRenderHandler renderer) {
        forgecompat$cloudRenderer = renderer;
    }

    @Override
    public IRenderHandler getWeatherRenderer() {
        return forgecompat$weatherRenderer;
    }

    public void setWeatherRenderer(IRenderHandler renderer) {
        forgecompat$weatherRenderer = renderer;
    }
}
