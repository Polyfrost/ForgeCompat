package net.minecraftforge.fml.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.forgecompat.ForgeCompat;
import org.polyfrost.forgecompat.Hooks;

public class FMLClientHandler {
    private static final FMLClientHandler INSTANCE = new FMLClientHandler();

    public static FMLClientHandler instance() {
        return INSTANCE;
    }

    public Minecraft getClient() {
        return Minecraft.getInstance();
    }

    public void haltGame(String message, Throwable cause) {
        throw new RuntimeException(message, cause);
    }

    public void onInitializationComplete() {
        getClient().gui = new GuiIngameForge(getClient());
        Hooks.applyLookAndFeel();
        ForgeCompat.initMods();
        getClient().options.load();
    }

    public boolean hasOptifine() {
        return false;
    }

    public net.minecraft.network.Connection getClientToServerNetworkManager() {
        return getClient().getNetworkHandler() == null ? null : getClient().getNetworkHandler().getConnection();
    }

    public void showGuiScreen(Object screen) {
        getClient().openScreen((net.minecraft.client.gui.screen.Screen) screen);
    }

    public boolean isGUIOpen(Class<?> type) {
        return getClient().screen != null && getClient().screen.getClass() == type;
    }
}
