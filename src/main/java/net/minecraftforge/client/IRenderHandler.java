package net.minecraftforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;

public abstract class IRenderHandler {
    public abstract void render(float partialTicks, ClientWorld world, Minecraft mc);
}
