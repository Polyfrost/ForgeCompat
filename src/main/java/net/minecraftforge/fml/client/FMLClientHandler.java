package net.minecraftforge.fml.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.GameMenuScreen;
import net.minecraft.client.gui.screen.menu.world.SelectWorldScreen;
import net.minecraft.client.resource.pack.ResourcePack;
import net.minecraft.client.gui.screen.menu.multiplayer.ConnectScreen;
import net.minecraft.client.options.ServerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.handler.PacketHandler;
import net.minecraft.server.network.handler.ServerHandshakePacketHandler;
import net.minecraft.server.network.handler.ServerLoginPacketHandler;
import net.minecraft.server.network.handler.ServerPlayPacketHandler;
import net.minecraft.server.network.handler.ServerQueryPacketHandler;
import net.minecraft.util.BlockableEventLoop;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.forgecompat.ForgeCompat;
import org.polyfrost.forgecompat.Hooks;
import org.polyfrost.forgecompat.ModResourcePack;

public class FMLClientHandler {
    private static final FMLClientHandler INSTANCE = new FMLClientHandler();
    private boolean loading = true;
    private final Map<String, ResourcePack> resourcePacks = new ConcurrentHashMap<>();

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
        loading = false;
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

    public void connectToServer(Screen guiMultiplayer, ServerListEntry serverEntry) {
        getClient().openScreen(new ConnectScreen(guiMultiplayer, getClient(), serverEntry));
    }

    public LocalClientPlayerEntity getClientPlayerEntity() {
        return getClient().player;
    }

    public ClientWorld getWorldClient() {
        return getClient().world;
    }

    public BlockableEventLoop getWorldThread(PacketHandler net) {
        if (net instanceof ServerPlayPacketHandler || net instanceof ServerLoginPacketHandler || net instanceof ServerQueryPacketHandler || net instanceof ServerHandshakePacketHandler) {
            return FMLCommonHandler.instance().getMinecraftServerInstance();
        }
        return getClient();
    }

    public String getCurrentLanguage() {
        return getClient().getLanguageManager().getLanguage().getCode();
    }

    public boolean isLoading() {
        return loading;
    }

    public void setupServerList() {
    }

    public void addSpecialModEntries(ArrayList<ModContainer> mods) {
    }

    public ResourcePack getResourcePackFor(String modId) {
        return resourcePacks.computeIfAbsent(modId, id -> {
            ModContainer mod = ForgeCompat.MODS.get(id);
            File source = mod == null ? null : mod.getSource();
            return source != null && source.isFile() ? new ModResourcePack(source) : null;
        });
    }

    public void tryLoadExistingWorld(SelectWorldScreen selectWorldGui, String dirName, String saveName) {
        getClient().startGame(dirName, saveName, null);
    }

    public IModGuiFactory getGuiFactoryFor(ModContainer selectedMod) {
        try {
            return ForgeCompat.guiFactory(selectedMod.getModId());
        } catch (ReflectiveOperationException e) {
            FMLLog.getLogger().error("Failed to create gui factory of {}", selectedMod.getModId(), e);
            return null;
        }
    }

    public void showInGameModOptions(GameMenuScreen guiIngameMenu) {
        showGuiScreen(new GuiModList(guiIngameMenu));
    }

    public boolean isGUIOpen(Class<?> type) {
        return getClient().screen != null && getClient().screen.getClass() == type;
    }
}
