package cc.polyfrost.oneconfig.libs.universal;

import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UTextComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.text.Text;

public final class UChat {
    private UChat() {
    }

    public static void chat(Object message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) return;
        Text text = message instanceof Text t ? t : new UTextComponent(String.valueOf(message));
        mc.gui.getChat().addMessage(text);
    }

    public static void say(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.sendChat(message);
    }
}
