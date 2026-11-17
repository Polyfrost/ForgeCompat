package org.polyfrost.forgecompat.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

import java.io.File;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    public File getChunkSaveLocation() {
        ServerWorld world = (ServerWorld) (Object) this;
        String folder = world.dimension.getId() == 0 ? null : "DIM" + world.dimension.getId();
        File dir = world.getStorage().getDirectory();
        return folder == null ? dir : new File(dir, folder);
    }
}
