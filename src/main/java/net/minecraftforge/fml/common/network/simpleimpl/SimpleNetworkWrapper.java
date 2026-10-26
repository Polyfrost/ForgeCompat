package net.minecraftforge.fml.common.network.simpleimpl;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraftforge.fml.relauncher.Side;

public class SimpleNetworkWrapper {
    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> handler, Class<REQ> type, int discriminator, Side side) {
    }

    public void sendToAll(IMessage message) {
    }

    public void sendTo(IMessage message, ServerPlayerEntity player) {
    }

    public void sendToServer(IMessage message) {
    }
}
