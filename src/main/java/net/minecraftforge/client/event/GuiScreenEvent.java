package net.minecraftforge.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Window;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

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

        @Cancelable
        public static class Pre extends InitGuiEvent {
            public Pre(Screen gui, List<ButtonWidget> buttonList) {
                super(gui, buttonList);
            }
        }

        public static class Post extends InitGuiEvent {
            public Post(Screen gui, List<ButtonWidget> buttonList) {
                super(gui, buttonList);
            }
        }
    }

    public static class MouseInputEvent extends GuiScreenEvent {
        public MouseInputEvent(Screen gui) {
            super(gui);
        }

        @Cancelable
        public static class Pre extends MouseInputEvent {
            public Pre(Screen gui) {
                super(gui);
            }
        }

        public static class Post extends MouseInputEvent {
            public Post(Screen gui) {
                super(gui);
            }
        }
    }

    public static class KeyboardInputEvent extends GuiScreenEvent {
        public KeyboardInputEvent(Screen gui) {
            super(gui);
        }

        @Cancelable
        public static class Pre extends KeyboardInputEvent {
            public Pre(Screen gui) {
                super(gui);
            }
        }

        public static class Post extends KeyboardInputEvent {
            public Post(Screen gui) {
                super(gui);
            }
        }
    }

    public static class DrawScreenEvent extends GuiScreenEvent {
        public final int mouseX;
        public final int mouseY;
        public final float renderPartialTicks;

        public DrawScreenEvent(Screen gui, int mouseX, int mouseY, float renderPartialTicks) {
            super(gui);
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.renderPartialTicks = renderPartialTicks;
        }

        public static class Pre extends DrawScreenEvent {
            public Pre(Screen gui, int mouseX, int mouseY, float renderPartialTicks) {
                super(gui, mouseX, mouseY, renderPartialTicks);
            }
        }

        public static class Post extends DrawScreenEvent {
            public Post(Screen gui, int mouseX, int mouseY, float renderPartialTicks) {
                super(gui, mouseX, mouseY, renderPartialTicks);
            }
        }
    }

    public static class ActionPerformedEvent extends GuiScreenEvent {
        public ButtonWidget button;
        public List<ButtonWidget> buttonList;

        public ActionPerformedEvent(Screen gui, ButtonWidget button, List<ButtonWidget> buttonList) {
            super(gui);
            this.button = button;
            this.buttonList = new ArrayList<>(buttonList);
        }

        public static class Pre extends ActionPerformedEvent {
            public Pre(Screen gui, ButtonWidget button, List<ButtonWidget> buttonList) {
                super(gui, button, buttonList);
            }
        }

        public static class Post extends ActionPerformedEvent {
            public Post(Screen gui, ButtonWidget button, List<ButtonWidget> buttonList) {
                super(gui, button, buttonList);
            }
        }
    }

    public static class BackgroundDrawnEvent extends GuiScreenEvent {
        private final int mouseX;
        private final int mouseY;

        public BackgroundDrawnEvent(Screen gui) {
            super(gui);
            Window window = new Window(Minecraft.getInstance());
            this.mouseX = Mouse.getX() * window.getWidth() / Minecraft.getInstance().width;
            this.mouseY = window.getHeight() - Mouse.getY() * window.getHeight() / Minecraft.getInstance().height - 1;
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }
    }
}
