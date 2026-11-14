package net.minecraftforge.event;

import net.minecraft.server.command.Command;
import net.minecraft.server.command.source.CommandSource;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class CommandEvent extends Event {
    public final Command command;
    public final CommandSource sender;
    public String[] parameters;
    public Throwable exception;

    public CommandEvent(Command command, CommandSource sender, String[] parameters) {
        this.command = command;
        this.sender = sender;
        this.parameters = parameters;
    }
}
