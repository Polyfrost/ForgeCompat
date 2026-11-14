package net.minecraftforge.fml.common.event;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.Side;

public class FMLServerEvent extends FMLStateEvent {
    private final MinecraftServer server;

    public FMLServerEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
