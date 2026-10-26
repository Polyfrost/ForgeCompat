package org.polyfrost.forgecompat.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow
    private Channel channel;

    public Channel channel() {
        return channel;
    }
}
