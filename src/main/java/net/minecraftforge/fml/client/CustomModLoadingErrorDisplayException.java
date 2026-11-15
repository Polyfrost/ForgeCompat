package net.minecraftforge.fml.client;

import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.render.TextRenderer;
import net.minecraftforge.fml.common.EnhancedRuntimeException;
import net.minecraftforge.fml.common.IFMLHandledException;

public abstract class CustomModLoadingErrorDisplayException extends EnhancedRuntimeException implements IFMLHandledException {
    public CustomModLoadingErrorDisplayException() {
    }

    public CustomModLoadingErrorDisplayException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract void initGui(FatalErrorScreen errorScreen, TextRenderer fontRenderer);

    public abstract void drawScreen(FatalErrorScreen errorScreen, TextRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime);

    @Override
    public void printStackTrace(WrappedPrintStream stream) {
    }
}
