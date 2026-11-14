package net.minecraftforge.fml.common.event;

import net.minecraft.server.MinecraftServer;

public class FMLServerStoppingEvent extends FMLServerEvent {
    public FMLServerStoppingEvent(MinecraftServer server) {
        super(server);
    }
}
