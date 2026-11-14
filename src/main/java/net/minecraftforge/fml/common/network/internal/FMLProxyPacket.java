package net.minecraftforge.fml.common.network.internal;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

public class FMLProxyPacket {
    private final String channel;
    private final PacketByteBuf payload;

    public FMLProxyPacket(PacketByteBuf payload, String channel) {
        this.payload = payload;
        this.channel = channel;
    }

    public String channel() {
        return channel;
    }

    public ByteBuf payload() {
        return payload;
    }

    public CustomPayloadC2SPacket toC2SPacket() {
        return new CustomPayloadC2SPacket(channel, payload);
    }
}
