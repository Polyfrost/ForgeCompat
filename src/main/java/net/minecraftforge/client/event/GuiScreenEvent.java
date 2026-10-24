package net.minecraftforge.client.event;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiScreenEvent extends Event {
    public final Screen gui;

    public GuiScreenEvent(Screen gui) {
        this.gui = gui;
    }

    public static class InitGuiEvent extends GuiScreenEvent {
        public List<ButtonWidget> buttonList;

        public InitGuiEvent(Screen gui, List<ButtonWidget> buttonList) {
            super(gui);
            this.buttonList = buttonList;
        }

        public static class Post extends InitGuiEvent {
            public Post(Screen gui, List<ButtonWidget> buttonList) {
                super(gui, buttonList);
            }
        }
    }
}
