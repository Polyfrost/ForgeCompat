package net.minecraftforge.fml.common.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.handler.CommandRegistry;

public class FMLServerStartingEvent extends FMLServerEvent {
    public FMLServerStartingEvent(MinecraftServer server) {
        super(server);
    }

    public void registerServerCommand(Command command) {
        ((CommandRegistry) getServer().getCommandHandler()).register(command);
    }
}
