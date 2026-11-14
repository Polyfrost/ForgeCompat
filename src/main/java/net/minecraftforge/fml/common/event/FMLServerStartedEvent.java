package net.minecraftforge.fml.common.event;

import net.minecraft.server.MinecraftServer;

public class FMLServerStartedEvent extends FMLServerEvent {
    public FMLServerStartedEvent(MinecraftServer server) {
        super(server);
    }
}
