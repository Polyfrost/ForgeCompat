package org.polyfrost.forgecompat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.hud.TextHud;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Properties;
import org.polyfrost.oneconfig.api.config.v1.Property;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.Visualizer;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.hud.v1.OneConfigHudWrapper;
import org.polyfrost.oneconfig.api.hud.v1.events.HudEditorToggleEvent;
import org.polyfrost.oneconfig.api.ui.v1.OneConfigUI;
import org.polyfrost.oneconfig.api.ui.v1.page.OneConfigPages;

public final class LegacyOneConfig {
    private static final List<TextHud> HUDS = new CopyOnWriteArrayList<>();
    private static volatile boolean editing;
    private static boolean listening;
    private static final java.util.Map<cc.polyfrost.oneconfig.config.core.OneKeyBind, Boolean> KEYBINDS = new java.util.concurrent.ConcurrentHashMap<>();

    private LegacyOneConfig() {
    }

    public static void registerKeyBind(cc.polyfrost.oneconfig.config.core.OneKeyBind keyBind) {
        listen();
        KEYBINDS.put(keyBind, false);
    }

    private static void listen() {
        if (!listening) {
            listening = true;
            MinecraftForge.EVENT_BUS.register(new Listener());
            EventManager.register(HudEditorToggleEvent.class, (Consumer<HudEditorToggleEvent>) event -> editing = event.open);
        }
    }

    public static Bridge register(cc.polyfrost.oneconfig.config.Config legacy) {
        listen();
        Bridge bridge = new Bridge(legacy);
        bridge.initialize(false);
        return bridge;
    }

    public static final class Listener {
        @SubscribeEvent
        public void onTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            for (TextHud hud : HUDS) hud.forgecompat$tick(editing);
            boolean inGame = net.minecraft.client.Minecraft.getInstance().screen == null;
            for (java.util.Map.Entry<cc.polyfrost.oneconfig.config.core.OneKeyBind, Boolean> entry : KEYBINDS.entrySet()) {
                boolean active = inGame && entry.getKey().isActive();
                if (active && !entry.getValue()) entry.getKey().run();
                entry.setValue(active);
            }
        }

