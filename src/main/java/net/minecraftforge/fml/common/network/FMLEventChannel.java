package net.minecraftforge.fml.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class FMLEventChannel {
    final String name;
    final EventBus bus = new EventBus();

    FMLEventChannel(String name) {
        this.name = name;
    }

    public void register(Object handler) {
        bus.register(handler);
    }

    public void unregister(Object handler) {
        bus.unregister(handler);
    }

    public void sendToServer(FMLProxyPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getNetworkHandler() != null) mc.getNetworkHandler().getConnection().send(packet.toC2SPacket());
    }

    public void sendTo(FMLProxyPacket packet, ServerPlayerEntity player) {
    }

    public void sendToAll(FMLProxyPacket packet) {
    }

    public void sendToDimension(FMLProxyPacket packet, int dimension) {
    }

    public void sendToAllAround(FMLProxyPacket packet, NetworkRegistry.TargetPoint point) {
    }
}
