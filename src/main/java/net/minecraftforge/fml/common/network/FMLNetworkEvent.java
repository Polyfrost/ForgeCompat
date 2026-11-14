package net.minecraftforge.fml.common.network;

import net.minecraft.network.Connection;
import net.minecraft.network.handler.PacketHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class FMLNetworkEvent<T extends PacketHandler> extends Event {
    public final Connection manager;
    public final T handler;

    public FMLNetworkEvent(T handler, Connection manager) {
        this.handler = handler;
        this.manager = manager;
    }

    public FMLNetworkEvent(Connection manager) {
        this(null, manager);
    }

    public static class ClientConnectedToServerEvent extends FMLNetworkEvent<PacketHandler> {
        public final boolean isLocal;
        public final String connectionType;

        public ClientConnectedToServerEvent(Connection manager, String connectionType) {
            super(manager);
            this.isLocal = manager.isLocal();
            this.connectionType = connectionType;
        }
    }

    public static class ClientDisconnectionFromServerEvent extends FMLNetworkEvent<PacketHandler> {
        public ClientDisconnectionFromServerEvent(Connection manager) {
            super(manager);
        }
    }

    public static class ServerConnectionFromClientEvent extends FMLNetworkEvent<PacketHandler> {
        public final boolean isLocal;

        public ServerConnectionFromClientEvent(PacketHandler handler, Connection manager) {
            super(handler, manager);
            this.isLocal = manager.isLocal();
        }
    }

    public static class ServerDisconnectionFromClientEvent extends FMLNetworkEvent<PacketHandler> {
        public ServerDisconnectionFromClientEvent(PacketHandler handler, Connection manager) {
            super(handler, manager);
        }
    }

    public static class CustomPacketEvent<S extends PacketHandler> extends FMLNetworkEvent<S> {
        public final FMLProxyPacket packet;
        public FMLProxyPacket reply;

        public CustomPacketEvent(S handler, Connection manager, FMLProxyPacket packet) {
            super(handler, manager);
            this.packet = packet;
        }
    }

    public static class ClientCustomPacketEvent extends CustomPacketEvent<PacketHandler> {
        public ClientCustomPacketEvent(PacketHandler handler, Connection manager, FMLProxyPacket packet) {
            super(handler, manager, packet);
        }
    }

    public static class ServerCustomPacketEvent extends CustomPacketEvent<PacketHandler> {
        public ServerCustomPacketEvent(PacketHandler handler, Connection manager, FMLProxyPacket packet) {
            super(handler, manager, packet);
        }
    }
}
