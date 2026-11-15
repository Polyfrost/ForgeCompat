package net.minecraftforge.fml.common;

import com.google.common.base.CharMatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.handler.PacketHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Formatting;
import net.minecraft.util.BlockableEventLoop;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FMLCommonHandler {
    private static final FMLCommonHandler INSTANCE = new FMLCommonHandler();
    private static final CharMatcher ALLOWED_CHARS = CharMatcher.anyOf("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000");

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

    private List<String> brandings;

    public void computeBranding() {
        if (brandings == null) brandings = new ArrayList<>(Arrays.asList("Minecraft 1.8.9", "Forge Compat", "Forge 11.15.1.2318"));
    }

    public List<String> getBrandings(boolean includeMC) {
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

    public Logger getFMLLogger() {
        return FMLLog.getLogger();
    }

    public ModContainer findContainerFor(Object mod) {
        if (mod instanceof String id) return Loader.instance().getIndexedModList().get(id);
        for (ModContainer container : Loader.instance().getModList()) {
            if (container.matches(mod)) return container;
        }
        return null;
    }

    private final List<ICrashCallable> crashCallables = new CopyOnWriteArrayList<>();
    private volatile CountDownLatch exitLatch;

    public void registerCrashCallable(ICrashCallable callable) {
        crashCallables.add(callable);
    }

    public void enhanceCrashReport(CrashReport crashReport, CrashReportCategory category) {
        for (ICrashCallable call : crashCallables) category.add(call.getLabel(), call);
    }

    public void raiseException(Throwable exception, String message, boolean stopGame) {
        FMLLog.log(Level.ERROR, exception, "Something raised an exception. The message was '%s'. 'stopGame' is %b", message, stopGame);
        if (stopGame) FMLClientHandler.instance().haltGame(message, exception);
    }

    public void showGuiScreen(Object screen) {
        FMLClientHandler.instance().showGuiScreen(screen);
    }

    public String getCurrentLanguage() {
        return FMLClientHandler.instance().getCurrentLanguage();
    }

    public String stripSpecialChars(String message) {
        return ALLOWED_CHARS.retainFrom(Formatting.strip(message));
    }

    public File getSavesDirectory() {
        return new File(Minecraft.getInstance().gameDir, "saves");
    }

    public BlockableEventLoop getWorldThread(PacketHandler net) {
        return FMLClientHandler.instance().getWorldThread(net);
    }

    public void expectServerStopped() {
        exitLatch = new CountDownLatch(1);
    }

    public void handleServerStopped() {
        CountDownLatch latch = exitLatch;
        if (latch != null) {
            latch.countDown();
            exitLatch = null;
        }
    }

    public void handleExit(int exitCode) {
        CountDownLatch latch = exitLatch;
        if (latch != null) {
            try {
                FMLLog.info("Waiting for the server to terminate/save.");
                if (!latch.await(10, TimeUnit.SECONDS)) FMLLog.warning("The server didn't stop within 10 seconds, exiting anyway.");
                else FMLLog.info("Server terminated.");
            } catch (InterruptedException e) {
                FMLLog.warning("Interrupted wait, exiting.");
            }
        }
        exitJava(exitCode, false);
    }

    public boolean shouldServerBeKilledQuietly() {
        return false;
    }

    public void exitJava(int exitCode, boolean hardExit) {
        if (hardExit) Runtime.getRuntime().halt(exitCode);
        else System.exit(exitCode);
    }
}
