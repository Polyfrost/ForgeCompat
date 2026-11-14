package net.minecraftforge.fml.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.Side;

public class FMLCommonHandler {
    private static final FMLCommonHandler INSTANCE = new FMLCommonHandler();

    public static FMLCommonHandler instance() {
        return INSTANCE;
    }

    public EventBus bus() {
        return MinecraftForge.EVENT_BUS;
    }

    public Side getSide() {
        return Side.CLIENT;
    }

    public Side getEffectiveSide() {
        Thread thread = Thread.currentThread();
        return thread.getName().equals("Server thread") || thread.getName().startsWith("Netty Server IO") ? Side.SERVER : Side.CLIENT;
    }

    public MinecraftServer getMinecraftServerInstance() {
        MinecraftServer server = Minecraft.getInstance().getServer();
        return server != null ? server : MinecraftServer.getInstance();
    }

    public String getModName() {
        return "fml,forge";
    }

    private java.util.List<String> brandings;

    public void computeBranding() {
        if (brandings == null) brandings = new java.util.ArrayList<>(java.util.Arrays.asList("Minecraft 1.8.9", "Forge Compat", "Forge 11.15.1.2318"));
    }

    public java.util.List<String> getBrandings(boolean includeMC) {
        computeBranding();
        return includeMC ? brandings : brandings.subList(1, brandings.size());
    }

    public void firePlayerCraftingEvent(PlayerEntity player, ItemStack crafted, Inventory craftMatrix) {
    }

    public void onRenderTickStart(float timer) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, timer));
    }

    public void onRenderTickEnd(float timer) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, timer));
    }

    public boolean shouldServerBeKilledQuietly() {
        return false;
    }

    public void exitJava(int exitCode, boolean hardExit) {
        if (hardExit) Runtime.getRuntime().halt(exitCode);
        else System.exit(exitCode);
    }
}
