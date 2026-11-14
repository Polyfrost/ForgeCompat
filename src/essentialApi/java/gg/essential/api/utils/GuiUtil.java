package gg.essential.api.utils;

import gg.essential.api.EssentialAPI;
import net.minecraft.client.gui.GuiScreen;

public interface GuiUtil {
    Companion Companion = new Companion();

    void openScreen(GuiScreen screen);

    GuiScreen openedScreen();

    static void open(GuiScreen screen) {
        EssentialAPI.getGuiUtil().openScreen(screen);
    }

    static GuiScreen getOpenedScreen() {
        return EssentialAPI.getGuiUtil().openedScreen();
    }

    final class Companion {
        public void open(GuiScreen screen) {
            GuiUtil.open(screen);
        }

        public GuiScreen getOpenedScreen() {
            return GuiUtil.getOpenedScreen();
        }
    }
}
