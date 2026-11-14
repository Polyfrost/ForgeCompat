package net.minecraftforge.fml.common;

public class MinecraftDummyContainer extends DummyModContainer {
    public MinecraftDummyContainer(String actualMCVersion) {
        super(new ModMetadata("Minecraft", "Minecraft", actualMCVersion));
    }
}
