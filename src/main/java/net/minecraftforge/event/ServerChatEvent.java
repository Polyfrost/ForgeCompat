package net.minecraftforge.event;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ServerChatEvent extends Event {
    public final String message, username;
    public final ServerPlayerEntity player;
    public Text component;

    public ServerChatEvent(ServerPlayerEntity player, String message, Text component) {
        this.message = message;
        this.player = player;
        this.username = player.getName();
        this.component = component;
    }
}
