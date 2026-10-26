package net.minecraftforge.fml.common.network;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class NetworkRegistry {
    public static final NetworkRegistry INSTANCE = new NetworkRegistry();

    public SimpleNetworkWrapper newSimpleChannel(String name) {
        return new SimpleNetworkWrapper();
    }
}
