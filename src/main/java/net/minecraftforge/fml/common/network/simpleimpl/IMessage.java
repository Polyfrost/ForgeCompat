package net.minecraftforge.fml.common.network.simpleimpl;

import io.netty.buffer.ByteBuf;

public interface IMessage {
    void fromBytes(ByteBuf buf);

    void toBytes(ByteBuf buf);
}
