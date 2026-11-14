package org.polyfrost.forgecompat.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket.class, net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket.class})
public class CustomPayloadMixin {
    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeBytes(Lio/netty/buffer/ByteBuf;)Lio/netty/buffer/ByteBuf;"))
    private ByteBuf forgecompat$keepReadable(PacketByteBuf buf, ByteBuf data) {
        return buf.writeBytes(data, data.readerIndex(), data.readableBytes());
    }
}
