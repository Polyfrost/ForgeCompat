package cc.polyfrost.oneconfig.config.elements;

import java.util.ArrayList;

public class OptionSubcategory {
    public final String category;
    public final String name;
    public final ArrayList<Object> options = new ArrayList<>();

    public OptionSubcategory(String category, String name) {
        this.category = category;
        this.name = name;
    }
}
