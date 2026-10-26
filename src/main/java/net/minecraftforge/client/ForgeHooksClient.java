package net.minecraftforge.client;

import net.minecraft.client.gui.screen.Screen;

public class ForgeHooksClient {
    public static void drawScreen(Screen screen, int mouseX, int mouseY, float partialTicks) {
        screen.render(mouseX, mouseY, partialTicks);
    }
}
