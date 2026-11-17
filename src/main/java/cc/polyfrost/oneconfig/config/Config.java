package cc.polyfrost.oneconfig.config;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import org.polyfrost.forgecompat.LegacyOneConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Config {
    public final transient Mod mod;
    public final transient String configFile;
    public boolean enabled;
    public final transient HashMap<String, Object> optionNames = new HashMap<>();
    private transient LegacyOneConfig.Bridge bridge;
    private transient boolean initialized;
    private final transient List<Consumer<LegacyOneConfig.Bridge>> pending = new ArrayList<>();

    public Config(Mod modData, String configFile, boolean enabled) {
        this.mod = modData;
        this.configFile = configFile;
        this.enabled = enabled;
        modData.config = this;
    }

    public Config(Mod modData, String configFile) {
        this(modData, configFile, true);
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        LegacyOneConfig.whenReady(this::attach);
    }

    private void attach() {
        bridge = LegacyOneConfig.register(this);
        pending.forEach(action -> action.accept(bridge));
        pending.clear();
    }

    public void openGui() {
        if (bridge != null) bridge.open();
    }

    public void save() {
        if (bridge != null) bridge.save();
    }

    protected void registerKeyBind(OneKeyBind keyBind, Runnable runnable) {
        keyBind.setRunnable(runnable);
        LegacyOneConfig.registerKeyBind(keyBind);
    }

    protected final void addDependency(String option, Supplier<Boolean> condition) {
        addDependency(option, option, condition);
    }

    protected final void addDependency(String option, String dependentOption) {
        withBridge(b -> b.dependency(option, dependentOption));
    }

    protected final void addDependency(String option, String dependencyName, Supplier<Boolean> condition) {
        withBridge(b -> b.dependency(option, dependencyName, condition));
    }

    protected final void hideIf(String option, Supplier<Boolean> condition) {
        withBridge(b -> b.hideIf(option, condition));
    }

    protected final void hideIf(String option, String dependentOption) {
        withBridge(b -> b.hideIf(option, dependentOption));
    }

    protected final void addListener(String option, Runnable runnable) {
        withBridge(b -> b.listener(option, runnable));
    }

    // mods may call these before initialize()
    private void withBridge(Consumer<LegacyOneConfig.Bridge> action) {
        if (bridge != null) action.accept(bridge);
        else pending.add(action);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
