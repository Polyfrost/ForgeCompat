package cc.polyfrost.oneconfig.config.data;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.elements.OptionPage;

public class Mod implements Comparable<Mod> {
    public final String name;
    public final ModType modType;
    public final String modIcon;
    public Config config;
    public OptionPage defaultPage = new OptionPage();

    public Mod(String name, ModType modType, String modIcon) {
        this.name = name;
        this.modType = modType;
        this.modIcon = modIcon;
    }

    public Mod(String name, ModType modType) {
        this(name, modType, null);
    }

    @Override
    public int compareTo(Mod mod) {
        return name.compareTo(mod.name);
    }
}