        @SubscribeEvent
        public void onRender(RenderGameOverlayEvent.Post event) {
            if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
            for (TextHud hud : HUDS) hud.forgecompat$render(editing);
        }
    }

    public static final class Bridge extends Config {
        private final cc.polyfrost.oneconfig.config.Config legacy;
        private final List<HudWrapper> huds = new ArrayList<>();

        Bridge(cc.polyfrost.oneconfig.config.Config legacy) {
            super(legacy.configFile, legacy.mod.name, category(legacy.mod.modType));
            this.legacy = legacy;
        }

        public void dependency(String option, String dependentOption) {
            addDependency(option, dependentOption);
        }

        public void dependency(String option, String name, Supplier<Boolean> condition) {
            addDependency(option, name, () -> Boolean.TRUE.equals(condition.get()) ? Property.Display.SHOWN : Property.Display.DISABLED);
        }

        public void hideIf(String option, Supplier<Boolean> condition) {
            java.util.function.BooleanSupplier hidden = () -> Boolean.TRUE.equals(condition.get());
            super.hideIf(option, hidden);
        }

        @Override
        public void hideIf(String option, String dependentOption) {
            super.hideIf(option, dependentOption);
        }

        public void listener(String option, Runnable runnable) {
            addCallback(option, runnable);
        }

        public void open() {
            OneConfigUI.open(OneConfigPages.modConfig(id));
        }

        // redeclared so register() can call it from this package
        @Override
        protected void initialize(boolean byConfigManager) {
            super.initialize(byConfigManager);
        }

        @Override
        protected Tree makeTree() {
            huds.clear();
            Tree tree = Tree.tree(id);
            Property<?> enabled = property("enabled", "Enabled", "", field(cc.polyfrost.oneconfig.config.Config.class, "enabled"), legacy);
            enabled.addMetadata("hidden", true);
            enabled.addMetadata("masterToggle", true);
            tree.put(enabled);
            collect(tree, legacy, cc.polyfrost.oneconfig.config.Config.class, null, null, "");
            return tree;
        }

        @Override
        protected void onInitialized(Tree tree) {
            for (HudWrapper hud : huds) {
                hud.register();
                HUDS.add(hud.hud);
            }
        }

        private void collect(Tree tree, Object owner, Class<?> stop, String category, String subcategory, String prefix) {
            for (Class<?> cls = owner.getClass(); cls != stop && cls != Object.class; cls = cls.getSuperclass()) {
                for (Field f : cls.getDeclaredFields()) {
                    if (f.isSynthetic() || f.isAnnotationPresent(Exclude.class)) continue;
                    Object target = Modifier.isStatic(f.getModifiers()) ? null : owner;
                    String id = prefix + f.getName();
                    if (f.getAnnotation(Switch.class) instanceof Switch a) {
                        option(tree, property(id, a.name(), a.description(), f, target), Visualizer.SwitchVisualizer.class,
                                category == null ? a.category() : category, subcategory == null ? a.subcategory() : subcategory);
                    } else if (f.getAnnotation(Dropdown.class) instanceof Dropdown a) {
                        Property<?> p = property(id, a.name(), a.description(), f, target);
                        p.addMetadata("options", a.options());
                        option(tree, p, Visualizer.DropdownVisualizer.class,
                                category == null ? a.category() : category, subcategory == null ? a.subcategory() : subcategory);
                    } else if (f.getAnnotation(DualOption.class) instanceof DualOption a) {
                        f.setAccessible(true);
                        Property<Integer> p = Properties.functional(
                                () -> (Boolean) get(f, target) ? 1 : 0,
                                value -> set(f, target, value != null && value == 1),
                                id, a.name(), description(a.description()), Integer.class);
                        p.addMetadata("options", new String[]{a.left(), a.right()});
                        option(tree, p, Visualizer.RadioVisualizer.class,
                                category == null ? a.category() : category, subcategory == null ? a.subcategory() : subcategory);
                    } else if (f.getAnnotation(HUD.class) instanceof HUD a && get(f, target) instanceof TextHud hud) {
                        addHud(tree, hud, id, a);
                    }
                }
            }
        }

        private void addHud(Tree tree, TextHud hud, String id, HUD a) {
            Class<TextHud> cls = TextHud.class;
            option(tree, property(id + "_enabled", a.name(), "", field(cls, "enabled"), hud), Visualizer.SwitchVisualizer.class, a.category(), a.name());
            for (String name : new String[]{"x", "y", "scale"}) {
                Property<?> p = property(id + "_" + name, null, "", field(cls, name), hud);
                p.addMetadata("hidden", true);
                tree.put(p);
            }
            collect(tree, hud, cls, a.category(), a.name(), id + "_");
            huds.add(new HudWrapper(this, hud, id, a.name()));
        }

        private static void option(Tree tree, Property<?> p, Class<? extends Visualizer> visualizer, String category, String subcategory) {
            p.addMetadata("visualizer", visualizer);
            p.addMetadata("category", category);
            p.addMetadata("subcategory", subcategory.isEmpty() ? "General" : subcategory);
            tree.put(p);
        }

        private static Config.Category category(ModType type) {
            return switch (type) {
                case PVP -> Config.Category.COMBAT;
                case HUD -> Config.Category.HUD;
                case UTIL_QOL -> Config.Category.QOL;
                case HYPIXEL, SKYBLOCK -> Config.Category.HYPIXEL;
                case THIRD_PARTY -> Config.Category.OTHER;
            };
        }
    }

    private static final class HudWrapper implements OneConfigHudWrapper {
        private static final float EMPTY_WIDTH = 40f;
        private static final float EMPTY_HEIGHT = 10f;

        private final Bridge config;
        private final TextHud hud;
        private String id;
        private String name;

        HudWrapper(Bridge config, TextHud hud, String fieldId, String name) {
            this.config = config;
            this.hud = hud;
            this.id = config.id + "/" + fieldId;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getModId() {
            return config.id;
        }

        @Override
        public float getX() {
            return hud.x;
        }

        @Override
        public void setX(float x) {
            hud.x = x;
        }

        @Override
        public float getY() {
            return hud.y;
        }

        @Override
        public void setY(float y) {
            hud.y = y;
        }

        @Override
        public float getScale() {
            return hud.scale;
        }

        @Override
        public void setScale(float scale) {
            hud.scale = scale;
        }

        @Override
        public float getScaledWidth() {
            float width = hud.forgecompat$width();
            return width > 0f ? width : EMPTY_WIDTH * hud.scale;
        }

        @Override
        public void setScaledWidth(float width) {
        }

        @Override
        public float getScaledHeight() {
            float height = hud.forgecompat$height();
            return height > 0f ? height : EMPTY_HEIGHT * hud.scale;
        }

        @Override
        public void setScaledHeight(float height) {
        }

        @Override
        public boolean getHidden() {
            return !hud.enabled;
        }

        @Override
        public void setHidden(boolean hidden) {
            hud.enabled = !hidden;
        }

        @Override
        public void save() {
            config.save();
        }
    }

    private static Field field(Class<?> owner, String name) {
        try {
            return owner.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String description(String description) {
        return description.isEmpty() ? null : description;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Property<?> property(String id, String title, String description, Field f, Object owner) {
        f.setAccessible(true);
        Supplier getter = () -> get(f, owner);
        Consumer setter = value -> set(f, owner, value);
        return Properties.functional(getter, setter, id, title, description(description), (Class) f.getType());
    }

    private static Object get(Field f, Object owner) {
        try {
            f.setAccessible(true);
            return f.get(owner);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void set(Field f, Object owner, Object value) {
        if (value instanceof Number n) {
            Class<?> type = f.getType();
            if (type == int.class) value = n.intValue();
            else if (type == float.class) value = n.floatValue();
            else if (type == double.class) value = n.doubleValue();
            else if (type == long.class) value = n.longValue();
        }
        try {
            f.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
