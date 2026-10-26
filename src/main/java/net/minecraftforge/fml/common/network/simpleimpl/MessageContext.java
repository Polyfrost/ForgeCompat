package net.minecraftforge.fml.common.network.simpleimpl;

import net.minecraft.network.handler.PacketHandler;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;

public class MessageContext {
    public final PacketHandler netHandler;
    public final Side side;

    public MessageContext(PacketHandler netHandler, Side side) {
        this.netHandler = netHandler;
        this.side = side;
    }

    public ServerPlayNetworkHandler getServerHandler() {
        return (ServerPlayNetworkHandler) netHandler;
    }
}
