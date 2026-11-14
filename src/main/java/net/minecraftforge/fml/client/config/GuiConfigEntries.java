package net.minecraftforge.fml.client.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.input.Mouse;

public class GuiConfigEntries {
    private static final int ROW_HEIGHT = 24;

    public final GuiConfig owningScreen;
    public final Minecraft mc;
    public final List<IConfigEntry> listEntries = new ArrayList<>();
    private int top;
    private int bottom;
    private int scroll;

    public GuiConfigEntries(GuiConfig parent, Minecraft mc) {
        this.owningScreen = parent;
        this.mc = mc;
        if (parent.configElements == null) return;
        for (IConfigElement element : parent.configElements) {
            if (element != null && element.showInGui()) listEntries.add(new Entry(element));
        }
    }

    public void initGui() {
        top = owningScreen.titleLine2 != null ? 33 : 23;
        bottom = owningScreen.height - 32;
        for (IConfigEntry entry : listEntries) ((Entry) entry).initWidgets();
        layout();
    }

    private void layout() {
        scroll = Math.max(0, Math.min(scroll, listEntries.size() * ROW_HEIGHT - (bottom - top)));
        for (int i = 0; i < listEntries.size(); i++) {
            Entry entry = (Entry) listEntries.get(i);
            entry.y = top + i * ROW_HEIGHT - scroll;
            boolean visible = entry.y >= top && entry.y + ROW_HEIGHT <= bottom;
            if (entry.button != null) {
                entry.button.y = entry.y;
                entry.button.visible = visible;
            } else if (entry.field != null) {
                entry.field.y = entry.y + 1;
                entry.field.setVisible(visible);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float tickDelta) {
        for (IConfigEntry entry : listEntries) ((Entry) entry).draw(mouseX, mouseY);
    }

    public void drawScreenPost(int mouseX, int mouseY, float tickDelta) {
        if (mouseY < top || mouseY >= bottom) return;
        for (IConfigEntry entry : listEntries) {
            Entry e = (Entry) entry;
            String comment = e.element.getComment();
            if (comment == null || comment.isEmpty() || e.category) continue;
            if (mouseX >= owningScreen.width / 2 - 150 && mouseX < owningScreen.width / 2 + 20 && mouseY >= e.y && mouseY < e.y + ROW_HEIGHT) {
                List<String> lines = new ArrayList<>();
                for (String line : comment.split("\n")) lines.addAll(mc.textRenderer.split(line, 300));
                owningScreen.drawToolTip(lines, mouseX, mouseY);
            }
        }
    }

    public boolean mouseClicked(int x, int y, int button) {
        if (y < top || y >= bottom) return false;
        for (IConfigEntry entry : listEntries) {
            Entry e = (Entry) entry;
            if (e.button != null && e.button.mouseClicked(mc, x, y)) {
                e.button.playClickSound(mc.getSoundManager());
                e.clicked();
                return true;
            }
        }
        return false;
    }

    public void mouseClickedPassThru(int x, int y, int button) {
        for (IConfigEntry entry : listEntries) {
            TextFieldWidget field = ((Entry) entry).field;
            if (field != null && field.isVisible()) field.mouseClicked(x, y, button);
        }
    }

    public boolean mouseReleased(int x, int y, int button) {
        return false;
    }

    public void keyTyped(char chr, int key) {
        for (IConfigEntry entry : listEntries) {
            Entry e = (Entry) entry;
            if (e.field != null && e.field.keyPressed(chr, key)) e.current = e.field.getText();
        }
    }

    public void updateScreen() {
        for (IConfigEntry entry : listEntries) {
            TextFieldWidget field = ((Entry) entry).field;
            if (field != null) field.tick();
        }
    }

    public void handleMouseInput() {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll -= Integer.signum(wheel) * ROW_HEIGHT;
            layout();
        }
    }

    public void onGuiClosed() {
    }

    public boolean hasChangedEntry(boolean includeChildren) {
        for (IConfigEntry entry : listEntries) {
            if (((Entry) entry).isChanged(includeChildren)) return true;
        }
        return false;
    }

    public boolean areAllEntriesDefault(boolean includeChildren) {
        for (IConfigEntry entry : listEntries) {
            if (!((Entry) entry).isDefault(includeChildren)) return false;
        }
        return true;
    }

    public boolean areAnyEntriesEnabled(boolean includeChildren) {
        return true;
    }

    public void setAllToDefault(boolean includeChildren) {
        for (IConfigEntry entry : listEntries) ((Entry) entry).setToDefault(includeChildren);
    }

    public void undoAllChanges(boolean includeChildren) {
        for (IConfigEntry entry : listEntries) ((Entry) entry).undoChanges(includeChildren);
    }

    public boolean saveConfigElements() {
        boolean requiresMcRestart = false;
        for (IConfigEntry entry : listEntries) requiresMcRestart |= ((Entry) entry).save();
        return requiresMcRestart;
    }

    public interface IConfigEntry {
    }

    public abstract static class ListEntryBase implements IConfigEntry {
    }

    public abstract static class ButtonEntry extends ListEntryBase {
    }

    public abstract static class BooleanEntry extends ButtonEntry {
    }

    public abstract static class CycleValueEntry extends ButtonEntry {
    }

    public abstract static class NumberSliderEntry extends ButtonEntry {
    }

    public abstract static class StringEntry extends ListEntryBase {
    }

    public abstract static class IntegerEntry extends StringEntry {
    }

    public abstract static class DoubleEntry extends StringEntry {
    }

    public abstract static class CategoryEntry extends ListEntryBase {
    }

    private final class Entry implements IConfigEntry {
        final IConfigElement element;
        final boolean category;
        final String label;
        GuiConfig childScreen;
        String before;
        String current;
        ButtonWidget button;
        TextFieldWidget field;
        int y;

        Entry(IConfigElement element) {
            this.element = element;
            this.category = !element.isProperty();
            String name = element.getName();
            String key = element.getLanguageKey();
            String translated = key == null ? null : net.minecraft.client.resource.language.I18n.translate(key);
            this.label = translated != null && !translated.equals(key) ? translated : name;
            if (category) {
                String line2 = (owningScreen.titleLine2 == null ? "" : owningScreen.titleLine2) + " > " + label;
                childScreen = new GuiConfig(owningScreen, element.getChildElements(), owningScreen.modID, owningScreen.configID,
                        owningScreen.allRequireWorldRestart || element.requiresWorldRestart(), owningScreen.allRequireMcRestart || element.requiresMcRestart(),
                        owningScreen.title, line2);
            } else {
                before = current = value();
            }
        }

        private boolean isList() {
            return element.isList();
        }

        private String value() {
            return isList() ? join(element.getList()) : String.valueOf(element.get());
        }

        private String defaultValue() {
            return isList() ? join(element.getDefaults()) : String.valueOf(element.getDefault());
        }

        private String join(Object[] values) {
            if (values == null) return "";
            StringBuilder out = new StringBuilder();
            for (Object value : values) {
                if (out.length() > 0) out.append(", ");
                out.append(value);
            }
            return out.toString();
        }

        private boolean isBoolean() {
            return !isList() && element.getType() == ConfigGuiType.BOOLEAN;
        }

        private String[] validValues() {
            String[] values = isList() ? null : element.getValidValues();
            return values == null || values.length == 0 ? null : values;
        }

        void initWidgets() {
            int x = owningScreen.width / 2;
            button = null;
            field = null;
            if (category) {
                button = new GuiButtonExt(0, x - 150, 0, 300, 18, label);
            } else if (isBoolean() || validValues() != null) {
                button = new GuiButtonExt(0, x + 20, 0, 130, 20, current);
            } else {
                field = new TextFieldWidget(0, mc.textRenderer, x + 21, 0, 128, 18);
                field.setMaxLength(Integer.MAX_VALUE);
                field.setText(current);
            }
        }

        void clicked() {
            if (category) {
                mc.openScreen(childScreen);
                return;
            }
            if (isBoolean()) {
                current = Boolean.toString(!Boolean.parseBoolean(current));
            } else {
                String[] values = validValues();
                int index = Arrays.asList(values).indexOf(current);
                current = values[(index + 1) % values.length];
            }
            button.message = current;
        }

        void draw(int mouseX, int mouseY) {
            if (y < top || y + ROW_HEIGHT > bottom) return;
            if (button != null) button.render(mc, mouseX, mouseY);
            if (category) return;
            int color = !isValid() ? 0xFF5555 : isChanged(false) ? 0xFFFF55 : 0xFFFFFF;
            owningScreen.drawString(mc.textRenderer, mc.textRenderer.trim(label, 165), owningScreen.width / 2 - 150, y + 6, color);
            if (field != null) field.render();
        }

        void sync() {
            if (button != null && !category) button.message = current;
            if (field != null) field.setText(current);
        }

        boolean isChanged(boolean includeChildren) {
            if (category) return includeChildren && childScreen.entryList.hasChangedEntry(true);
            return !Objects.equals(before, current);
        }

        boolean isDefault(boolean includeChildren) {
            if (category) return !includeChildren || childScreen.entryList.areAllEntriesDefault(true);
            return Objects.equals(defaultValue(), current);
        }

        void setToDefault(boolean includeChildren) {
            if (category) {
                if (includeChildren) childScreen.entryList.setAllToDefault(true);
                return;
            }
            current = defaultValue();
            sync();
        }

        void undoChanges(boolean includeChildren) {
            if (category) {
                if (includeChildren) childScreen.entryList.undoAllChanges(true);
                return;
            }
            current = before;
            sync();
        }

        private String[] items() {
            String trimmed = current.trim();
            return trimmed.isEmpty() ? new String[0] : trimmed.split("\\s*,\\s*");
        }

        private boolean isValid() {
            if (isList()) {
                String[] items = items();
                if (element.isListLengthFixed() && items.length != element.getDefaults().length) return false;
                if (element.getMaxListLength() > 0 && items.length > element.getMaxListLength()) return false;
                for (String item : items) if (!isValid(item)) return false;
                return true;
            }
            return isValid(current);
        }

        private boolean isValid(String value) {
            try {
                switch (element.getType()) {
                    case INTEGER -> {
                        int i = Integer.parseInt(value);
                        return inRange(i);
                    }
                    case DOUBLE -> {
                        double d = Double.parseDouble(value);
                        return inRange(d);
                    }
                    default -> {
                        Pattern pattern = element.getValidationPattern();
                        return pattern == null || pattern.matcher(value).matches();
                    }
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private boolean inRange(double value) {
            Object min = element.getMinValue();
            Object max = element.getMaxValue();
            return (min == null || value >= Double.parseDouble(min.toString())) && (max == null || value <= Double.parseDouble(max.toString()));
        }

        boolean save() {
            if (category) return childScreen.entryList.saveConfigElements();
            if (!isChanged(false) || !isValid()) return false;
            if (isList()) element.set((Object[]) items());
            else element.set(current);
            before = current;
            return element.requiresMcRestart() || owningScreen.allRequireMcRestart;
        }
    }
}
