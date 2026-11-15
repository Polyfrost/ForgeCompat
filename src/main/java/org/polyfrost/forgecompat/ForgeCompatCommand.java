package org.polyfrost.forgecompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;

public class ForgeCompatCommand extends AbstractCommand {
    private Screen pending;

    @Override
    public String getName() {
        return "forgecompat";
    }

    @Override
    public String getUsage(CommandSource source) {
        return "/forgecompat config <modid>";
    }

    @Override
    public boolean canUse(CommandSource source) {
        return true;
    }

    @Override
    public void run(CommandSource source, String[] args) throws CommandException {
        if (args.length != 2 || !args[0].equals("config")) throw new IncorrectUsageException(getUsage(source));
        if (!ForgeCompat.GUI_FACTORIES.containsKey(args[1])) throw new CommandException("No config screen for " + args[1] + ". Available: " + ForgeCompat.GUI_FACTORIES.keySet());
        try {
            IModGuiFactory instance = ForgeCompat.guiFactory(args[1]);
            Class<? extends Screen> gui = instance.mainConfigGuiClass();
            if (gui == null) throw new CommandException(args[1] + " has no config screen");
            pending = gui.getConstructor(Screen.class).newInstance((Screen) null);
        } catch (ReflectiveOperationException e) {
            ForgeCompat.LOGGER.error("Failed to open config screen of {}", args[1], e);
            throw new CommandException("Failed to open config screen of " + args[1]);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (pending == null) return;
        Minecraft.getInstance().openScreen(pending);
        pending = null;
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String[] args, BlockPos pos) {
        return args.length == 2 ? suggestMatching(args, ForgeCompat.GUI_FACTORIES.keySet()) : Arrays.asList("config");
    }
}
