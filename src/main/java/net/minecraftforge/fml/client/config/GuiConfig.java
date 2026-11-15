package net.minecraftforge.fml.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class GuiConfig extends Screen {
    public final Screen parentScreen;
    public String title = "Config GUI";
    public String titleLine2;
    public final List<IConfigElement> configElements;
    public final List<IConfigEntry> initEntries;
    public GuiConfigEntries entryList;
    protected GuiButtonExt btnDefaultAll;
    protected GuiButtonExt btnUndoAll;
    protected GuiCheckBox chkApplyGlobally;
    public final String modID;
    public final String configID;
    public final boolean isWorldRunning;
    public final boolean allRequireWorldRestart;
    public final boolean allRequireMcRestart;
    public boolean needsRefresh = true;

    public GuiConfig(Screen parentScreen, List<IConfigElement> configElements, String modID, String configID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
        this(parentScreen, configElements, modID, configID, allRequireWorldRestart, allRequireMcRestart, title, null);
    }

    public GuiConfig(Screen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
        this(parentScreen, configElements, modID, null, allRequireWorldRestart, allRequireMcRestart, title, null);
    }

    public GuiConfig(Screen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
        this(parentScreen, configElements, modID, null, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
    }

    public GuiConfig(Screen parentScreen, List<IConfigElement> configElements, String modID, String configID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
        this.minecraft = Minecraft.getInstance();
        this.parentScreen = parentScreen;
        this.configElements = configElements;
        this.entryList = new GuiConfigEntries(this, minecraft);
        this.initEntries = new ArrayList<>(entryList.listEntries);
        this.allRequireWorldRestart = allRequireWorldRestart;
        this.allRequireMcRestart = allRequireMcRestart;
        this.modID = modID;
        this.configID = configID;
        this.isWorldRunning = minecraft.world != null;
        if (title != null) this.title = title;
        this.titleLine2 = titleLine2;
        if (this.titleLine2 != null && this.titleLine2.startsWith(" > ")) this.titleLine2 = this.titleLine2.replaceFirst(" > ", "");
    }

    public static String getAbridgedConfigPath(String path) {
        String gameDir = Minecraft.getInstance().gameDir.getAbsolutePath().replace("\\", "/");
        if (gameDir.endsWith(".")) gameDir = gameDir.substring(0, gameDir.length() - 1);
        return path.replace("\\", "/").replace(gameDir, "/.minecraft/").replace("//", "/");
    }

    @Override
    public void init() {
        Keyboard.enableRepeatEvents(true);
        if (entryList == null || needsRefresh) {
            entryList = new GuiConfigEntries(this, minecraft);
            needsRefresh = false;
        }
        buttons.clear();
        String applyGlobally = "Apply globally";
        int checkWidth = textRenderer.getWidth(applyGlobally) + 13;
        int half = (100 + 5 + 100 + 5 + 100 + 5 + checkWidth) / 2;
        int left = width / 2 - half;
        buttons.add(new GuiButtonExt(2000, left, height - 29, 100, 20, "Done"));
        buttons.add(btnDefaultAll = new GuiButtonExt(2001, left + 210, height - 29, 100, 20, "Reset to default"));
        buttons.add(btnUndoAll = new GuiButtonExt(2002, left + 105, height - 29, 100, 20, "Undo changes"));
        buttons.add(chkApplyGlobally = new GuiCheckBox(2003, left + 315, height - 24, applyGlobally, false));
        entryList.initGui();
    }

    @Override
    public void removed() {
        entryList.onGuiClosed();
        if (configID != null && parentScreen instanceof GuiConfig parent) {
            parent.needsRefresh = true;
            parent.init();
        }
        if (!(parentScreen instanceof GuiConfig)) Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 2000) {
            try {
                if ((configID != null || !(parentScreen instanceof GuiConfig)) && entryList.hasChangedEntry(true)) {
                    boolean requiresMcRestart = entryList.saveConfigElements();
                    if (Loader.isModLoaded(modID)) {
                        ConfigChangedEvent event = new OnConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (!event.getResult().equals(Result.DENY)) MinecraftForge.EVENT_BUS.post(new PostConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart));
                        if (parentScreen instanceof GuiConfig parent) parent.needsRefresh = true;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            minecraft.openScreen(parentScreen);
        } else if (button.id == 2001) {
            entryList.setAllToDefault(chkApplyGlobally.isChecked());
        } else if (button.id == 2002) {
            entryList.undoAllChanges(chkApplyGlobally.isChecked());
        }
    }

    @Override
    public void handleMouse() {
        super.handleMouse();
        entryList.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        if (button != 0 || !entryList.mouseClicked(x, y, button)) {
            entryList.mouseClickedPassThru(x, y, button);
            super.mouseClicked(x, y, button);
        }
    }

    @Override
    protected void mouseReleased(int x, int y, int button) {
        if (button != 0 || !entryList.mouseReleased(x, y, button)) super.mouseReleased(x, y, button);
    }

    @Override
    protected void keyPressed(char chr, int key) {
        if (key == Keyboard.KEY_ESCAPE) minecraft.openScreen(parentScreen);
        else entryList.keyTyped(chr, key);
    }

    @Override
    public void tick() {
        super.tick();
        entryList.updateScreen();
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        renderBackground();
        entryList.drawScreen(mouseX, mouseY, tickDelta);
        drawCenteredString(textRenderer, title, width / 2, 8, 0xFFFFFF);
        if (titleLine2 != null) drawCenteredString(textRenderer, textRenderer.trim(titleLine2, width - 6), width / 2, 18, 0xFFFFFF);
        boolean global = chkApplyGlobally.isChecked();
        btnUndoAll.active = entryList.areAnyEntriesEnabled(global) && entryList.hasChangedEntry(global);
        btnDefaultAll.active = entryList.areAnyEntriesEnabled(global) && !entryList.areAllEntriesDefault(global);
        super.render(mouseX, mouseY, tickDelta);
        entryList.drawScreenPost(mouseX, mouseY, tickDelta);
    }

    public void drawToolTip(List<String> lines, int x, int y) {
        renderTooltip(lines, x, y);
    }
}
