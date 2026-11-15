package net.minecraftforge.fml.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.multiplayer.DisconnectedScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

public class GuiMessageDialog extends DisconnectedScreen {
    protected String buttonText;

    public GuiMessageDialog(Screen nextScreen, String title, Text message, String buttonText) {
        super(nextScreen, title, message);
        this.buttonText = buttonText;
    }

    @Override
    public void init() {
        super.init();
        ((ButtonWidget) buttons.get(0)).message = I18n.translate(buttonText);
    }
}
