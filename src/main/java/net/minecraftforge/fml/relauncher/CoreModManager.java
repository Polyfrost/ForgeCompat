package net.minecraftforge.fml.relauncher;

import java.util.ArrayList;
import java.util.List;

public class CoreModManager {
    private static final List<String> IGNORED = new ArrayList<>();
    private static final List<String> REPARSEABLE = new ArrayList<>();

    public static List<String> getIgnoredMods() {
        return IGNORED;
    }

    public static List<String> getReparseableCoremods() {
        return REPARSEABLE;
    }

    public static List<String> getLoadedCoremods() {
        return new ArrayList<>();
    }
}
