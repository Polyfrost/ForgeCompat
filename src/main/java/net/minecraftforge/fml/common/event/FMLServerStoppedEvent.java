package net.minecraftforge.fml.common.event;

import net.minecraft.server.MinecraftServer;

public class FMLServerStoppedEvent extends FMLServerEvent {
    public FMLServerStoppedEvent(MinecraftServer server) {
        super(server);
    }
}
