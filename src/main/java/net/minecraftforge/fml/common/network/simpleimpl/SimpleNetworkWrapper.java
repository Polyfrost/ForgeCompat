package net.minecraftforge.fml.common.network.simpleimpl;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class SimpleNetworkWrapper {
    public SimpleNetworkWrapper() {
    }

    public SimpleNetworkWrapper(String channelName) {
    }

    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> handler, Class<REQ> type, int discriminator, Side side) {
    }

    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> handler, Class<REQ> type, int discriminator, Side side) {
    }

    public void sendToAll(IMessage message) {
    }

    public void sendTo(IMessage message, ServerPlayerEntity player) {
    }

    public void sendToServer(IMessage message) {
    }

    public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
    }

    public void sendToDimension(IMessage message, int dimensionId) {
    }
}
