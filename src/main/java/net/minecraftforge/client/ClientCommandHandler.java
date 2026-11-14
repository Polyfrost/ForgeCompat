package net.minecraftforge.client;

import net.minecraft.server.command.Command;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.handler.CommandRegistry;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

public class ClientCommandHandler extends CommandRegistry {
    public static final ClientCommandHandler instance = new ClientCommandHandler();

    @Override
    public int run(CommandSource source, String message) {
        message = message.trim();
        if (message.startsWith("/")) message = message.substring(1);
        String[] split = message.split(" ");
        String[] args = new String[split.length - 1];
        System.arraycopy(split, 1, args, 0, args.length);
        Command command = getCommands().get(split[0]);
        if (command == null) return 0;
        try {
            if (command.canUse(source)) {
                CommandEvent event = new CommandEvent(command, source, args);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    if (event.exception != null) throw event.exception;
                    return 1;
                }
                command.run(source, event.parameters);
                return 1;
            }
            source.sendMessage(error("commands.generic.permission"));
        } catch (IncorrectUsageException e) {
            source.sendMessage(error("commands.generic.usage", error(e.getMessage(), e.getArgs())));
        } catch (CommandException e) {
            source.sendMessage(error(e.getMessage(), e.getArgs()));
        } catch (Throwable t) {
            source.sendMessage(error("commands.generic.exception"));
            t.printStackTrace();
        }
        return -1;
    }

    private static Text error(String key, Object... args) {
        TranslatableText text = new TranslatableText(key, args);
        text.getStyle().setColor(Formatting.RED);
        return text;
    }
}
