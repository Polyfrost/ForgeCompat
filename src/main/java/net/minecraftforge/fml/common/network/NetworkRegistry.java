package net.minecraftforge.fml.common.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.Connection;
import net.minecraft.network.handler.PacketHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class NetworkRegistry {
    public static final NetworkRegistry INSTANCE = new NetworkRegistry();
    private static final Map<String, FMLEventChannel> EVENT_CHANNELS = new ConcurrentHashMap<>();
    static final Map<Object, IGuiHandler> GUI_HANDLERS = new ConcurrentHashMap<>();

    public SimpleNetworkWrapper newSimpleChannel(String name) {
        return new SimpleNetworkWrapper();
    }

    public java.util.EnumMap<net.minecraftforge.fml.relauncher.Side, Object> newChannel(String name, io.netty.channel.ChannelHandler... handlers) {
        return new java.util.EnumMap<>(net.minecraftforge.fml.relauncher.Side.class);
    }

    public FMLEventChannel newEventDrivenChannel(String name) {
        return EVENT_CHANNELS.computeIfAbsent(name, FMLEventChannel::new);
    }

    public boolean hasChannel(String name, net.minecraftforge.fml.relauncher.Side side) {
        return EVENT_CHANNELS.containsKey(name);
    }

    public void registerGuiHandler(Object mod, IGuiHandler handler) {
        GUI_HANDLERS.put(mod, handler);
    }

    public static IGuiHandler guiHandler(Object mod) {
        return GUI_HANDLERS.get(mod);
    }

    public static boolean onClientPayload(PacketHandler handler, Connection connection, CustomPayloadS2CPacket packet) {
        FMLEventChannel channel = EVENT_CHANNELS.get(packet.getChannel());
        if (channel == null) return false;
        channel.bus.post(new FMLNetworkEvent.ClientCustomPacketEvent(handler, connection, new FMLProxyPacket(packet.getData(), packet.getChannel())));
        return true;
    }

    public static class TargetPoint {
        public final double x, y, z, range;
        public final int dimension;

        public TargetPoint(int dimension, double x, double y, double z, double range) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.range = range;
            this.dimension = dimension;
        }
    }
}
