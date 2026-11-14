package net.minecraftforge.common;

import java.io.File;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.Dimension;

public class DimensionManager {
    private static final Integer[] VANILLA = {0, -1, 1};

    private static MinecraftServer server() {
        return Minecraft.getInstance().getServer();
    }

    public static ServerWorld getWorld(int id) {
        MinecraftServer server = server();
        return server == null ? null : server.getWorld(id);
    }

    public static ServerWorld[] getWorlds() {
        MinecraftServer server = server();
        return server == null || server.worlds == null ? new ServerWorld[0] : server.worlds;
    }

    public static Integer[] getIDs() {
        return VANILLA.clone();
    }

    public static Integer[] getStaticDimensionIDs() {
        return VANILLA.clone();
    }

    public static boolean isDimensionRegistered(int id) {
        return Arrays.asList(VANILLA).contains(id);
    }

    public static int getProviderType(int id) {
        return id;
    }

    public static Dimension createProviderFor(int id) {
        return Dimension.fromId(id);
    }

    public static File getCurrentSaveRootDirectory() {
        ServerWorld world = getWorld(0);
        return world == null ? null : world.getStorage().getDirectory();
    }
}
