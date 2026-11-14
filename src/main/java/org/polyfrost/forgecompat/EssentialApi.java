package org.polyfrost.forgecompat;

import java.util.List;
import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.oneconfig.api.notifications.v1.Notifications;

public final class EssentialApi {
    private static final EssentialApi INSTANCE = new EssentialApi();
    private static volatile boolean opening;
    private static volatile Screen pending;

    static {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    private EssentialApi() {
    }

    public static void open(Object screen) {
        pending = (Screen) screen;
        opening = true;
    }

    public static Object screen() {
        return Minecraft.getInstance().screen;
    }

    public static void notify(String title, String message, float seconds, Runnable onClick) {
        if (FabricLoader.getInstance().isModLoaded("oneconfig")) {
            Notifications.Builder builder = Notifications.builder(title, message).duration(seconds * 1000);
            if (onClick != null) builder.onClick(onClick);
            builder.send();
        } else {
            Minecraft.getInstance().gui.getChat().addMessage(new LiteralText(title + ": " + message));
        }
    }

    public static void registerCommand(String name, Consumer<String[]> handler) {
        ClientCommandHandler.instance.register(new AbstractCommand() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getUsage(CommandSource source) {
                return "/" + name;
            }

            @Override
            public boolean canUse(CommandSource source) {
                return true;
            }

            @Override
            public void run(CommandSource source, String[] args) {
                handler.accept(args);
            }

            @Override
            public List<String> getSuggestions(CommandSource source, String[] args, net.minecraft.util.math.BlockPos pos) {
                return null;
            }
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!opening) return;
        opening = false;
        Minecraft.getInstance().openScreen(pending);
    }
}
