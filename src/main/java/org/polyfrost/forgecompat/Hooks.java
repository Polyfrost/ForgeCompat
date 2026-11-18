package org.polyfrost.forgecompat;

import com.google.common.reflect.ClassPath;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.CreativeModeTab;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraftforge.fml.common.registry.RegistryDelegate;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class Hooks {
    static final Set<String> FORGE_METHODS = Set.of(
            "canRiderInteract/0", "getDisplayNameString/0", "shouldSpreadItems/0", "getRegistryName/0",
            "getDigSpeed/2", "getMetadata/1", "getToolClasses/1", "isSideSolid/3"
    );

    private static int modifiers;
    private static final ByteBuffer mouseButtons = ByteBuffer.allocate(16);
    private static Object lookAndFeel;

    public static Class<?> forName(String name) throws ClassNotFoundException {
        return Class.forName(runtimeClassName(name), true, Hooks.class.getClassLoader());
    }

    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(runtimeClassName(name), initialize, loader);
    }

    public static Class<?> loadClassInfo(ClassPath.ClassInfo info) {
        try {
            return info.load();
        } catch (IllegalStateException | NoClassDefFoundError original) {
            try {
                return Class.forName(info.getName(), false, Hooks.class.getClassLoader());
            } catch (ClassNotFoundException | LinkageError e) {
                original.addSuppressed(e);
                throw original;
            }
        }
    }

    public static Object[] toArray(Collection<?> collection, Class<?> arrayType) {
        try {
            return collection.toArray((Object[]) Array.newInstance(arrayType.getComponentType(), collection.size()));
        } catch (ArrayStoreException e) {
            // the elements don't fit the cast either, keep the old behaviour
            return collection.toArray();
        }
    }

    private static String runtimeClassName(String name) {
        if (name == null || !name.startsWith("net.minecraft.")) return name;
        String mapped = ForgeCompat.mappings().mcpToRuntime(name.replace('.', '/'));
        return mapped == null ? name : mapped.replace('/', '.');
    }

    public static String deobfClass(String notch) {
        Mappings m = ForgeCompat.mappings();
        String runtime = m.notchToInt.get(notch);
        return runtime == null ? mapNested(notch, Hooks::deobfClass) : m.intToMcp.getOrDefault(runtime, notch);
    }

    public static String obfClass(String mcp) {
        Mappings m = ForgeCompat.mappings();
        String runtime = m.fromSrg.map(mcp);
        return runtime == null ? mapNested(mcp, Hooks::obfClass) : m.intToNotch.getOrDefault(runtime, mcp);
    }

    private static String mapNested(String name, UnaryOperator<String> mapper) {
        int i = name.lastIndexOf('$');
        return i < 0 ? name : mapper.apply(name.substring(0, i)) + name.substring(i);
    }

    public static Set<String> obfedClasses() {
        return Collections.unmodifiableSet(ForgeCompat.mappings().notchToInt.keySet());
    }

    public static String staticFieldType(String oldOwner, String oldName, String newOwner, String newName) {
        return ForgeCompat.mappings().staticFieldType(oldOwner, oldName, newOwner, newName);
    }

    public static String srgMethod(String owner, String name, String desc) {
        Mappings m = ForgeCompat.mappings();
        String intOwner = m.notchToInt.getOrDefault(owner, owner);
        String runtime = m.toInt.mapMethodName(owner, name, desc);
        String srg = m.srgMethod(intOwner, runtime, m.toInt.mapMethodDesc(desc));
        return srg == null ? name : srg;
    }

    public static String srgField(String owner, String name, String desc) {
        Mappings m = ForgeCompat.mappings();
        String intOwner = m.notchToInt.getOrDefault(owner, owner);
        if (desc == null) desc = m.notchFieldDesc(owner, name);
        if (desc == null) return name;
        String runtime = m.toInt.mapFieldName(owner, name, desc);
        String srg = m.srgField(intOwner, runtime, m.toInt.mapDesc(desc));
        return srg == null ? name : srg;
    }

    public static Field getDeclaredField(Class<?> owner, String name) throws NoSuchFieldException {
        try {
            return owner.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            String runtime = name.startsWith("field_") ? ForgeCompat.mappings().srgToRuntime(name) : null;
            if (runtime != null) return owner.getDeclaredField(runtime);
            if (owner == Field.class && name.equals("modifiers")) return Hooks.class.getDeclaredField("modifiers");
            if (owner == Mouse.class && name.equals("buttons")) return Hooks.class.getDeclaredField("mouseButtons");
            throw e;
        }
    }

    public static CodeSource getCodeSource(ProtectionDomain domain) {
        CodeSource source = domain.getCodeSource();
        if (source == null || source.getLocation() == null) return source;
        try {
            Path original = ForgeCompat.ORIGINAL_JARS.get(Path.of(source.getLocation().toURI()).toAbsolutePath().normalize());
            return original == null ? source : new CodeSource(original.toUri().toURL(), source.getCodeSigners());
        } catch (Exception e) {
            return source;
        }
    }

    public static void setLookAndFeel(LookAndFeel laf) throws UnsupportedLookAndFeelException {
        if (Display.isCreated()) UIManager.setLookAndFeel(laf);
        else lookAndFeel = laf;
    }

    public static void setLookAndFeel(String className) throws ReflectiveOperationException, UnsupportedLookAndFeelException {
        if (Display.isCreated()) UIManager.setLookAndFeel(className);
        else lookAndFeel = className;
    }

    public static void applyLookAndFeel() {
        Object laf = lookAndFeel;
        if (laf == null) return;
        lookAndFeel = null;
        try {
            if (laf instanceof LookAndFeel instance) UIManager.setLookAndFeel(instance);
            else UIManager.setLookAndFeel((String) laf);
        } catch (Exception e) {
            ForgeCompat.LOGGER.error("Failed to set look and feel {}", laf, e);
        }
    }

    public static int nextCreativeTabId() {
        CreativeModeTab[] tabs = CreativeModeTab.ALL;
        try {
            CreativeModeTab.class.getField(ForgeCompat.mappings().srgToRuntime("field_78032_a")).set(null, Arrays.copyOf(tabs, tabs.length + 1));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to grow the creative tab array", e);
        }
        return tabs.length;
    }

    public static boolean canRiderInteract(Object entity) {
        return false;
    }

    public static String getDisplayNameString(Object player) {
        return ((PlayerEntity) player).getName();
    }

    public static boolean shouldSpreadItems(Object renderer) {
        return true;
    }

    public static String getRegistryName(Object entry) {
        Identifier id = entry instanceof Block block ? Block.REGISTRY.getKey(block) : Item.REGISTRY.getKey((Item) entry);
        return id == null ? null : id.toString();
    }

    public static float getDigSpeed(Object item, ItemStack stack, BlockState state) {
        return ((Item) item).getMiningSpeed(stack, state.getBlock());
    }

    public static int getMetadata(Object item, ItemStack stack) {
        return stack.getMetadata();
    }

    public static Set<String> getToolClasses(Object item, ItemStack stack) {
        if (item instanceof PickaxeItem) return Set.of("pickaxe");
        if (item instanceof AxeItem) return Set.of("axe");
        if (item instanceof ShovelItem) return Set.of("shovel");
        return Collections.emptySet();
    }

    public static String getEntityName(ItemStack stack) {
        return Entities.getKey(stack.getMetadata());
    }

    private static Field buttonHeight;

    public static int buttonHeight(ButtonWidget button) {
        try {
            if (buttonHeight == null) {
                buttonHeight = ButtonWidget.class.getDeclaredField(ForgeCompat.mappings().srgToRuntime("field_146121_g"));
                buttonHeight.setAccessible(true);
            }
            return buttonHeight.getInt(button);
        } catch (ReflectiveOperationException e) {
            return 20;
        }
    }

    public static int nextPotionId() {
        StatusEffect[] effects = StatusEffect.BY_ID;
        for (int i = 1; i < effects.length; i++) if (effects[i] == null) return i;
        throw new IllegalStateException("No free potion ids left");
    }

    public static RegistryDelegate<?> delegate(Object entry) {
        Identifier key = entry instanceof Block block ? Block.REGISTRY.getKey(block) : Item.REGISTRY.getKey((Item) entry);
        return new RegistryDelegate<Object>() {
            public Object get() { return entry; }
            public String name() { return String.valueOf(key); }
            public Identifier getResourceName() { return key; }
            @SuppressWarnings("unchecked")
            public Class<Object> type() { return (Class<Object>) (entry instanceof Block ? Block.class : Item.class); }
        };
    }

    public static float eyeHeight(Object entity) {
        return ((Entity) entity).getEyeHeight();
    }

    public static boolean isSideSolid(Object world, BlockPos pos, Direction side, boolean defaultValue) {
        BlockState state = ((WorldView) world).getBlockState(pos);
        return state == null ? defaultValue : state.getBlock().isCube();
    }

    private static volatile ClientWorld realWorld;

    public static boolean isSceneWorld(World world) {
        return world != null && world.isClient && world != realWorld;
    }

    public static ClientWorld realWorld() {
        return realWorld;
    }

    public static void setRealWorld(ClientWorld world) {
        realWorld = world;
    }
}
