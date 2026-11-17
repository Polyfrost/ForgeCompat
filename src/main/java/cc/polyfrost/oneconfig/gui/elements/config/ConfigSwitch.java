package cc.polyfrost.oneconfig.gui.elements.config;

import java.lang.reflect.Field;

public class ConfigSwitch {
    public final Field field;
    public final Object parent;
    public final String name;
    public final String description;
    public final String category;
    public final String subcategory;

    public ConfigSwitch(Field field, Object parent, String name, String description, String category, String subcategory, int size) {
        this.field = field;
        this.parent = parent;
        this.name = name;
        this.description = description;
        this.category = category;
        this.subcategory = subcategory;
    }
}
