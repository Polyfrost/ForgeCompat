package net.minecraftforge.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class FakePlayer extends ServerPlayerEntity {
    public FakePlayer(ServerWorld world, GameProfile name) {
        super(MinecraftServer.getInstance(), world, name, new ServerPlayerInteractionManager(world));
    }

    @Override
    public void sendMessage(Text message) {
    }

    @Override
    public void tick() {
    }
}
