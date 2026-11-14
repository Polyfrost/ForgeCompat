package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket.class, net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket.class})
public class CustomPayloadMixin {
    @WrapOperation(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeBytes(Lio/netty/buffer/ByteBuf;)Lio/netty/buffer/ByteBuf;"))
    private ByteBuf forgecompat$keepReadable(PacketByteBuf buf, ByteBuf data, Operation<ByteBuf> original) {
        return original.call(buf, data.duplicate());
    }
}
