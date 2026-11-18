package cc.polyfrost.oneconfig.config.core;

import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;

public final class ConfigUtils {
    private ConfigUtils() {
    }

    public static OptionSubcategory getSubCategory(OptionPage page, String category, String subcategory) {
        for (OptionSubcategory sub : page.subcategories) {
            if (sub.category.equals(category) && sub.name.equals(subcategory)) return sub;
        }
        OptionSubcategory sub = new OptionSubcategory(category, subcategory);
        page.subcategories.add(sub);
        return sub;
    }
}
