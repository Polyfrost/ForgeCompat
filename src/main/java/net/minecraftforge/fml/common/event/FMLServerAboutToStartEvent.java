package net.minecraftforge.fml.common.event;

import net.minecraft.server.MinecraftServer;

public class FMLServerAboutToStartEvent extends FMLServerEvent {
    public FMLServerAboutToStartEvent(MinecraftServer server) {
        super(server);
    }
}
