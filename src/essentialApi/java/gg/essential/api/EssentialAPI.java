package gg.essential.api;

import gg.essential.api.gui.Notifications;
import gg.essential.api.utils.GuiUtil;

public interface EssentialAPI {
    Companion Companion = new Companion();

    Notifications notifications();

    GuiUtil guiUtil();

    static EssentialAPI getInstance() {
        return Shim.INSTANCE;
    }

    static Notifications getNotifications() {
        return Shim.INSTANCE;
    }

    static GuiUtil getGuiUtil() {
        return Shim.INSTANCE;
    }

    final class Companion {
        public EssentialAPI getInstance() {
            return Shim.INSTANCE;
        }

        public Notifications getNotifications() {
            return Shim.INSTANCE;
        }

        public GuiUtil getGuiUtil() {
            return Shim.INSTANCE;
        }
    }
}
